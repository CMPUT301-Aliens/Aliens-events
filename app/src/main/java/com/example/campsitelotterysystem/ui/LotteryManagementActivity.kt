package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.model.User
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import java.util.Date
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import com.example.campsitelotterysystem.controller.NotificationController
import com.example.campsitelotterysystem.db.FirestoreNotificationDB
import com.example.campsitelotterysystem.db.getCurrentUserId

/**
 * Purpose: The owner's Waitlist & Winner Management screen (#41, #35, #43, #34).
 * Role: Opened from the lottery details with EXTRA_CAMPSITE_ID. Loads the campsite's
 * lottery, runs the draw if registration has ended, then shows the entrants and winners.
 * The owner can tick entrants and send them a notification.
 * Outstanding issues: The draw only runs when this screen is opened after registration
 * ends. Winners are not notified automatically yet. Reopen the screen to see new entrants.
 */
class LotteryManagementActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CAMPSITE_ID = "campsiteId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val campsiteId = intent.getStringExtra(EXTRA_CAMPSITE_ID)
        setContent {
            CampsiteLotterySystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LotteryManagementScreen(
                        campsiteId = campsiteId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LotteryManagementScreen(campsiteId: String?, modifier: Modifier = Modifier) {
    val lotteryController = remember { LotteryController(FirestoreLotteryDB(), FirestoreUserDB()) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var entrants by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val notificationController = remember { NotificationController(FirestoreNotificationDB()) }
    val context = LocalContext.current
    val ownerId = remember { getCurrentUserId(context) }
    var notificationText by remember { mutableStateOf("") }
    var sendStatus by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    LaunchedEffect(campsiteId) {
        if (campsiteId == null) {
            message = "No campsite was given."
            isLoading = false
            return@LaunchedEffect
        }
        lotteryController.getLotteryForCampsite(
            campsiteId,
            onSuccess = { found ->
                lottery = found
                if (found == null) {
                    message = "This campsite has no lottery yet."
                    isLoading = false
                } else {
                    lotteryController.drawIfReady(
                        found,
                        Date(),
                        onSuccess = { current ->
                            lottery = current
                            lotteryController.getWaitlistEntrants(
                                current,
                                onSuccess = { users ->
                                    entrants = users
                                    isLoading = false
                                },
                                onFailure = { error ->
                                    message = "Could not load the waitlist: ${error.message}"
                                    isLoading = false
                                }
                            )
                        },
                        onFailure = { error ->
                            message = "Could not run the draw: ${error.message}"
                            isLoading = false
                        }
                    )
                }
            },
            onFailure = { error ->
                message = "Could not load the lottery: ${error.message}"
                isLoading = false
            }
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Waitlist & Winner Management", style = MaterialTheme.typography.titleLarge)

        if (isLoading) {
            Text("Loading...")
        }
        if (message.isNotEmpty()) {
            Text(message)
        }

        lottery?.let { current ->
            // #34: show the count out of the capacity, if the owner set one.
            val capacity = current.waitlistCapacity
            if (capacity == null) {
                Text("Total entrants: ${current.waitingList.size} (no limit)")
            } else {
                Text("Total entrants: ${current.waitingList.size} / $capacity")
            }
            if (current.isWaitlistFull()) {
                Text("The waitlist is full, so no one else can join.")
            }
            Text("Winners to draw: ${current.numberOfWinners}")

            if (current.status == "drawn") {
                Text(
                    "Winners",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                val winners = entrants.filter { it.deviceId in current.winners }
                if (!isLoading && winners.isEmpty()) {
                    Text("No one joined the waitlist, so there are no winners.")
                }
                winners.forEach { winner ->
                    val name = winner.name.ifBlank { "No name given" }
                    Text("$name (ID: ${winner.deviceId})")
                }
            } else {
                Text("The draw runs automatically after registration ends on ${formatDateTime(current.registrationEndDate)}.")
            }

            Text(
                "Entrant list",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            if (entrants.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedIds.size == entrants.size,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) entrants.map { it.deviceId }.toSet() else emptySet()
                        }
                    )
                    Text("Select all")
                }
            }
            entrants.forEach { entrant ->
                val name = entrant.name.ifBlank { "No name given" }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = entrant.deviceId in selectedIds,
                        onCheckedChange = { checked ->
                            selectedIds = if (checked) selectedIds + entrant.deviceId else selectedIds - entrant.deviceId
                        }
                    )
                    Text("$name (ID: ${entrant.deviceId})")
                }
            }
            if (!isLoading && entrants.isEmpty()) {
                Text("No entrants have joined the waitlist yet.")
            }

            // #43: the owner sends a notification to the entrants they ticked.
            if (entrants.isNotEmpty()) {
                Text(
                    "Send a notification",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                OutlinedTextField(
                    value = notificationText,
                    onValueChange = { notificationText = it },
                    label = { Text("Message to selected entrants") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    enabled = !isSending,
                    onClick = {
                        val chosenIds = selectedIds.toList()
                        val errors = lotteryController.validateWaitlistMessage(current, notificationText, chosenIds)
                        if (errors.isNotEmpty()) {
                            sendStatus = errors.joinToString("\n")
                        } else {
                            isSending = true
                            sendStatus = ""
                            val notification = lotteryController.buildWaitlistNotification(
                                current, ownerId, chosenIds, notificationText, Date()
                            )
                            val recipients = entrants.filter { it.deviceId in selectedIds }
                            val delivered = recipients.count { it.notificationsEnabled }
                            // sendNotification calls onSuccess once per save: one for each entrant who allows
                            // notifications, one for the owner's sent copy, and one for the admin log.
                            val savesNeeded = delivered + 2
                            var savesDone = 0
                            var failed = false
                            notificationController.sendNotification(
                                recipients,
                                ownerId,
                                notification,
                                onSuccess = {
                                    savesDone++
                                    if (savesDone == savesNeeded && !failed) {
                                        isSending = false
                                        var status = "Notification sent to $delivered entrant(s)."
                                        val optedOut = recipients.size - delivered
                                        if (optedOut > 0) status += " $optedOut turned notifications off."
                                        sendStatus = status
                                        notificationText = ""
                                        selectedIds = emptySet()
                                    }
                                },
                                onFailure = { error ->
                                    if (!failed) {
                                        failed = true
                                        isSending = false
                                        sendStatus = "Could not send the notification: ${error.message}"
                                    }
                                }
                            )
                        }
                    }
                ) {
                    Text(if (isSending) "Sending..." else "Send Notification")
                }
                if (sendStatus.isNotEmpty()) {
                    Text(sendStatus)
                }
            }

        }
    }
}