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
import androidx.compose.material.icons.filled.Menu
import coil.compose.AsyncImage
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Filter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Purpose : Entrant "Find Campsites" screen (US 01.01.01 search, US 01.01.02 filter,
 *           US 01.01.03 summary list). Shows a search bar, filters (location, date range,
 *           number of guests), a result count, and a card for each matching campsite.
 * Role : Loads all campsites once through CampsiteController, then narrows them on the device.
 *        Filters are applied with filterCampsites, then the search text is matched against
 *        name, location and tags. Tapping a card hands that campsite to the caller, which
 *        opens the details screen.
 * Outstanding issues : Dates are typed as yyyy-MM-dd. The tents slider is shown but disabled
 *                      and is not part of the filter. In the menu, only Profile is linked;
 *                      Waitlists and Bookings are not built yet, and Explore / Search just
 *                      closes the menu. Unpublished campsites are not hidden. The
 *                      "TEMPORARY DEBUG LOG" of image URLs should be removed before release.
 *
 */
@Composable
fun CampsiteSearchScreen(
    controller: CampsiteController,
    onCampsiteClick: (Campsite) -> Unit,
    onOpenProfile: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var allCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var applied by remember { mutableStateOf(Filter()) }
    var errorMessage by remember { mutableStateOf("") }

    // Filter inputs (draft values, applied when "Apply filters" is tapped)
    val format = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.CANADA).apply { isLenient = false }
    }
    var location by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var people by remember { mutableFloatStateOf(0f) }
    var filterError by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    fun parse(text: String): Date? =
        if (text.isBlank()) null else runCatching { format.parse(text) }.getOrNull()

    // Load campsites when screen opens.
    LaunchedEffect(controller) {
        controller.getAllCampsites(
            onSuccess = { results ->
                // TEMPORARY DEBUG LOG: Check image URLs received from Firestore.
                results.forEach { campsite ->
                    android.util.Log.d(
                        "CampsiteImage",
                        "${campsite.name}: ${campsite.promotionalImages}"
                    )
                }
                allCampsites = results
                errorMessage = ""
            },
            onFailure = {
                errorMessage = it.message ?: "Unable to load campsites."
            }
        )
    }

    // Filter first, then apply the name/location/tag search on top.
    val searchTerm = searchText.trim()
    val campsites = controller.filterCampsites(allCampsites, applied).filter { campsite ->
        searchTerm.isBlank() ||
                campsite.name.contains(searchTerm, ignoreCase = true) ||
                campsite.location.contains(searchTerm, ignoreCase = true) ||
                campsite.tags.any { it.contains(searchTerm, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title + menu + search bar
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(text = { Text("Explore / Search") }, onClick = { menuExpanded = false })
                            DropdownMenuItem(text = { Text("Waitlists") }, onClick = { menuExpanded = false })
                            DropdownMenuItem(text = { Text("Bookings") }, onClick = { menuExpanded = false })
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = { menuExpanded = false; onOpenProfile() }
                            )
                        }
                    }
                    Text(
                        text = "Find Campsites",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search campsites...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true
                )
            }
        }

        // Filters
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Filters", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        val s = parse(start)
                        val e = parse(end)
                        when {
                            (start.isNotBlank() || end.isNotBlank()) && (s == null || e == null) ->
                                filterError = "Enter both dates as yyyy-MM-dd."
                            s != null && e != null && e.before(s) ->
                                filterError = "End date must not be before start date."
                            else -> {
                                filterError = null
                                applied = Filter(
                                    location.trim(),
                                    if (s != null && e != null) Pair(s, e) else null,
                                    people.toInt()
                                )
                            }
                        }
                    }) { Text("Apply filters") }
                    OutlinedButton(onClick = {
                        location = ""; start = ""; end = ""; people = 0f
                        filterError = null
                        applied = Filter()
                    }) { Text("Reset filters") }
                }
                OutlinedTextField(location, { location = it }, label = { Text("Location") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(start, { start = it }, label = { Text("Start (yyyy-MM-dd)") },
                        singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(end, { end = it }, label = { Text("End (yyyy-MM-dd)") },
                        singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("Select # Guests", style = MaterialTheme.typography.labelLarge)
                Text(if (people.toInt() == 0) "# People: any" else "# People: ${people.toInt()}")
                Slider(value = people, onValueChange = { people = it }, valueRange = 0f..20f, steps = 19)
                Text("# Tents (not in the filter yet)")
                Slider(value = 1f, onValueChange = {}, enabled = false, valueRange = 0f..10f)
                filterError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }

        // Result count / error / empty message
        item {
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Column {
                    Text(
                        text = "${campsites.size} campsites found",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (campsites.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No matching campsites found.")
                    }
                }
            }
        }

        // Result cards
        if (errorMessage.isEmpty()) {
            items(campsites) { campsite ->
                Card(
                    onClick = { onCampsiteClick(campsite) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Campsite thumbnail
                        val imageUrl = campsite.promotionalImages.firstOrNull()
                        if (!imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "${campsite.name} thumbnail",
                                modifier = Modifier.size(width = 110.dp, height = 120.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 120.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No image")
                            }
                        }

                        // Campsite information
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = campsite.name,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Location: ${campsite.location}")
                            Text("Maximum guests: ${campsite.maximumOccupancy}")
                            Text("Price per night: \$${campsite.pricePerNight}")
                            Text("Rating: ${campsite.averageRating}/5")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Description: ${
                                    campsite.description.ifBlank { "No description available" }
                                }"
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tags: ${
                                    campsite.tags.joinToString(", ").ifBlank { "None" }
                                }"
                            )
                        }
                    }
                }
            }
        }
    }
}