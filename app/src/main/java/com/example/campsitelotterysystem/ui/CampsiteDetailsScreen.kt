package com.example.campsitelotterysystem.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.User
import com.example.campsitelotterysystem.model.Review
import com.example.campsitelotterysystem.db.DBConnector
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import kotlinx.coroutines.launch

/**
 * Purpose : Displays campsite details, reviews, and links to screen where lotteries are entered.
 * Role : Opened from MainActivity.kt with non-null campsite and opens CampsiteDetailsScreen2.kt by hiding content when variable is set to true
 * Outstanding issues : None, to my knowledge
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    userId: String,
    onBack: () -> Unit,
    onRateCampsite: (Int, String, () -> Unit, (String) -> Unit) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRateDialog by remember { mutableStateOf(false) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    LaunchedEffect(campsite.id) {
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

    BackHandler {
        onBack()
    }
    var nextPage by remember { mutableStateOf<Boolean>(false) }

    if (!nextPage) {

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
                    "Rating: ${
                        String.format(
                            "%.1f",
                            campsite.averageRating
                        )
                    }/5 (${campsite.ratingCount} reviews)"
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

                Button(onClick = { nextPage = true }) {
                    Text("Proceed to booking page")
                }

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
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: Please select a star rating.")
                            }
                        } else {
                            onRateCampsite(
                                rating, reviewText,
                                {
                                    showRateDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Rating saved successfully!")
                                    }
                                },
                                { errorMsg ->
                                    showRateDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Error: $errorMsg")
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    } else {
        CampsiteDetailsScreen2(
            userId = userId,
            campsite = campsite,
                onBack = { nextPage = false })

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
                                tint = if (i <= selectedRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
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
