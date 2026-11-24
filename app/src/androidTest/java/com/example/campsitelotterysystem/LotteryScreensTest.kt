package com.example.campsitelotterysystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.campsitelotterysystem.ui.LotteryEditScreen
import com.example.campsitelotterysystem.ui.LotteryManagementScreen
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the lottery screens (#17, #32, #41).
 * They open the screens with no campsite, so they don't need any Firestore data.
 */
class LotteryScreensTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun publishIsDisabledWhenFormIsEmpty() {
        composeRule.setContent { LotteryEditScreen(campsiteId = null) }

        composeRule.onNodeWithText("Publish Lottery Listing").assertIsNotEnabled()
    }
    @Test
    fun formShowsAllFields() {
        composeRule.setContent { LotteryEditScreen(campsiteId = null) }

        composeRule.onNodeWithText("Number of winners").assertIsDisplayed()
        composeRule.onNodeWithText("Registration start").assertIsDisplayed()
        composeRule.onNodeWithText("Registration end").assertIsDisplayed()
        composeRule.onNodeWithText("Booking start").assertIsDisplayed()
        composeRule.onNodeWithText("Booking end").assertIsDisplayed()
    }

    @Test
    fun winnersAloneDoesNotEnablePublish() {
        composeRule.setContent { LotteryEditScreen(campsiteId = null) }

        composeRule.onNodeWithText("Number of winners").performTextInput("3")

        composeRule.onNodeWithText("Publish Lottery Listing").assertIsNotEnabled()
    }

    @Test
    fun managementScreenWithoutCampsiteShowsMessage() {
        composeRule.setContent { LotteryManagementScreen(campsiteId = null) }

        composeRule.onNodeWithText("Waitlist & Winner Management").assertIsDisplayed()
        composeRule.onNodeWithText("No campsite was given.").assertIsDisplayed()
    }
}