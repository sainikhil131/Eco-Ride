package com.example.ecoride.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoride.BuildConfig
import com.example.ecoride.navigation.Ride
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun RideDetailScreen(
    rideHash: Int?,
    db: FirebaseFirestore
) {
    var ride by remember { mutableStateOf<Ride?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Initialize Razorpay Checkout
    LaunchedEffect(Unit) {
        Checkout.preload(context)
    }

    // Payment result launcher
    val paymentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Payment result is handled by MainActivity implementing PaymentResultListener
    }

    LaunchedEffect(rideHash) {
        coroutineScope.launch {
            db.collection("rides")
                .get()
                .addOnSuccessListener { result ->
                    val rides = result.map { it.toObject(Ride::class.java).copy(id = it.id) }
                    ride = rides.find { it.hashCode() == rideHash } ?: rides.firstOrNull()
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        return
    }

    ride?.let { selectedRide ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = selectedRide.date,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total price for 1 passenger: ${selectedRide.price}",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Text(
                        text = selectedRide.departureTime,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        text = selectedRide.fromLocation,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = selectedRide.arrivalTime,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        text = selectedRide.toLocation,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = selectedRide.driverName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedRide.carDetails,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Button(
                onClick = {
                    // Start Razorpay payment
                    val activity = context as? Activity
                    activity?.let {
                        startRazorpayPayment(
                            activity = it,
                            amount = parsePriceToPaise(selectedRide.price),
                            rideId = selectedRide.id,
                            onSuccess = { paymentId ->
                                // Update Firestore with payment details
                                coroutineScope.launch {
                                    db.collection("rides").document(selectedRide.id)
                                        .update("paymentStatus", "PAID", "paymentId", paymentId)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Payment successful! Ride booked.", Toast.LENGTH_LONG).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Failed to update ride status", Toast.LENGTH_LONG).show()
                                        }
                                }
                            },
                            onError = { errorCode, errorMessage ->
                                Toast.makeText(context, "Payment failed: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))
            ) {
                Text(
                    text = "Request a Ride",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    } ?: run {
        Text(
            text = "Ride not found",
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        )
    }
}

// Helper function to start Razorpay payment
private fun startRazorpayPayment(
    activity: Activity,
    amount: Int,
    rideId: String,
    onSuccess: (String) -> Unit,
    onError: (Int, String) -> Unit
) {
    val checkout = Checkout()
    checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID) // Use the key from BuildConfig

    try {
        val options = JSONObject().apply {
            put("name", "EcoRide")
            put("description", "Payment for Ride $rideId")
            put("currency", "INR")
            put("amount", amount) // Amount in paise
            put("prefill", JSONObject().apply {
                put("email", "test@example.com") // Replace with user email
                put("contact", "1234567890") // Replace with user phone
            })
            put("theme", JSONObject().apply {
                put("color", "#FF4D4D")
            })
        }
        checkout.open(activity, options)
    } catch (e: Exception) {
        onError(-1, e.message ?: "Error initiating payment")
    }

    // Payment results are handled by the MainActivity implementing PaymentResultListener
}

// Helper function to convert price (e.g., "$20" or "20") to paise
private fun parsePriceToPaise(price: String): Int {
    val cleanPrice = price.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
    return (cleanPrice * 100).toInt() // Convert to paise
}