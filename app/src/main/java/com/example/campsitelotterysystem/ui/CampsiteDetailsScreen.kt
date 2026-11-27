package com.example.campsitelotterysystem.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.DBConnector
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.model.Review
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit,
    onRateCampsite: (Int, String, () -> Unit, (String) -> Unit) -> Unit
) {

    BackHandler {
        onBack()
    }

    // Shared UI state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Lottery & waiting list state
    val context = LocalContext.current
    val currentUserId = remember { getCurrentUserId(context) }
    val lotteryController = remember { LotteryController(FirestoreLotteryDB(), FirestoreUserDB()) }

    var activeLottery by remember { mutableStateOf<Lottery?>(null) }
    var isLoadingLottery by remember { mutableStateOf(true) }
    var waitlistMessage by remember { mutableStateOf("") }
    var isUpdatingWaitlist by remember { mutableStateOf(false) }

    // Ratings & reviews state
    var showRateDialog by remember { mutableStateOf(false) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

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

        DBConnector.db.collection("reviews")
            .whereEqualTo("campsiteId", campsite.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                reviews = querySnapshot.toObjects(Review::class.java)
            }
            .addOnFailureListener {
                // Silently ignore or log error
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(campsite.promotionalImages) { imageUrl ->

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "${campsite.name} image",
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
                "Rating: ${String.format("%.1f", campsite.averageRating)}/5 (${campsite.ratingCount} reviews)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showRateDialog = true }) {
                Text("Rate Campsite")
            }

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
                    Text(
                        "Entrants on waitlist: ${lottery.waitingList.size}" +
                                (lottery.waitlistCapacity?.let { " / $it" } ?: "")
                    )

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

            // -------------------------------------------------------------------------------------
            //                                reviews section
            // -------------------------------------------------------------------------------------
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Reviews",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (reviews.isEmpty()) {
                Text("No reviews yet.", style = MaterialTheme.typography.bodyMedium)
            } else {
                reviews.forEach { review ->
                    ReviewItem(review)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        if (showRateDialog) {
            RateCampsiteDialog(
                onDismiss = { showRateDialog = false },
                onSubmit = { rating, reviewText ->
                    if (rating == 0) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error: Please select a star rating.")
                        }
                    } else {
                        onRateCampsite(
                            rating,
                            reviewText,
                            {
                                showRateDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Rating saved successfully!")
                                }
                            },
                            { errorMsg ->
                                showRateDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error: $errorMsg")
                                }
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun RateCampsiteDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Leave a Rating", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = {
                            selectedRating = i
                            showError = false
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$i Star",
                                tint = if (i <= selectedRating) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (showError) {
                    Text(
                        text = "Error: Please select a star rating.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Add a comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedRating == 0) {
                            showError = true
                        } else {
                            onSubmit(selectedRating, reviewText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Rating")
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (review.reviewerProfileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = review.reviewerProfileImageUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = review.reviewerName.ifBlank { "Anonymous" },
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = review.rating.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            if (review.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}