package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.MessageController
import com.example.campsitelotterysystem.db.FirestoreMessageDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Message
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Purpose: Direct messaging screen between an entrant and a campsite owner (US 01.14.01, US 02.12.01)
 * Role: Displays the conversation header, timestamped messages for a reservation, and a compose bar
 */
class MessageActivity: ComponentActivity(){

    // get values from the screen that this screen is opened from
    companion object{
        const val EXTRA_RESERVATION_ID = "reservationId"
        const val EXTRA_RECIPIENT_ID = "recipientId"
        const val EXTRA_CURRENT_USER_ID = "currentUserId"
        const val EXTRA_HEADER_TITLE = "headerTitle"
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val reservationId = intent.getStringExtra(EXTRA_RESERVATION_ID) ?: ""
        val recipientId = intent.getStringExtra(EXTRA_RECIPIENT_ID) ?: ""
        val currentUserId = intent.getStringExtra(EXTRA_CURRENT_USER_ID) ?: getCurrentUserId(this)
        val headerTitle = intent.getStringExtra(EXTRA_HEADER_TITLE) ?: "Reservation Chat"

        setContent{
            CampsiteLotterySystemTheme{
                MessageScreen(
                    reservationId = reservationId,
                    currentUserId = currentUserId,
                    recipientId = recipientId,
                    headerTitle = headerTitle,
                    onBack = {finish()}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    reservationId: String,
    currentUserId: String,
    recipientId: String,
    headerTitle: String = "Reservation Chat",
    messageController: MessageController = remember { MessageController(FirestoreMessageDB()) },
    onBack: () -> Unit = {}
){
    //------------------------  State Variables  --------------------

    val context = LocalContext.current
    // who is currently logged in
    val activeUserId = remember(currentUserId) {
        currentUserId.ifBlank { getCurrentUserId(context) }
    }
    // list of chat messages fetched from the database.
    var messages by remember{ mutableStateOf<List<Message>>(emptyList()) }
    // track what the user is currently typing
    var draftText by remember{ mutableStateOf("") }
    // waiting for messages to download
    var isLoading by remember{ mutableStateOf(true) }
    // disable the send button until message is sent
    var isSending by remember{ mutableStateOf(false) }
    // for confirmation/error message
    var statusMessage by remember{ mutableStateOf("") }
    var isError by remember{ mutableStateOf(false) }

    // make the date readable
    val dateFormatter = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    }

    // get all messages with this reservationId from the DB
    fun loadThread() {
        // if unsuccessful: turn on isError, displays error message
        if (reservationId.isBlank()) {
            isError = true
            statusMessage = "Missing reservation ID."
            isLoading = false
            return
        }
        // if successful: save them into messages
        messageController.getMessagesForReservation(
            reservationId = reservationId,
            onSuccess = { loaded ->
                messages = loaded
                isLoading = false
            },
            onFailure = { err ->
                isError = true
                statusMessage = err.message ?: "Failed to load messages."
                isLoading = false
            }
        )
    }

    LaunchedEffect(reservationId) {
        loadThread()
    }

    Scaffold(
        // -------------------------------------- HEADER ---------------------------------------
        topBar = {
            TopAppBar(
                title = {
                    // main chat title, reservation ID and recipient
                    Column {
                        Text(headerTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Reservation: ${reservationId.ifBlank { "N/A" }} • To: ${recipientId.ifBlank { "Unknown" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                // back button
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        } // end topBar
    ){ innerPadding ->
        // --------------------------------------  MAIN BODY ---------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // -------------------------------------------------------------------------------------
            //                                conversation thread
            // -------------------------------------------------------------------------------------
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    messages.isEmpty() -> {
                        Text(
                            text = "No messages yet. Send a message below to start the conversation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // draw the conversation thread
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ){
                            items(messages){ msg ->
                                val isSentByMe = msg.senderId == activeUserId
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
                                ){
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = if (isSentByMe) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.widthIn(max = 280.dp)
                                    ){
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            // sender
                                            Text(
                                                text = if (isSentByMe) "You" else msg.senderId,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))

                                            // message content
                                            Text(
                                                text = msg.content,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))

                                            // date sent
                                            Text(
                                                text = dateFormatter.format(msg.timestamp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } // end inner column
                                    }
                                } // end mid column
                            }
                        } // end lazy column
                    }
                } // end when
            } // end convo thread BOX

            // Sent confirmation/error feedback (US 02.12.01)
            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusMessage,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // -------------------------------------------------------------------------------------
            //                                    Compose bar
            // -------------------------------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ){
                // -------------- message bar
                OutlinedTextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    placeholder = { Text("Write a message...") },
                    modifier = Modifier.weight(1f),
                    enabled = !isSending
                )
                // -------------- send button
                Button(
                    enabled = !isSending && draftText.isNotBlank(),
                    onClick = {
                        val contentToSend = draftText
                        isSending = true
                        statusMessage = ""
                        isError = false

                        messageController.sendMessage(
                            senderId = activeUserId,
                            recipientId = recipientId,
                            reservationId = reservationId,
                            content = contentToSend,
                            onSuccess = {
                                draftText = ""
                                isSending = false
                                isError = false
                                statusMessage = "Message sent."
                                loadThread()
                            },
                            onInvalid = { validationMsg ->
                                isSending = false
                                isError = true
                                statusMessage = validationMsg
                            },
                            onFailure = { err ->
                                isSending = false
                                isError = true
                                statusMessage = err.message ?: "Failed to send message."
                            }
                        )
                    }
                ){
                    Text(if (isSending) "Sending..." else "Send")
                }
            }
        } // end column
    } // end scaffold
} // end MessageScreen()