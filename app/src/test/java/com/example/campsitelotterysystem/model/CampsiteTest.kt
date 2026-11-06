
package com.example.campsitelotterysystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.CampsiteEditScreen
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Purpose: UI tests for registering, viewing and editing campsites.
 * Role: Checks user interactions using dummy campsite data.
 * Outstanding issues: None.
 */
class CampsiteEditScreenTest {

    @get:Rule
    val draw = createComposeRule()

    private class DummyCampsiteDB : CampsiteDB {

        val savedData = mutableListOf<Campsite>()

        override fun addCampsite(
            campsite: Campsite,
            onSuccess: (String) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            val id = "fake-id-${savedData.size + 1}"
            savedData.add(campsite.copy(id = id))
            onSuccess(id)
        }

        override fun updateCampsite(
            campsite: Campsite,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            val index = savedData.indexOfFirst { it.id == campsite.id }

            if (index >= 0) {
                savedData[index] = campsite
            }

            onSuccess()
        }

        override fun getCampsitesByOwner(
            ownerId: String,
            onSuccess: (List<Campsite>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(savedData.filter { it.ownerId == ownerId })
        }

        override fun getCampsite(
            id: String,
            onSuccess: (Campsite?) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(savedData.firstOrNull { it.id == id })
        }

        override fun getAllCampsites(
            onSuccess: (List<Campsite>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(savedData)
        }

        override fun removeCampsite(
            campsite: Campsite,
            onSuccess: (String) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(campsite.id)
        }

        override fun removeAllCampsitesByOwner(
            ownerId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess()
        }
    }

    private val existing = Campsite(
        id = "c1",
        ownerId = "owner-1",
        name = "Lakeview",
        location = "Banff",
        maximumOccupancy = 4,
        pricePerNight = 65.0,
        tags = listOf("lake", "hiking")
    )

    private fun type(label: String, text: String) {
        draw.onNodeWithText(label)
            .performScrollTo()
            .performTextInput(text)
    }

    private fun tap(buttonText: String) {
        draw.onNodeWithText(buttonText)
            .performScrollTo()
            .performClick()
    }

    @Test
    fun emptyFormShowsError() {
        val db = DummyCampsiteDB()

        draw.setContent {
            CampsiteEditScreen(
                CampsiteController(db),
                "owner-1",
                null,
                onDone = {}
            )
        }

        tap("Publish Campsite")

        draw.onNodeWithText(
            "Campsite name cannot be blank.",
            substring = true
        ).assertIsDisplayed()

        assertEquals(0, db.savedData.size)
    }

    @Test
    fun filledFormSavesCampsite() {
        val db = DummyCampsiteDB()

        draw.setContent {
            CampsiteEditScreen(
                CampsiteController(db),
                "owner-1",
                null,
                onDone = {}
            )
        }

        type("Campsite name", "Lakeview")
        type("Location", "Banff")
        type("Maximum occupancy", "4")
        type("Tags (required, comma separated)", "lake, hiking")

        tap("Publish Campsite")

        assertEquals(1, db.savedData.size)
        assertEquals("owner-1", db.savedData[0].ownerId)
    }

    @Test
    fun existingCampsiteShowsDetails() {
        val db = DummyCampsiteDB()
        db.savedData.add(existing)

        draw.setContent {
            CampsiteEditScreen(
                CampsiteController(db),
                "owner-1",
                "c1",
                onDone = {}
            )
        }

        draw.waitForIdle()

        draw.onNodeWithText("Campsite Details")
            .assertIsDisplayed()

        draw.onNodeWithText("Lakeview")
            .assertIsDisplayed()

        draw.onNodeWithText("Edit")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun editButtonUnlocksPrefilledForm() {
        val db = DummyCampsiteDB()
        db.savedData.add(existing)

        draw.setContent {
            CampsiteEditScreen(
                CampsiteController(db),
                "owner-1",
                "c1",
                onDone = {}
            )
        }

        draw.waitForIdle()

        draw.onNodeWithText("Edit")
            .performScrollTo()
            .performClick()

        draw.waitForIdle()

        draw.onNodeWithText("Campsite name")
            .assertIsEnabled()

        draw.onNodeWithText("Update Campsite Details")
            .performScrollTo()
            .assertIsDisplayed()

        draw.onNodeWithText("Cancel")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
