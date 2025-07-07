package com.example.ecoride.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoride.navigation.Ride
import com.example.ecoride.navigation.RideSearchParams // Add this import
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@Composable
fun RideListingScreen(
    onRideClick: (Ride) -> Unit,
    db: FirebaseFirestore,
    searchParams: RideSearchParams? = null
) {
    val rides = remember { mutableStateListOf<Ride>() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchParams) {
        coroutineScope.launch {
            isLoading = true

            try {
                // Start with base query - properly handle the types
                val queryRef = db.collection("rides")
                var query: Query = queryRef

                // Apply filters if search parameters exist
                searchParams?.let { params ->
                    if (params.fromLocation.isNotEmpty()) {
                        query = query.whereEqualTo("fromLocation", params.fromLocation)
                    }

                    if (params.toLocation.isNotEmpty()) {
                        query = query.whereEqualTo("toLocation", params.toLocation)
                    }

                    if (params.dateTime != "Today") {
                        query = query.whereEqualTo("date", params.dateTime)
                    }

                    // Filter by available seats
                    query = query.whereGreaterThanOrEqualTo("seatsAvailable", params.seatsNeeded)
                }

                // Execute the query
                query.get()
                    .addOnSuccessListener { result ->
                        rides.clear()
                        for (document in result) {
                            val ride = document.toObject(Ride::class.java).copy(id = document.id)
                            rides.add(ride)
                        }
                        isLoading = false
                    }
                    .addOnFailureListener { exception ->
                        errorMessage = "Error loading rides: ${exception.message}"
                        isLoading = false
                    }
            } catch (e: Exception) {
                errorMessage = "Error with query: ${e.message}"
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Rides",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            rides.isEmpty() -> {
                Text(
                    text = "No rides available matching your criteria",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            else -> {
                LazyColumn {
                    items(rides) { ride ->
                        RideItem(ride = ride, onClick = { onRideClick(ride) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Rest of the file remains the same...
@Composable
fun RideItem(ride: Ride, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = ride.departureTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ride.fromLocation,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ride.arrivalTime,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ride.toLocation,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Driver: ${ride.driverName}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = ride.price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3C34)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${ride.seatsAvailable} seats",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}