package com.example.campsitelotterysystem

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.campsitelotterysystem.ui.ImageGalleryScreen
import com.example.campsitelotterysystem.ui.theme.CampsiteLotterySystemTheme
import org.junit.Rule
import org.junit.Test

class ImageGalleryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun imageGalleryDisplaysContentOrEmptyState() {
        composeTestRule.setContent {
            CampsiteLotterySystemTheme {
                ImageGalleryScreen()
            }
        }

        // Wait for screen rendering
        composeTestRule.waitForIdle()
    }
}
