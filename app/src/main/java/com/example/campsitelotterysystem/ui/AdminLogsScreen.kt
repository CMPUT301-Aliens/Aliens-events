package com.example.campsitelotterysystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.NotificationController
import com.example.campsitelotterysystem.db.FirestoreNotificationDB
import com.example.campsitelotterysystem.model.Notification

/**
 * - fetches the data from the NotificationController.
 * - displays all system notifications in a scrollable list
 */

@Composable
fun AdminLogsScreen(
    controller: NotificationController = remember { NotificationController(FirestoreNotificationDB()) }
) {
    // state variables
    var logs by remember { mutableStateOf<List<Notification>>(emptyList()) } //holds the data
    var isLoading by remember { mutableStateOf(true) } //manages loading spinner
    var errorMessage by remember { mutableStateOf("") } // handles failures

    //get notification logs from database
    LaunchedEffect(Unit) {
        controller.getAllNotificationLogs(
            onSuccess = {
                // Show newest logs at the top
                logs = it.sortedByDescending { notif -> notif.timestamp }
                isLoading = false
            },
            onFailure = {
                errorMessage = it.message ?: "Failed to load notification logs."
                isLoading = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // screen title
        Text("System Notification Logs", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {  // show loading wheel
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {  // show error text
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        } else if (logs.isEmpty()) { // show is-empty message
            Text("No notifications have been logged yet.")
        } else {
            // scrollable list of notification log cards
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Type: ${log.type.uppercase()}", style = MaterialTheme.typography.labelMedium)
                            Text("Time: ${log.timestamp}")
                            Text("Recipients: ${log.recipientIds.joinToString()}")
                            Spacer(modifier = Modifier.height(4.dp))
                            // notification message content
                            Text(log.messageContent)
                        }
                    }
                }
            }
        }
    }
}