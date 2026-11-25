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
import com.example.campsitelotterysystem.model.Campsite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit,
    onRateCampsite: (Int, String, () -> Unit, (String) -> Unit) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showRateDialog by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
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
                        onRateCampsite(rating, reviewText,
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
