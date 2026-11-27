package com.example.campsitelotterysystem.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campsitelotterysystem.model.Campsite
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Lottery
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit
) {

    BackHandler {
        onBack()
    }

    // state variables for fetching the campsite's active lottery and tracking the current user
    val context = LocalContext.current
    val currentUserId = remember { getCurrentUserId(context) }
    val lotteryController = remember { LotteryController(FirestoreLotteryDB(), FirestoreUserDB()) }

    var activeLottery by remember { mutableStateOf<Lottery?>(null) }
    var isLoadingLottery by remember { mutableStateOf(true) }
    var waitlistMessage by remember { mutableStateOf("") }
    var isUpdatingWaitlist by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(campsite.id) {
        if (campsite.id.isNotBlank()) {
            lotteryController.getLotteryForCampsite(
                campsiteId = campsite.id,
                onSuccess = { lottery ->
                    activeLottery = lottery
                    isLoadingLottery = false
                },
                onFailure = {
                    waitlistMessage = "Could not load lottery info."
                    isLoadingLottery = false
                }
            )
        } else {
            isLoadingLottery = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Campsite Details")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Campsite images
            if (campsite.promotionalImages.isNotEmpty()) {

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(campsite.promotionalImages) { imageUrl ->

                        AsyncImage(
                            model = imageUrl,
                            contentDescription =
                                "${campsite.name} image",
                            modifier = Modifier
                                .width(280.dp)
                                .height(190.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

            } else {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No campsite images available")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = campsite.name,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Location: ${campsite.location}")

            Text(
                "Maximum guests: ${campsite.maximumOccupancy}"
            )

            Text(
                "Price per night: \$${campsite.pricePerNight}"
            )

            Text(
                "Rating: ${campsite.averageRating}/5"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                campsite.description.ifBlank {
                    "No description available."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Rules and Policies",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                campsite.rulesAndPolicy.ifBlank {
                    "No rules or policies provided."
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tags",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                campsite.tags.joinToString(", ").ifBlank {
                    "No tags available."
                }
            )
            // -------------------------------------------------------------------------------------
            //                                waitlist section
            // -------------------------------------------------------------------------------------
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Lottery & Waiting List",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            when {
                isLoadingLottery -> {
                    Text("Checking for active lottery...")
                }
                activeLottery == null -> {
                    Text("No active lottery for this campsite right now.")
                }
                else -> {
                    val lottery = activeLottery!!
                    val isJoined = lottery.waitingList.contains(currentUserId)
                    val isFull = lottery.waitlistCapacity != null &&
                            lottery.waitingList.size >= lottery.waitlistCapacity
                    val isOpen = lottery.isRegistrationOpen(Date())

                    Text("Registration closes: ${formatDateTime(lottery.registrationEndDate)}")
                    Text("Entrants on waitlist: ${lottery.waitingList.size}" +
                            (lottery.waitlistCapacity?.let { " / $it" } ?: ""))

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isJoined) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUpdatingWaitlist,
                            onClick = {
                                isUpdatingWaitlist = true
                                lotteryController.leaveWaitingList(
                                    userId = currentUserId,
                                    lottery = lottery,
                                    onSuccess = { updated ->
                                        activeLottery = updated
                                        waitlistMessage = "You left the waiting list."
                                        isUpdatingWaitlist = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Left the waiting list.")
                                        }
                                    },
                                    onFailure = {
                                        waitlistMessage = "Failed to leave waiting list."
                                        isUpdatingWaitlist = false
                                    }
                                )
                            }
                        ) {
                            Text("Leave Waiting List")
                        }
                    } else {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isOpen && !isFull && !isUpdatingWaitlist,
                            onClick = {
                                isUpdatingWaitlist = true
                                lotteryController.joinWaitingList(
                                    userId = currentUserId,
                                    lottery = lottery,
                                    onSuccess = { updated ->
                                        activeLottery = updated
                                        waitlistMessage = "Joined waiting list!"
                                        isUpdatingWaitlist = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Successfully joined the lottery!")
                                        }
                                    },
                                    onInvalid = { reason ->
                                        waitlistMessage = reason
                                        isUpdatingWaitlist = false
                                    },
                                    onFailure = { err ->
                                        waitlistMessage = err.message ?: "Could not join waiting list."
                                        isUpdatingWaitlist = false
                                    }
                                )
                            }
                        ) {
                            Text(
                                when {
                                    !isOpen -> "Registration Closed"
                                    isFull -> "Waitlist Full"
                                    else -> "Join Waiting List"
                                }
                            )
                        }
                    }
                }
            }

            if (waitlistMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = waitlistMessage,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
