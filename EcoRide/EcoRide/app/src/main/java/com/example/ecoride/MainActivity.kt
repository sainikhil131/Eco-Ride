package com.example.ecoride

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoride.navigation.BottomNavigationBar
import com.example.ecoride.navigation.Screen
import com.example.ecoride.screens.FindRideScreen
import com.example.ecoride.screens.HomeScreen
import com.example.ecoride.screens.InboxScreen
import com.example.ecoride.screens.LoginScreen
import com.example.ecoride.screens.ProfileScreen
import com.example.ecoride.screens.PublishRideScreen
import com.example.ecoride.screens.RideDetailScreen
import com.example.ecoride.screens.RideListingScreen
import com.example.ecoride.ui.theme.EcoRideTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.razorpay.PaymentResultListener

class MainActivity : ComponentActivity(), PaymentResultListener {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("GoogleSignIn", "Google sign-in failed", e)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            Log.d("Permissions", "Location permission granted")
            setupNavigation()
        } else {
            Log.w("Permissions", "Location permissions denied")
            setupNavigation() // Proceed with basic functionality even without location
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleSignIn", "signInWithCredential:success")
                    setupNavigation()
                } else {
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.exception)
                    setupNavigation() // Fallback to login screen on failure
                }
            }
    }

    private fun setupNavigation() {
        setContent {
            EcoRideTheme {
                val navController = rememberNavController()
                val startDestination = if (firebaseAuth.currentUser != null) Screen.Home.route else Screen.Login.route

                Scaffold(
                    bottomBar = {
                        if (firebaseAuth.currentUser != null) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Login.route) {
                            LoginScreen(
                                onGoogleSignInClick = {
                                    val signInIntent = googleSignInClient.signInIntent
                                    signInLauncher.launch(signInIntent)
                                },
                                onNavigateToSignup = { /* TODO: Implement signup navigation */ },
                                onNavigateToLogin = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Home.route) {
                            HomeScreen(
                                onNavigateToFindRide = {
                                    navController.navigate(Screen.FindRide.route)
                                }
                            )
                        }
                        composable(Screen.FindRide.route) {
                            FindRideScreen(
                                onSearchClick = { params ->
                                    navController.navigate(Screen.RideListing.route)
                                }
                            )
                        }
                        composable(Screen.RideListing.route) {
                            RideListingScreen(
                                onRideClick = { ride ->
                                    navController.navigate(Screen.RideDetail.createRoute(ride))
                                },
                                db = db
                            )
                        }
                        composable(Screen.PublishRide.route) {
                            PublishRideScreen(
                                onPublishClick = {
                                    navController.navigate(Screen.RideListing.route)
                                },
                                db = db
                            )
                        }
                        composable(Screen.RideDetail.route) { backStackEntry ->
                            val rideHash = backStackEntry.arguments?.getString("ride")?.toIntOrNull()
                            RideDetailScreen(
                                rideHash = rideHash,
                                db = db
                            )
                        }
                        composable(Screen.Inbox.route) {
                            InboxScreen(
                                db = db,
                                auth = firebaseAuth
                            )
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                db = db,
                                storage = storage,
                                onLogoutClick = {
                                    firebaseAuth.signOut()
                                    googleSignInClient.signOut()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        // Handled in RideDetailScreen via callback
    }

    override fun onPaymentError(code: Int, response: String?) {
        // Handled in RideDetailScreen via callback
    }
}