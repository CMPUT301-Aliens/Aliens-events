package com.example.campsitelotterysystem.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.controller.UserController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.User
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme

/**
 * handles browsing campsites, browsing user profiles, and deleting profiles.
 * uses a tabbed layout.
 */
class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampsiteLotterySystemTheme {
                // Tracks toggle state between Admin Dashboard and System Logs
                var currentScreen by remember { mutableStateOf("DASHBOARD") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == "DASHBOARD",
                                onClick = { currentScreen = "DASHBOARD" },
                                icon = { Text("🏕️") },
                                label = { Text("Dashboard") }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "LOGS",
                                onClick = { currentScreen = "LOGS" },
                                icon = { Text("📋") },
                                label = { Text("Logs") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            "DASHBOARD" -> AdminDashboardScreen()
                            "LOGS" -> AdminLogsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current

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

    Box(modifier = Modifier.fillMaxSize()) {
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

        // Floating Action Button to open Image Gallery Activity
        ExtendedFloatingActionButton(
            onClick = {
                context.startActivity(Intent(context, ImageGalleryActivity::class.java))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Image Gallery")
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
    // when incremented: launchedEffect re-runs & get current list after deletion
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // get all registered campsites
    LaunchedEffect(refreshTrigger) {
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement
                        .SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (campsite.name.isNotBlank())
                                campsite.name
                            else "Unnamed Campsite",
                            style = MaterialTheme
                                .typography
                                .titleMedium
                        )
                        Text(
                            campsite.location,
                            style = MaterialTheme
                                .typography
                                .bodyMedium
                        )
                        Text("Owner ID: ${campsite.ownerId}", style = MaterialTheme.typography.bodySmall)
                    }
                    // delete button
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            if (campsite.id.isNotBlank()) {
                                campsiteController.removeCampsite(
                                    campsite,
                                    onSuccess = { refreshTrigger++ }, // Reload list automatically
                                    onFailure = { /* Handle error */ }
                                )
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
