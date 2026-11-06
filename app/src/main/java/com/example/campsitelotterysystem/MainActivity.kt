
package com.example.campsitelotterysystem

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.getCurrentUserId
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.CampsiteDetailsScreen
import com.example.campsitelotterysystem.ui.CampsiteSearchScreen
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme

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
        val campsiteController =
            CampsiteController(FirestoreCampsiteDB())

        enableEdgeToEdge()

        setContent {
            CampsiteLotterySystemTheme {

                var selectedCampsite by remember {
                    mutableStateOf<Campsite?>(null)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {

                        if (selectedCampsite == null) {

                            CampsiteSearchScreen(
                                controller = campsiteController,
                                onCampsiteClick = { campsite ->
                                    selectedCampsite = campsite
                                }
                            )

                        } else {

                            CampsiteDetailsScreen(
                                campsite = selectedCampsite!!,
                                onBack = {
                                    selectedCampsite = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
