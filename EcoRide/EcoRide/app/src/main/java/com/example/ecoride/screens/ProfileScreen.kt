package com.example.ecoride.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

data class UserProfile(
    val username: String = "",
    val userPhotoUrl: String? = null,
    val driverLicensePhotoUrl: String? = null,
    val carRCPhotoUrl: String? = null,
    val permanentAddress: String = ""
)

@Composable
fun ProfileScreen(
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    onLogoutClick: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: return
    val coroutineScope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var permanentAddress by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }

    // Image pickers for user photo, driver's license, and car RC
    val userPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebaseStorage(it, "user_photos/$userId.jpg", storage) { url ->
                db.collection("users").document(userId).update("userPhotoUrl", url)
            }
        }
    }
    val driverLicensePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebaseStorage(it, "driver_licenses/$userId.jpg", storage) { url ->
                db.collection("users").document(userId).update("driverLicensePhotoUrl", url)
            }
        }
    }
    val carRCPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToFirebaseStorage(it, "car_rcs/$userId.jpg", storage) { url ->
                db.collection("users").document(userId).update("carRCPhotoUrl", url)
            }
        }
    }

    // Fetch user profile data from Firestore
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userProfile = document.toObject(UserProfile::class.java)?.copy(username = user.displayName ?: "Unknown")
                        permanentAddress = userProfile?.permanentAddress ?: ""
                    } else {
                        // If the document doesn't exist, create one with default values
                        val newProfile = UserProfile(username = user.displayName ?: "Unknown")
                        db.collection("users").document(userId).set(newProfile)
                        userProfile = newProfile
                        permanentAddress = ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Log.w("Firestore", "Error fetching user profile", it)
                }
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Username
        Text(
            text = "Username: ${userProfile?.username ?: "Unknown"}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // User Photo
        Text(text = "User Photo", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
        userProfile?.userPhotoUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "User Photo",
                modifier = Modifier
                    .size(100.dp)
                    .clickable { if (isEditing) userPhotoPicker.launch("image/*") }
            )
        } ?: run {
            Text(
                text = "No photo uploaded",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { if (isEditing) userPhotoPicker.launch("image/*") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Driver's License Photo
        Text(text = "Driver's License", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
        userProfile?.driverLicensePhotoUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Driver's License Photo",
                modifier = Modifier
                    .size(100.dp)
                    .clickable { if (isEditing) driverLicensePicker.launch("image/*") }
            )
        } ?: run {
            Text(
                text = "No driver's license uploaded",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { if (isEditing) driverLicensePicker.launch("image/*") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Car RC Photo
        Text(text = "Car RC", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
        userProfile?.carRCPhotoUrl?.let { url ->
            Image(
                painter = rememberAsyncImagePainter(url),
                contentDescription = "Car RC Photo",
                modifier = Modifier
                    .size(100.dp)
                    .clickable { if (isEditing) carRCPicker.launch("image/*") }
            )
        } ?: run {
            Text(
                text = "No car RC uploaded",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.clickable { if (isEditing) carRCPicker.launch("image/*") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Permanent Address
        Text(text = "Permanent Address", fontSize = 16.sp, color = Color(0xFF1A3C34))
        Spacer(modifier = Modifier.height(8.dp))
        if (isEditing) {
            OutlinedTextField(
                value = permanentAddress,
                onValueChange = { permanentAddress = it },
                label = { Text("Permanent Address") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = userProfile?.permanentAddress ?: "Not set",
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Edit/Save Button
        Button(
            onClick = {
                if (isEditing) {
                    // Save the updated address to Firestore
                    db.collection("users").document(userId).update("permanentAddress", permanentAddress)
                        .addOnSuccessListener {
                            userProfile = userProfile?.copy(permanentAddress = permanentAddress)
                        }
                        .addOnFailureListener { e -> Log.w("Firestore", "Error updating address", e) }
                }
                isEditing = !isEditing
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C34))
        ) {
            Text(
                text = if (isEditing) "Save" else "Edit Profile",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D4D))
        ) {
            Text(
                text = "Logout",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

// Helper function to upload images to Firebase Storage
fun uploadImageToFirebaseStorage(uri: Uri, path: String, storage: FirebaseStorage, onSuccess: (String) -> Unit) {
    val storageRef: StorageReference = storage.reference.child(path)
    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                onSuccess(downloadUri.toString())
            }
        }
        .addOnFailureListener { e -> Log.w("FirebaseStorage", "Error uploading image", e) }
}