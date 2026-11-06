package com.example.campsitelotterysystem.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Lottery
import okhttp3.internal.wait

//Following function is adapted from Anthropic, Claude
//(MS PAINT DIAGRAM OF REQUESTED UI TEMPLATE)
//Have list of kotlin class i want to display in this form, where blue is an image, yellow is a button, and all other colours are text. Need to display directly under other part of composible function
//5/11/2026
@Composable
fun WaitlistRow(
    waitlist: Lottery,
    onButtonClick: (Lottery) -> Unit,
    userId : String,
    campsiteController: CampsiteController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            var primaryCampsite : Campsite = Campsite() //
                campsiteController.getCampsite(waitlist.campsiteIds[0],
                onSuccess = {campsite -> primaryCampsite = campsite},
                    onFailure = {}
            )

            // Top section: image + three text lines
            Row(verticalAlignment = Alignment.Top) {
                AsyncImage(                       // Coil; swap for Image(painterResource(...)) if local
                    model = primaryCampsite.promotionalImages[0],
                    contentDescription = primaryCampsite.description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )


                Spacer(Modifier.width(12.dp))


                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = primaryCampsite.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = primaryCampsite.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = waitlist.registrationStartDate.toString() + " - " + waitlist.registrationEndDate.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Bottom section: label left, button right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
            var statusText : String = ""
            var buttonText : String = ""
            if (waitlist.status == "open") {statusText = "Open"; buttonText = "Leave"}
            else if (userId in waitlist.winners ) {statusText = "Won"; buttonText = "Book"}
            else {statusText = "Lost"}
                Text(
                    text = "",
                    style = MaterialTheme.typography.labelLarge
                )
                if (buttonText != "") { //if lottery is lost there is no action to be taken
                Button(onClick = { onButtonClick(waitlist) }) {

                    Text(buttonText)}
                }
            }
        }
    }
}

fun handleButtonPress(waitlist : Lottery, userId: String, campsiteController: CampsiteController, lotteryController: LotteryController) {
    if (waitlist.status == "open") {
        //remove user from waitlist

    }
    else {
        //move to lottery win page
    }

}

@Composable
fun ActiveWaitListActivity(
    controller: LotteryController,
    userId : String,
    campsiteController: CampsiteController
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var waitlists by remember { mutableStateOf<List<Lottery>>(emptyList())}
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        controller.getLotteriesWithUserInWaitlist(
            userId,
            onSuccess = { lotteries ->
                waitlists = lotteries
                isLoading = false
            },
            onFailure = { error ->
                errorMessage = "Couldn't load your waitlists. Please try again."
                isLoading = false
            }
        )
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
                            DropdownMenuItem(
                                text = { Text("Explore / Search") },
                                onClick = { menuExpanded = false })
                            DropdownMenuItem(
                                text = { Text("Waitlists") },
                                onClick = { menuExpanded = false })
                            DropdownMenuItem(
                                text = { Text("Bookings") },
                                onClick = { menuExpanded = false })
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = { menuExpanded = false; onOpenProfile() }
                            )
                        }
                    }
                    Text(
                        text = "Active waiting lists",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

            }

        }

    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(waitlists, key = { it.id }) { item ->
            WaitlistRow(waitlist = item, onButtonClick = { /* handle click */ }, userId,
            )
        }

    }
}