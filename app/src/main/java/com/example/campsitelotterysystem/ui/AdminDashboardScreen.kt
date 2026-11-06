package com.example.campsitelotterysystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.controller.UserController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.User

/**
 * handles browsing campsites, browsing user profiles, and deleting profiles.
 * uses a tabbed layout.
 */

@Composable
fun AdminDashboardScreen() {
    // Initialize dependencies
    val userDB = remember { FirestoreUserDB() }
    val campsiteDB = remember { FirestoreCampsiteDB() }
    val lotteryDB = remember { FirestoreLotteryDB() }
    val campsiteController = remember { CampsiteController(campsiteDB) }
    val lotteryController = remember { LotteryController(lotteryDB, userDB) }
    val userController = remember { UserController(userDB, campsiteDB, lotteryDB, lotteryController, campsiteController) }

    //track tab state
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profiles", "Campsites")

    //screen tabs for navigation
    Column(modifier = Modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> AdminProfilesTab(userController)
            1 -> AdminCampsitesTab(campsiteController)
        }
    }
}
// ------------------------------------------------------------------------------------
//                                    PROFILES TAB
// ------------------------------------------------------------------------------------
@Composable
fun AdminProfilesTab(userController: UserController) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    // when incremented: launchedEffect re-runs & get current list after deletion
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // get all profiles from database
    LaunchedEffect(refreshTrigger) {
        userController.getAllProfiles(
            onSuccess = { users = it },
            onFailure = { /* Handle error */ }
        )
    }

    // Scrollable list for profiles
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement
                        .SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Role: ${user.role.uppercase()}",
                            style = MaterialTheme
                                .typography
                                .labelSmall
                        )
                        Text(
                            if (user.name.isNotBlank())
                                user.name
                            else "Unnamed User",
                            style = MaterialTheme
                                .typography
                                .titleMedium
                        )
                        Text("Device ID: ${user.deviceId}", style = MaterialTheme.typography.bodySmall)
                    }
                    // delete button
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            userController.removeProfile(
                                user,
                                onSuccess = { refreshTrigger++ }, // Reload list automatically
                                onFailure = { /* Show error toast */ }
                            )
                        }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------
//                                    CAMPSITES TAB
// ------------------------------------------------------------------------------------
@Composable
fun AdminCampsitesTab(campsiteController: CampsiteController) {
    var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }

    // get all registered campsites
    LaunchedEffect(Unit) {
        campsiteController.getAllCampsites(
            onSuccess = { campsites = it },
            onFailure = { /* Handle error */ }
        )
    }

    // scrollable list of campsites
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(campsites) { campsite ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        campsite.name,
                        style = MaterialTheme
                            .typography
                            .titleMedium
                    )
                    Text(
                        campsite.location,
                        style = MaterialTheme
                            .typography
                            .bodyMedium)
                    Text(
                        "Owner ID: ${campsite.ownerId}",
                        style = MaterialTheme
                            .typography
                            .bodySmall)
                }
            }
        }
    }
}