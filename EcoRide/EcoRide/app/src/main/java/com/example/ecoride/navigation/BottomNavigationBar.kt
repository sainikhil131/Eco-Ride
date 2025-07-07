package com.example.ecoride.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ecoride.R
import java.util.Locale

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.FindRide,
        Screen.RideListing,
        Screen.PublishRide,
        Screen.Inbox,
        Screen.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    // Map each screen to its corresponding drawable icon
                    val iconResId = when (screen) {
                        Screen.FindRide -> R.drawable.ic_search
                        Screen.RideListing -> R.drawable.ic_ride
                        Screen.PublishRide -> R.drawable.ic_publish
                        Screen.Inbox -> R.drawable.ic_inbox
                        Screen.Profile -> R.drawable.ic_profile
                        else -> R.drawable.ic_ride // Fallback icon
                    }
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = screen.route
                    )
                },
                label = { Text(screen.route.replace("_", " ").capitalize(Locale.ROOT)) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}