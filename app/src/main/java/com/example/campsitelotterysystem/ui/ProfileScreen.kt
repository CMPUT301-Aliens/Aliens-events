package com.example.campsitelotterysystem.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.User

/**
 * Purpose : Entrant "Your Profile & Settings" screen, laid out to match the storyboard.
 *           Edit name, email and phone (US 01.07.03), view the device ID (US 01.09.01),
 *           toggle notifications (US 01.10.01) and delete the profile (US 01.08.01).
 * Role :    Loads the current device's user through UserDB and writes changes back.
 * Outstanding issues : Profile image upload is shown but disabled (not built yet).
 *                      Menu items (Explore / Search, Waitlists, Bookings, Profile) are
 *                      not linked to any screen yet.
 *                      onDeleted is a hook for the caller to leave this screen.(doesn't go anywhere)
 *
 */

@Composable
fun ProfileScreen(
    userDB: UserDB = remember { FirestoreUserDB() },
    onDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val userId = remember { getCurrentUserId(context) }
    var menuExpanded by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun show(text: String, error: Boolean) {
        message = text
        messageIsError = error
    }
    fun loadProfile() {
        userDB.getOrCreateUser(
            userId,
            onSuccess = {
                user = it
                name = it.name
                email = it.email
                phone = it.phoneNumber
                notificationsEnabled = it.notificationsEnabled
            },
            onFailure = { show("Could not load your profile. Check your connection.", true) }
        )
    }
        LaunchedEffect(userId) { loadProfile() }

        Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primaryContainer) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            // Not linked to any screen yet
                            DropdownMenuItem(
                                text = { Text("Explore / Search") },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Waitlists") },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Bookings") },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = { menuExpanded = false }
                            )
                        }
                    }
                    Column {
                        Text("Your Profile & Settings", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Manage your account and preferences",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Image (upload planned for the final part)
            ProfileCard("Profile Image") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    OutlinedButton(
                        onClick = { /* image upload: planned for the final part */ },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    ) { Text("Upload / Change Image") }
                }
            }

            // Personal Information
            ProfileCard("Personal Information") {
                OutlinedTextField(name, { name = it }, label = { Text("Name") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(email, { email = it }, label = { Text("Email") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())

                message?.let {
                    Text(
                        it,
                        color = if (messageIsError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    enabled = user != null,
                    onClick = {
                        val problem = validateProfile(name, email, phone)
                        val current = user
                        if (problem != null) {
                            show(problem, true)
                        } else if (current != null) {
                            val updated = current.copy(
                                name = name.trim(),
                                email = email.trim(),
                                phoneNumber = phone.trim()
                            )
                            userDB.updateProfile(
                                updated,
                                onSuccess = { user = updated; show("Profile saved.", false) },
                                onFailure = { show("Could not save your profile.", true) }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save Changes") }
            }

            // Device ID (read-only)
            ProfileCard("Device ID · Read-only") {
                OutlinedTextField(
                    value = userId, onValueChange = {}, readOnly = true,
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }

            // Preferences
            ProfileCard("Preferences") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Receive Push Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        enabled = user != null,
                        onCheckedChange = { enabled ->
                            notificationsEnabled = enabled
                            userDB.setNotificationPreference(
                                userId, enabled,
                                onSuccess = { show("Notification preference saved.", false) },
                                onFailure = {
                                    notificationsEnabled = !enabled // revert
                                    show("Could not save notification preference.", true)
                                }
                            )
                        }
                    )
                }
            }

            // Danger Zone
            ProfileCard("Danger Zone") {
                Button(
                    enabled = user != null,
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete Profile") }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile?") },
            text = { Text("Are you sure you want to delete your profile?") },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        userDB.deleteProfile(
                            userId,
                            onSuccess = {
                                // Clear the screen, then recreate a default profile for this device
                                user = null
                                name = ""
                                email = ""
                                phone = ""
                                notificationsEnabled = true
                                onDeleted()
                                loadProfile()
                                show("Profile deleted.", false)
                            },
                            onFailure = { show("Could not delete your profile.", true) }
                        )
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete Profile") }
            }
        )
    }
}

/** Keeps a consistent layout for profile cards, making it easier to reuse.  */
@Composable
private fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

/** Returns an error message for invalid profile input, or null if it is valid. */
internal fun validateProfile(name: String, email: String, phone: String): String? = when {
    name.isBlank() -> "Name is required."
    email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
        "Email is not valid."
    phone.isNotBlank() && phone.count { it.isDigit() } < 10 ->
        "Phone number needs at least 10 digits."
    else -> null
}