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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailsScreen(
    campsite: Campsite,
    onBack: () -> Unit
) {

    BackHandler {
        onBack()
    }

    Scaffold(
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
        }
    }
}
