package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.LotteryController
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.model.User
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme

/**
 * Purpose: The owner's Waitlist & Winner Management screen (#41, and later #35).
 * Role: Opened from the lottery details with EXTRA_CAMPSITE_ID. Loads the campsite's
 * lottery and its waitlist entrants through LotteryController.
 * Outstanding issues: The draw (#35) is not added yet. The list loads when the screen
 * opens, so reopen the screen to see new entrants.
 */
class LotteryManagementActivity : ComponentActivity() {
    companion object {
        const val EXTRA_CAMPSITE_ID = "campsiteId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val campsiteId = intent.getStringExtra(EXTRA_CAMPSITE_ID)
        setContent {
            CampsiteLotterySystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LotteryManagementScreen(
                        campsiteId = campsiteId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LotteryManagementScreen(campsiteId: String?, modifier: Modifier = Modifier) {
    val lotteryController = remember { LotteryController(FirestoreLotteryDB(), FirestoreUserDB()) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var entrants by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(campsiteId) {
        if (campsiteId == null) {
            message = "No campsite was given."
            isLoading = false
            return@LaunchedEffect
        }
        lotteryController.getLotteryForCampsite(
            campsiteId,
            onSuccess = { found ->
                lottery = found
                if (found == null) {
                    message = "This campsite has no lottery yet."
                    isLoading = false
                } else {
                    lotteryController.getWaitlistEntrants(
                        found,
                        onSuccess = { users ->
                            entrants = users
                            isLoading = false
                        },
                        onFailure = { error ->
                            message = "Could not load the waitlist: ${error.message}"
                            isLoading = false
                        }
                    )
                }
            },
            onFailure = { error ->
                message = "Could not load the lottery: ${error.message}"
                isLoading = false
            }
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Waitlist & Winner Management", style = MaterialTheme.typography.titleLarge)

        if (isLoading) {
            Text("Loading...")
        }
        if (message.isNotEmpty()) {
            Text(message)
        }

        lottery?.let { current ->
            Text("Total entrants: ${current.waitingList.size}")
            Text("Winners to draw: ${current.numberOfWinners}")

            Text(
                "Entrant list",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            if (!isLoading && entrants.isEmpty()) {
                Text("No entrants have joined the waitlist yet.")
            }
            entrants.forEach { entrant ->
                val name = entrant.name.ifBlank { "No name given" }
                Text("$name (ID: ${entrant.deviceId})")
            }
        }
    }
}