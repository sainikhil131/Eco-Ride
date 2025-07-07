package com.example.ecoride.screens

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import androidx.activity.ComponentActivity
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.text.SimpleDateFormat
import java.util.*
import com.example.ecoride.navigation.RideSearchParams // Add this import

@Composable
fun FindRideScreen(onSearchClick: (RideSearchParams) -> Unit) {
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var fromLatLng by remember { mutableStateOf<LatLng?>(null) }
    var toLatLng by remember { mutableStateOf<LatLng?>(null) }
    var dateTime by remember { mutableStateOf("Today") }
    var seatsNeeded by remember { mutableStateOf(1) }

    // Add state for polyline points
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var selectedDate by remember { mutableStateOf(calendar.time) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = calendar.time
                dateTime = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate)
            },
            year,
            month,
            day
        )
    }

    // Initialize Places API
    if (!Places.isInitialized()) {
        Places.initialize(context, "AIzaSyDy7XwE7BXxp9_8lzCg3VHBGHArmNEWVVs") // Replace with your API key
    }

    // Autocomplete Launcher for From location
    val fromAutocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val place = Autocomplete.getPlaceFromIntent(data)
                fromLocation = place.name ?: ""
                fromLatLng = place.latLng
            }
        }
    )

    // Autocomplete Launcher for To location
    val toAutocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                val place = Autocomplete.getPlaceFromIntent(data)
                toLocation = place.name ?: ""
                toLatLng = place.latLng
            }
        }
    )

    // Map state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.7749, -122.4194), 10f) // Default to San Francisco
    }

    // Update LaunchedEffect to calculate route when both points are available
    LaunchedEffect(fromLatLng, toLatLng) {
        when {
            fromLatLng != null && toLatLng != null -> {
                // Calculate midpoint for better camera positioning
                val midLat = (fromLatLng!!.latitude + toLatLng!!.latitude) / 2
                val midLng = (fromLatLng!!.longitude + toLatLng!!.longitude) / 2
                val midPoint = LatLng(midLat, midLng)

                // Calculate appropriate zoom level based on distance
                val latDiff = Math.abs(fromLatLng!!.latitude - toLatLng!!.latitude)
                val lngDiff = Math.abs(fromLatLng!!.longitude - toLatLng!!.longitude)
                val maxDiff = maxOf(latDiff, lngDiff)
                val zoom = when {
                    maxDiff > 0.5 -> 8f
                    maxDiff > 0.2 -> 10f
                    maxDiff > 0.05 -> 12f
                    else -> 14f
                }

                cameraPositionState.position = CameraPosition.fromLatLngZoom(midPoint, zoom)

                // Simple straight line route for now
                // In a real app, you'd use Directions API to get actual route
                polylinePoints = listOf(fromLatLng!!, toLatLng!!)
            }
            fromLatLng != null -> {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(fromLatLng!!, 12f)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Find a ride",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // From Location with Autocomplete
        Text(text = "WHERE ARE YOU GOING?", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
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

        // Enhanced Map View with route
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            cameraPositionState = cameraPositionState
        ) {
            // From marker with green icon
            fromLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "From",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            // To marker with red icon
            toLatLng?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "To",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )
            }

            // Draw polyline route if both points are selected
            if (polylinePoints.isNotEmpty()) {
                Polyline(
                    points = polylinePoints,
                    color = Color.Blue,
                    width = 5f
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Date Picker
        Text(text = "WHEN?", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dateTime,
                onValueChange = { /* Read-only field */ },
                label = { Text("Date") },
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = false) { /* Disable manual input */ },
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seats Needed
        Text(text = "SEATS NEEDED?", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { if (seatsNeeded > 1) seatsNeeded-- }) {
                Text(text = "-", fontSize = 24.sp)
            }
            Text(
                text = seatsNeeded.toString(),
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            IconButton(onClick = { seatsNeeded++ }) {
                Text(text = "+", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Search Button - modified to pass search parameters
        Button(
            onClick = {
                onSearchClick(
                    RideSearchParams(
                        fromLatLng = fromLatLng,
                        toLatLng = toLatLng,
                        fromLocation = fromLocation,
                        toLocation = toLocation,
                        dateTime = dateTime,
                        seatsNeeded = seatsNeeded
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))
        ) {
            Text(
                text = "Search",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}