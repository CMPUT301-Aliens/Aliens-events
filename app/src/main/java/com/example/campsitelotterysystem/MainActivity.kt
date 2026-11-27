package com.example.campsitelotterysystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.FirestoreLotteryDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.model.User
import com.example.campsitelotterysystem.ui.AdminDashboardActivity
import com.example.campsitelotterysystem.ui.CampsiteDetailsScreen
import com.example.campsitelotterysystem.ui.CampsiteSearchScreen
import com.example.campsitelotterysystem.ui.OwnerDashboardActivity
import com.example.campsitelotterysystem.ui.ProfileScreen
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme

/** Which screen is showing. Waitlists and Bookings are not built yet. */
private enum class AppScreen { SEARCH, PROFILE }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the current device ID
        val deviceId = getCurrentUserId(this)

        // Create the campsite controller
        val campsiteController = CampsiteController(FirestoreCampsiteDB())

        enableEdgeToEdge()

        setContent {
            var currentUser by rememberSaveable { mutableStateOf<com.example.campsitelotterysystem.model.User?>(null) }

            androidx.compose.runtime.LaunchedEffect(deviceId) {
                FirestoreUserDB().getOrCreateUser(
                    deviceId,
                    onSuccess = { user ->
                        Log.d("UserDB", "Loaded user: $user")
                        currentUser = user
                    },
                    onFailure = { error ->
                        Log.e("UserDB", "Failed to load user", error)
                    }
                )
            }

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
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .navigationBarsPadding()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                          // Temporary button to test MessageActivity — remove before final PR - adds random values for recipientID, reservationID, header
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            val intent = Intent(
                                                this@MainActivity,
                                                com.example.campsitelotterysystem.ui.MessageActivity::class.java
                                            ).apply {
                                                putExtra(com.example.campsitelotterysystem.ui.MessageActivity.EXTRA_RESERVATION_ID, "test-res-101")
                                                putExtra(com.example.campsitelotterysystem.ui.MessageActivity.EXTRA_RECIPIENT_ID, "test-owner-999")
                                                putExtra(com.example.campsitelotterysystem.ui.MessageActivity.EXTRA_HEADER_TITLE, "Pine Valley — Owner")
                                            }
                                            startActivity(intent)
                                        }
                                    ) {
                                        Text("Test Chat")
                                    }

                                    // Button to open Admin Dashboard Activity
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            startActivity(Intent(this@MainActivity,
                                                AdminDashboardActivity::class.java))
                                        }
                                    ) {
                                        Text("Admin Dashboard")
                                    }

                                    // Button to open Owner Dashboard Activity
                                    ExtendedFloatingActionButton(
                                        onClick = {
                                            startActivity(Intent(this@MainActivity, OwnerDashboardActivity::class.java))
                                        }
                                    ) {
                                        Text("Owner")
                                    }
                                }
                            }
                        } else {
                            CampsiteDetailsScreen(
                                campsite = campsite,
                                userId = deviceId,
                                onBack = { selectedCampsite = null },
                                onRateCampsite = { rating, text, onComplete, onError ->
                                    val db = com.example.campsitelotterysystem.db.DBConnector.db
                                    db.collection("reviews")
                                        .whereEqualTo("campsiteId", campsite.id)
                                        .whereEqualTo("reviewerId", deviceId)
                                        .get()
                                        .addOnSuccessListener { snapshot ->
                                            if (!snapshot.isEmpty) {
                                                onError("You have already reviewed this campsite.")
                                            } else {
                                                val newCount = campsite.ratingCount + 1
                                                val newAvg = ((campsite.averageRating * campsite.ratingCount) + rating) / newCount
                                                val updatedCampsite = campsite.copy(
                                                    averageRating = newAvg,
                                                    ratingCount = newCount
                                                )
                                                val review = com.example.campsitelotterysystem.model.Review(
                                                    campsiteId = campsite.id,
                                                    reviewerId = deviceId,
                                                    rating = rating,
                                                    text = text,
                                                    reviewerName = currentUser?.name?.takeIf { it.isNotBlank() } ?: "Anonymous",
                                                    reviewerProfileImageUrl = currentUser?.profileImageUrl ?: ""
                                                )
                                                db.collection("reviews")
                                                    .add(review)
                                                    .addOnSuccessListener {
                                                        campsiteController.updateCampsite(
                                                            campsite = updatedCampsite,
                                                            onSuccess = {
                                                                selectedCampsite = updatedCampsite
                                                                onComplete()
                                                            },
                                                            onInvalid = { onError("Invalid campsite data") },
                                                            onFailure = { onError(it.message ?: "Failed to update") }
                                                        )
                                                    }
                                                    .addOnFailureListener {
                                                        onError(it.message ?: "Failed to save review")
                                                    }
                                            }
                                        }
                                        .addOnFailureListener {
                                            onError(it.message ?: "Failed to check existing reviews")
                                        }
                                }
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
