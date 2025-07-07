package com.example.ecoride.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object FindRide : Screen("find_ride")
    object RideListing : Screen("ride_listing")
    object PublishRide : Screen("publish_ride")
    object RideDetail : Screen("ride_detail/{ride}") {
        fun createRoute(ride: Ride) = "ride_detail/${ride.hashCode()}"
    }
    object Inbox : Screen("inbox")
    object Profile : Screen("profile")
}