package com.example.ecoride.navigation

import com.google.android.gms.maps.model.LatLng

data class RideSearchParams(
    val fromLatLng: LatLng? = null,
    val toLatLng: LatLng? = null,
    val fromLocation: String = "",
    val toLocation: String = "",
    val dateTime: String = "Today",
    val seatsNeeded: Int = 1
)

data class Ride(
    val id: String = "",
    val driverName: String = "",
    val fromLocation: String = "",
    val fromLat: Double? = null,
    val fromLng: Double? = null,
    val toLocation: String = "",
    val toLat: Double? = null,
    val toLng: Double? = null,
    val departureTime: String = "",
    val arrivalTime: String = "",
    val price: String = "",
    val seatsAvailable: Int = 0,
    val date: String = "",
    val carDetails: String = "",
    val paymentStatus: String = "PENDING",
    val paymentId: String? = null
)