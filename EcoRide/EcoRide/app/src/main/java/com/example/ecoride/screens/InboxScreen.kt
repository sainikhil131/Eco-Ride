package com.example.ecoride.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val recipientId: String = "",
    val rideId: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

@Composable
fun InboxScreen(
    db: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val messages = remember { mutableStateListOf<Message>() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    val currentUserId = auth.currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            db.collection("messages")
                .whereEqualTo("recipientId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    messages.clear()
                    for (document in result) {
                        val message = document.toObject(Message::class.java).copy(id = document.id)
                        messages.add(message)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
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
            text = "Inbox",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (messages.isEmpty()) {
            Text(
                text = "No messages yet",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn {
                items(messages) { message ->
                    MessageItem(message, db)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageItem(
    message: Message,
    db: FirebaseFirestore
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isRead) Color.White else Color(0xFFF0F0F0)
        ),
        onClick = {
            if (!message.isRead) {
                coroutineScope.launch {
                    db.collection("messages").document(message.id)
                        .update("isRead", true)
                }
            }
            // Navigate to message detail or ride detail screen
            // navController.navigate("messageDetail/${message.id}")
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "From: ${message.senderName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Regarding Ride: ${message.rideId}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.message,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    .format(Date(message.timestamp)),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}