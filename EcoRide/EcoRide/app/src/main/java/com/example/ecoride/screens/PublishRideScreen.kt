package com.example.ecoride.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoride.navigation.Ride
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PublishRideScreen(
    onPublishClick: () -> Unit,
    db: FirebaseFirestore
) {
    // State management for UI elements
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var fromLatLng by remember { mutableStateOf<LatLng?>(null) }
    var toLatLng by remember { mutableStateOf<LatLng?>(null) }
    var dateTime by remember { mutableStateOf("Today, 5:30 PM") }
    var seatsAvailable by remember { mutableIntStateOf(1) }
    var price by remember { mutableStateOf("") }
    var carDetails by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    var selectedDate by remember { mutableStateOf(calendar.time) }

    // Date and Time Pickers
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = calendar.time
                dateTime = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()).format(selectedDate)
            },
            year,
            month,
            day
        )
    }

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                selectedDate = calendar.time
                dateTime = SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault()).format(selectedDate)
            },
            hour,
            minute,
            false
        )
    }

    // Places API Initialization (should ideally be in MainActivity or Application class)
    if (!Places.isInitialized()) {
        Places.initialize(context, "AIzaSyDy7XwE7BXxp9_8lzCg3VHBGHArmNEWVVs") // Replace with your actual API key
    }

    // Autocomplete Launchers for From and To locations
    val fromAutocompleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            fromLocation = place.name ?: ""
            fromLatLng = place.latLng
        }
    }

    val toAutocompleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            toLocation = place.name ?: ""
            toLatLng = place.latLng
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Publish a Ride",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "RIDE DETAILS",
            fontSize = 16.sp,
            color = Color(0xFF1A3C34)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // From Location with Autocomplete
        OutlinedTextField(
            value = fromLocation,
            onValueChange = { fromLocation = it },
            label = { Text("From") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(context)
                    fromAutocompleteLauncher.launch(intent)
                },
            enabled = false // Disable manual input
        )
        Spacer(modifier = Modifier.height(8.dp))

        // To Location with Autocomplete
        OutlinedTextField(
            value = toLocation,
            onValueChange = { toLocation = it },
            label = { Text("To") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(context)
                    toAutocompleteLauncher.launch(intent)
                },
            enabled = false // Disable manual input
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Date and Time Selection
        Text(
            text = "WHEN?",
            fontSize = 16.sp,
            color = Color(0xFF1A3C34)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dateTime,
                onValueChange = { /* Read-only */ },
                label = { Text("Date and time") },
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = false) { },
                enabled = false
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                    contentDescription = "Select Date",
                    tint = Color(0xFF1A3C34)
                )
            }
            IconButton(onClick = { timePickerDialog.show() }) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_recent_history),
                    contentDescription = "Select Time",
                    tint = Color(0xFF1A3C34)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Seats Available
        Text(
            text = "SEATS AVAILABLE?",
            fontSize = 16.sp,
            color = Color(0xFF1A3C34)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { if (seatsAvailable > 1) seatsAvailable-- }) {
                Text(text = "-", fontSize = 24.sp)
            }
            Text(
                text = seatsAvailable.toString(),
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { seatsAvailable++ }) {
                Text(text = "+", fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Price
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price (e.g., $20)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Car Details
        OutlinedTextField(
            value = carDetails,
            onValueChange = { carDetails = it },
            label = { Text("Car Details (e.g., Toyota Camry)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Publish Button with Validation
        Button(
            onClick = {
                if (fromLatLng == null || toLatLng == null) {
                    Log.w("PublishRide", "Location coordinates are missing")
                    // Optionally show a Snackbar or AlertDialog to inform the user
                    return@Button
                }
                val ride = Ride(
                    driverName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown",
                    fromLocation = fromLocation,
                    fromLat = fromLatLng?.latitude,
                    fromLng = fromLatLng?.longitude,
                    toLocation = toLocation,
                    toLat = toLatLng?.latitude,
                    toLng = toLatLng?.longitude,
                    departureTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(selectedDate),
                    arrivalTime = "", // Could be calculated using Directions API
                    price = price,
                    seatsAvailable = seatsAvailable,
                    date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                    carDetails = carDetails
                )
                db.collection("rides").add(ride)
                    .addOnSuccessListener { onPublishClick() }
                    .addOnFailureListener { Log.w("Firestore", "Error adding ride", it) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C34)),
            enabled = fromLatLng != null && toLatLng != null // Disable if locations are not selected
        ) {
            Text(
                text = "Publish Ride",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}