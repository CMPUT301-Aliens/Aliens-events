package com.example.campsitelotterysystem.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

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
fun AdminDashboardScreen(modifier: Modifier = Modifier) {
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, ImageGalleryActivity::class.java))
                }
            ) {
                Text("Image Gallery")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
                0 -> AdminProfilesTab(userController, snackbarHostState)
                1 -> AdminCampsitesTab(campsiteController, snackbarHostState)
            }
        }
    }
}
// ------------------------------------------------------------------------------------
//                                    PROFILES TAB
// ------------------------------------------------------------------------------------
@Composable
fun AdminProfilesTab(
    userController: UserController,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        var users by remember { mutableStateOf<List<User>>(emptyList()) }
        // when incremented: launchedEffect re-runs & get current list after deletion
        var refreshTrigger by remember { mutableIntStateOf(0) }

        var searchQuery by remember { mutableStateOf("") }
        var userToDelete by remember { mutableStateOf<User?>(null) }
        var selectedUser by remember { mutableStateOf<User?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // get all profiles from database
        LaunchedEffect(refreshTrigger) {
            userController.getAllProfiles(
                onSuccess = { users = it },
                onFailure = { error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to load profiles: ${error.message}")
                    }
                }
            )
        }

        // Filter logic for search bar
        val query = searchQuery.trim() // clear leading or trailing spaces in search term
        val filteredUsers = users.filter { user ->
            query.isBlank() ||
                    user.name.orEmpty().contains(query, ignoreCase = true) ||
                    user.deviceId.orEmpty().contains(query, ignoreCase = true)
        }  // find users, make list

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)){
            // Search Bar (US 03.05.01)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by Name or ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // if empty display no profiles text
            if (filteredUsers.isEmpty()) {
                Text(
                    text = if (users.isEmpty()) "No profiles found." else "No matching profiles.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else{
                // Scrollable list for profiles
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUsers) { user ->
                        Card(
                            modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)
                                    .clickable { selectedUser = user }) { // Click to view details
                                    Text("Role: ${user.role.uppercase()}", style = MaterialTheme.typography.labelSmall)
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
                                    onClick = {userToDelete = user } // Triggers the popup
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
        //select a profile popup
        if (selectedUser != null) {
            AlertDialog(
                onDismissRequest = { selectedUser = null },
                title = { Text("Profile Details") },
                text = {
                    Column {
                        Text("Name: ${selectedUser!!.name}")
                        Text("Role: ${selectedUser!!.role.uppercase()}")
                        Text("Device ID: ${selectedUser!!.deviceId}")
                        Text("Email: ${selectedUser!!.email}")
                        Text("Phone: ${selectedUser!!.phoneNumber}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedUser = null }) { Text("Close") }
                }
            )
        }

        //delete confirmation popup
        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to permanently remove this profile? This will delete them from all waitlists and remove their campsites.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            val user = userToDelete
                            userToDelete = null // Close popup
                            if (user != null) {
                                userController.removeProfile(
                                    user = user,
                                    onSuccess = {
                                        refreshTrigger++
                                        // Show temporary success banner
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Profile successfully deleted")
                                        }
                                    },
                                    onFailure = { error ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Delete failed: ${error.message}")
                                        }
                                    }
                                )
                            }
                        }
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) { Text("Cancel") }
                }
            )
        }

    }
}
// ------------------------------------------------------------------------------------
//                                    CAMPSITES TAB
// ------------------------------------------------------------------------------------
@Composable
fun AdminCampsitesTab(
    campsiteController: CampsiteController,
    snackbarHostState: SnackbarHostState
) {
    Box(modifier = Modifier.fillMaxSize()){
        var campsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
        // when incremented: launchedEffect re-runs & get current list after deletion
        var refreshTrigger by remember { mutableIntStateOf(0) }
        var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }
        var campsiteToDelete by remember { mutableStateOf<Campsite?>(null) }
        val coroutineScope = rememberCoroutineScope()

        // get all registered campsites
        LaunchedEffect(refreshTrigger) {
            campsiteController.getAllCampsites(
                onSuccess = { campsites = it },
                onFailure = { error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to load campsites: ${error.message}")
                    }
                }
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // if there are no campsites
            if (campsites.isEmpty()) {
                Text(
                    text = "No campsites are currently registered in the system.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                // scrollable list of campsites
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(campsites) { campsite ->
                        Card(
                            modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement
                                    .SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)
                                    .clickable { selectedCampsite = campsite }) {  // able to click campsite
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
                                    Text(
                                        "Owner ID: ${campsite.ownerId}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                // delete button
                                Button(
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    onClick = {campsiteToDelete = campsite } // Triggers the popup
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
        // select a campsite popup
        if (selectedCampsite != null) {
            AlertDialog(
                onDismissRequest = { selectedCampsite = null },
                title = { Text(if (selectedCampsite!!.name.isNotBlank()) selectedCampsite!!.name else "Campsite Details") },
                text = {
                    Column {
                        Text("Location: ${selectedCampsite!!.location}")
                        Text("Max Occupancy: ${selectedCampsite!!.maximumOccupancy}")
                        Text("Price: $${selectedCampsite!!.pricePerNight}/night")
                        Text("Owner ID: ${selectedCampsite!!.ownerId}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedCampsite = null }) { Text("Close") }
                }
            )
        }
        //delete confirmation popup
        if (campsiteToDelete != null) {
            AlertDialog(
                onDismissRequest = { campsiteToDelete = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to permanently remove this campsite? Its active lotteries will be cancelled.") },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            val campsite = campsiteToDelete
                            campsiteToDelete = null // Close popup
                            if (campsite != null && campsite.id.isNotBlank()) {
                                campsiteController.removeCampsite(
                                    campsite = campsite,
                                    onSuccess = {
                                        refreshTrigger++
                                        // Show temporary success banner
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Campsite successfully deleted")
                                        }
                                    },
                                    onFailure = { error ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Delete failed: ${error.message}")
                                        }
                                    }
                                )
                            }
                        }
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    TextButton(onClick = { campsiteToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

