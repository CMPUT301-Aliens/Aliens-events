package com.example.campsitelotterysystem

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.CampsiteDetailsScreen
import com.example.campsitelotterysystem.ui.CampsiteSearchScreen
import com.example.campsitelotterysystem.ui.ProfileScreen
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme

/** Which screen is showing. Waitlists and Bookings are not built yet. */
private enum class AppScreen { SEARCH, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the current device ID
        val deviceId = getCurrentUserId(this)

        // Load or create the user in Firestore
        FirestoreUserDB().getOrCreateUser(
            deviceId,
            onSuccess = { user ->
                Log.d("UserDB", "Loaded user: $user")
            },
            onFailure = { error ->
                Log.e("UserDB", "Failed to load user", error)
            }
        )

        // Create the campsite controller
        val campsiteController = CampsiteController(FirestoreCampsiteDB())

        enableEdgeToEdge()

        setContent {
            CampsiteLotterySystemTheme {
                var screen by rememberSaveable { mutableStateOf(AppScreen.SEARCH) }
                // Campsite isn't saveable, so this resets to search after a rotation.
                var selectedCampsite by remember { mutableStateOf<Campsite?>(null) }

                when (screen) {
                    AppScreen.SEARCH -> {
                        val campsite = selectedCampsite
                        if (campsite == null) {
                            Box(Modifier.fillMaxSize().statusBarsPadding()) {
                                CampsiteSearchScreen(
                                    controller = campsiteController,
                                    onCampsiteClick = { selectedCampsite = it },
                                    onOpenProfile = { screen = AppScreen.PROFILE }
                                )
                            }
                        } else {
                            CampsiteDetailsScreen(
                                campsite = campsite,
                                onBack = { selectedCampsite = null }
                            )
                        }
                    }

                    AppScreen.PROFILE -> {
                        BackHandler {
                            selectedCampsite = null
                            screen = AppScreen.SEARCH
                        }
                        ProfileScreen(
                            onOpenSearch = {
                                selectedCampsite = null
                                screen = AppScreen.SEARCH
                            }
                        )
                    }
                }
            }
        }
    }
}
