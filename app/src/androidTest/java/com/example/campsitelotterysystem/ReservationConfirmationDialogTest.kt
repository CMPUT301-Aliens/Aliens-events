package com.example.campsitelotterysystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.campsitelotterysystem.model.Reservation
import com.example.campsitelotterysystem.ui.ReservationConfirmationDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Purpose : UI tests for ReservationConfirmationDialog (US 01.05.01).
 * Role : Verifies that Booking ID, campsite name and location, check-in/check-out dates, cost, and the "Done" button display and dismiss properly.
 * Outstanding issues : none
 */

class ReservationConfirmationDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun confirmationDialogDisplaysAllDetailsAndClosesOnDone() {
        val checkIn = Date(1_780_000_000_000L)
        val checkOut = Date(1_780_172_800_000L)
        val fmt = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        var doneClicked = false

        val reservation = Reservation(
            id = "BOOK-98765",
            entrantId = "user-1",
            campsiteId = "camp-1",
            campsiteName = "Jasper Lakeview",
            campsiteLocation = "Jasper, AB",
            reservedDates = listOf(checkIn, checkOut),
            totalCost = 120.0
        )

        composeRule.setContent {
            MaterialTheme {
                ReservationConfirmationDialog(
                    reservation = reservation,
                    onDone = { doneClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Reservation Confirmed!").assertIsDisplayed()
        composeRule.onNodeWithText("Booking ID: BOOK-98765").assertIsDisplayed()
        composeRule.onNodeWithText("Campsite: Jasper Lakeview").assertIsDisplayed()
        composeRule.onNodeWithText("Location: Jasper, AB").assertIsDisplayed()
        composeRule.onNodeWithText("Check-in Date: ${fmt.format(checkIn)}").assertIsDisplayed()
        composeRule.onNodeWithText("Check-out Date: ${fmt.format(checkOut)}").assertIsDisplayed()
        composeRule.onNodeWithText("Total Cost: $120.00").assertIsDisplayed()

        composeRule.onNodeWithText("Done").performClick()
        assertTrue(doneClicked)
    }
}