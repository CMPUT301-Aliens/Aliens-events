package com.example.campsitelotterysystem.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme

/**
 * Purpose: Activity hosting the Image Gallery UI
 * Role: activity that sets the view to the gallery
 * Outstanding Issues: None
 */
class ImageGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampsiteLotterySystemTheme {
                ImageGalleryScreen()
            }
        }
    }
}
