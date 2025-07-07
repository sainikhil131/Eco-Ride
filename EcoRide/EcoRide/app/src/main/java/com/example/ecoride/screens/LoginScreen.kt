package com.example.ecoride.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoride.R

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "EcoRide Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "EcoRide",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your ride, Your choice",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
        ) {
            Text(
                text = "Continue with Google",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onNavigateToSignup,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Sign up with Email",
                color = Color.Black,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) {
            Text(
                text = "Already have an account? Log in",
                color = Color(0xFF4285F4),
                fontSize = 14.sp
            )
        }
    }
}