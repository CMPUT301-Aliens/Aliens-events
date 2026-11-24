package com.example.campsitelotterysystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.MessageController
import com.example.campsitelotterysystem.controller.NotificationController
import com.example.campsitelotterysystem.db.FirestoreMessageDB
import com.example.campsitelotterysystem.db.FirestoreNotificationDB
import com.example.campsitelotterysystem.model.Message
import com.example.campsitelotterysystem.model.Notification

/**
 * Fetches notification and message logs from their respective controllers
 * and displays them in a tabbed admin audit view.
 */
@Composable
fun AdminLogsScreen(
    controller: NotificationController = remember { NotificationController(FirestoreNotificationDB()) },
    messageController: MessageController = remember { MessageController(FirestoreMessageDB()) }
) {
    // Tab state: 0 = Notifications, 1 = Messages
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Notifications", "Messages")

    // Notification logs state
    var notificationLogs by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoadingNotifications by remember { mutableStateOf(true) }
    var notificationError by remember { mutableStateOf("") }

    // Message logs state
    var messageLogs by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isLoadingMessages by remember { mutableStateOf(true) }
    var messageError by remember { mutableStateOf("") }

    // Optional search filter for Sender / Recipient / Reservation ID
    var searchQuery by remember { mutableStateOf("") }

    // for viewing details of a notification
    var selectedMessage by remember { mutableStateOf<Message?>(null) }

    // -----------------------------------------------------------------------------
    //                                fetching data
    // -----------------------------------------------------------------------------

    // get notification logs from database
    LaunchedEffect(Unit) {
        controller.getAllNotificationLogs(
            onSuccess = {
                notificationLogs = it.sortedByDescending { notif -> notif.timestamp }
                isLoadingNotifications = false
            },
            onFailure = {
                notificationError = it.message ?: "Failed to load notification logs."
                isLoadingNotifications = false
            }
        )
    }

    // get message logs from database
    LaunchedEffect(Unit) {
        messageController.getAllMessageLogs(
            onSuccess = {
                messageLogs = it.sortedByDescending { msg -> msg.timestamp }
                isLoadingMessages = false
            },
            onFailure = {
                messageError = it.message ?: "Failed to load message logs."
                isLoadingMessages = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("System Audit Logs", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // tab switching logic
        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {selectedTabIndex = index},
                    text = {Text(title)}
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // -------------------------------  search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Filter by Sender, Recipient, or Content") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))


        // -----------------------------------------------------------------------------
        //                                  page content
        // -----------------------------------------------------------------------------

        when (selectedTabIndex) {
            0 -> {
                val filteredNotifications = notificationLogs.filter { notif ->
                    searchQuery.isBlank() ||
                            notif.senderId.contains(searchQuery, ignoreCase = true) ||
                            notif.recipientIds.any { it.contains(searchQuery, ignoreCase = true) } ||
                            notif.messageContent.contains(searchQuery, ignoreCase = true) ||
                            notif.type.contains(searchQuery, ignoreCase = true)
                }
                when {
                    isLoadingNotifications -> CircularProgressIndicator()
                    notificationError.isNotEmpty() -> Text(notificationError, color = MaterialTheme.colorScheme.error)
                    filteredNotifications.isEmpty() -> Text("No notifications match your view.")
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredNotifications) { log ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Type: ${log.type.uppercase()}", style = MaterialTheme.typography.labelMedium)
                                        Text("Time: ${log.timestamp}")
                                        Text("Sender: ${log.senderId}")
                                        Text("Recipients: ${log.recipientIds.joinToString()}")
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(log.messageContent)
                                    }
                                } // end card
                            }
                        } // end lazy column
                    }
                }
            } // end notifications

            1 -> {
                val filteredMessages = messageLogs.filter { msg ->
                    searchQuery.isBlank() ||
                            msg.senderId.contains(searchQuery, ignoreCase = true) ||
                            msg.recipientId.contains(searchQuery, ignoreCase = true) ||
                            msg.reservationId.contains(searchQuery, ignoreCase = true) ||
                            msg.content.contains(searchQuery, ignoreCase = true)
                }

                when {
                    isLoadingMessages -> CircularProgressIndicator()
                    messageError.isNotEmpty() -> Text(messageError, color = MaterialTheme.colorScheme.error)
                    filteredMessages.isEmpty() -> Text(
                        if (messageLogs.isEmpty()) "No messages have been logged yet."
                        else "No messages match your search."
                    )
                    else -> {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredMessages) { msg ->
                                Card(
                                    onClick = { selectedMessage = msg },
                                    modifier = Modifier.fillMaxWidth())
                                {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "Reservation ID: ${msg.reservationId}",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text("Time: ${msg.timestamp}")
                                        Text("From: ${msg.senderId}")
                                        Text("To: ${msg.recipientId}")
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(msg.content)
                                    }
                                } // end card
                            }
                        } // end lazy column
                    }
                }
            } // end messages
        } // end when

        selectedMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { selectedMessage = null },
                title = { Text("Message Details") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Message ID: ${msg.id}")
                        Text("Reservation ID: ${msg.reservationId}")
                        Text("Sender ID: ${msg.senderId}")
                        Text("Recipient ID: ${msg.recipientId}")
                        Text("Date Sent: ${msg.timestamp}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(msg.content)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedMessage = null }) {
                        Text("Close")
                    }
                }
            )
        } // end selected message popup
    } // end column
} // end fun AdminLogsScreen()