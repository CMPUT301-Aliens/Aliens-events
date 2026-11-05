
package com.example.campsitelotterysystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Filter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteSearchScreen(
    controller: CampsiteController,
    onCampsiteClick: (Campsite) -> Unit
) {

    var searchText by remember { mutableStateOf("") }
    var locationFilter by remember { mutableStateOf("") }
    var guestText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }

    var campsites by remember {
        mutableStateOf<List<Campsite>>(emptyList())
    }

    var errorMessage by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    // Convert the date picker UTC date into a local calendar date.
    fun convertPickerDate(millis: Long): Date {
        val utcCalendar = Calendar.getInstance(
            TimeZone.getTimeZone("UTC")
        ).apply {
            timeInMillis = millis
        }

        return Calendar.getInstance().apply {
            set(
                utcCalendar.get(Calendar.YEAR),
                utcCalendar.get(Calendar.MONTH),
                utcCalendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
            )
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    // Load campsites using filters and search keyword.
    fun loadCampsites(
        location: String = locationFilter,
        guests: String = guestText,
        date: Date? = selectedDate,
        keyword: String = searchText
    ) {

        val filter = Filter(
            location = location.trim(),
            date = date,
            numberOfGuests = guests.toIntOrNull()
        )

        controller.filterCampsites(
            filter = filter,
            onSuccess = { results ->

                // TEMPORARY DEBUG LOG: Check image URLs received from Firestore.
                results.forEach { campsite ->
                    android.util.Log.d(
                        "CampsiteImage",
                        "${campsite.name}: ${campsite.promotionalImages}"
                    )
                }

                val searchTerm = keyword.trim()

                campsites = results.filter { campsite ->
                    searchTerm.isBlank() ||
                            campsite.name.contains(
                                searchTerm,
                                ignoreCase = true
                            ) ||
                            campsite.location.contains(
                                searchTerm,
                                ignoreCase = true
                            ) ||
                            campsite.tags.any {
                                it.contains(searchTerm, ignoreCase = true)
                            }
                }

                errorMessage = ""
            },
            onFailure = {
                errorMessage =
                    it.message ?: "Unable to load campsites."
            }
        )
    }

    // Load campsites when screen opens.
    LaunchedEffect(controller) {
        loadCampsites()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Find Campsites",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
                loadCampsites(keyword = newText)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search campsites...")
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchText = ""
                            loadCampsites(keyword = "")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Location filter
        OutlinedTextField(
            value = locationFilter,
            onValueChange = {
                locationFilter = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Location")
            },
            placeholder = {
                Text("Enter city or area")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Number of guests filter
        OutlinedTextField(
            value = guestText,
            onValueChange = { newValue ->
                if (newValue.all { it.isDigit() }) {
                    guestText = newValue
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Number of guests")
            },
            placeholder = {
                Text("Enter number of guests")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Available date filter
        OutlinedButton(
            onClick = {
                showDatePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedDate?.let {
                    "Available date: ${
                        SimpleDateFormat(
                            "MMM dd, yyyy",
                            Locale.getDefault()
                        ).format(it)
                    }"
                } ?: "Select available date"
            )
        }

        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val date = convertPickerDate(it)
                                selectedDate = date
                                loadCampsites(date = date)
                            }

                            showDatePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Apply and clear filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = {
                    loadCampsites()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply Filters")
            }

            OutlinedButton(
                onClick = {
                    locationFilter = ""
                    guestText = ""
                    selectedDate = null
                    searchText = ""

                    loadCampsites(
                        location = "",
                        guests = "",
                        date = null,
                        keyword = ""
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear Filters")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage.isNotEmpty()) {

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error
            )

        } else {

            // Result count
            Text(
                text = "${campsites.size} campsites found",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (campsites.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching campsites found.")
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(campsites) { campsite ->

                        // Clickable campsite card
                        Card(
                            onClick = {
                                onCampsiteClick(campsite)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                // Campsite thumbnail
                                val imageUrl =
                                    campsite.promotionalImages.firstOrNull()

                                if (!imageUrl.isNullOrBlank()) {

                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription =
                                            "${campsite.name} thumbnail",
                                        modifier = Modifier.size(
                                            width = 110.dp,
                                            height = 120.dp
                                        ),
                                        contentScale = ContentScale.Crop
                                    )

                                } else {

                                    Box(
                                        modifier = Modifier
                                            .size(
                                                width = 110.dp,
                                                height = 120.dp
                                            )
                                            .background(
                                                MaterialTheme.colorScheme
                                                    .surfaceVariant
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No image")
                                    }
                                }

                                // Campsite information
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = campsite.name,
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        "Location: ${campsite.location}"
                                    )

                                    Text(
                                        "Maximum guests: ${campsite.maximumOccupancy}"
                                    )

                                    Text(
                                        "Price per night: \$${campsite.pricePerNight}"
                                    )

                                    Text(
                                        "Rating: ${campsite.averageRating}/5"
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = "Description: ${
                                            campsite.description.ifBlank {
                                                "No description available"
                                            }
                                        }"
                                    )

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(
                                        text = "Tags: ${
                                            campsite.tags.joinToString(", ")
                                                .ifBlank { "None" }
                                        }"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
