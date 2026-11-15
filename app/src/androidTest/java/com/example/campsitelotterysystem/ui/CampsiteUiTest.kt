
package com.example.campsitelotterysystem.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CampsiteUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val pineLake = Campsite(
        id = "1",
        name = "Pine Lake Campground",
        location = "Edmonton",
        description = "A quiet campsite near the lake.",
        maximumOccupancy = 6,
        pricePerNight = 45.0,
        averageRating = 4.5,
        tags = listOf("lake", "fishing"),
        rulesAndPolicy = "No littering."
    )

    private val riverCamp = Campsite(
        id = "2",
        name = "River Camp",
        location = "Calgary",
        description = "A campsite beside the river.",
        maximumOccupancy = 4,
        pricePerNight = 35.0,
        averageRating = 4.0,
        tags = listOf("river")
    )

    private fun createController(): CampsiteController {
        return CampsiteController(
            FakeCampsiteDB(listOf(pineLake, riverCamp))
        )
    }

    @Test
    fun searchScreenDisplaysCampsites() {
        composeRule.setContent {
            MaterialTheme {
                CampsiteSearchScreen(
                    controller = createController(),
                    onCampsiteClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Find Campsites").assertIsDisplayed()
        composeRule.onNodeWithText("Pine Lake Campground").assertIsDisplayed()
        composeRule.onNodeWithText("River Camp").assertIsDisplayed()
        composeRule.onNodeWithText("2 campsites found").assertIsDisplayed()
    }

    @Test
    fun searchScreenFiltersCampsitesByKeyword() {
        composeRule.setContent {
            MaterialTheme {
                CampsiteSearchScreen(
                    controller = createController(),
                    onCampsiteClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Search campsites...")
            .performTextInput("Pine")

        composeRule.onNodeWithText("Pine Lake Campground")
            .assertIsDisplayed()

        composeRule.onNodeWithText("1 campsites found")
            .assertIsDisplayed()
    }

    @Test
    fun searchScreenOpensSelectedCampsite() {
        var selectedCampsite: Campsite? = null

        composeRule.setContent {
            MaterialTheme {
                CampsiteSearchScreen(
                    controller = createController(),
                    onCampsiteClick = {
                        selectedCampsite = it
                    }
                )
            }
        }

        composeRule.onNodeWithText("Pine Lake Campground")
            .performClick()

        composeRule.runOnIdle {
            assertEquals("1", selectedCampsite?.id)
        }
    }

    @Test
    fun detailsScreenDisplaysCampsiteInformation() {
        composeRule.setContent {
            MaterialTheme {
                CampsiteDetailsScreen(
                    campsite = pineLake,
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText("Campsite Details").assertIsDisplayed()
        composeRule.onNodeWithText("Pine Lake Campground").assertIsDisplayed()
        composeRule.onNodeWithText("Location: Edmonton").assertIsDisplayed()
        composeRule.onNodeWithText("Maximum guests: 6").assertIsDisplayed()
        composeRule.onNodeWithText("Description").assertIsDisplayed()

        composeRule.onNodeWithText(
            "A quiet campsite near the lake."
        ).assertIsDisplayed()

        composeRule.onNodeWithText("Rules and Policies").assertIsDisplayed()
        composeRule.onNodeWithText("No littering.").assertIsDisplayed()
        composeRule.onNodeWithText("Tags").assertIsDisplayed()
        composeRule.onNodeWithText("lake, fishing").assertIsDisplayed()
    }

    @Test
    fun detailsScreenBackButtonCallsOnBack() {
        var backClicked = false

        composeRule.setContent {
            MaterialTheme {
                CampsiteDetailsScreen(
                    campsite = pineLake,
                    onBack = {
                        backClicked = true
                    }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Go back")
            .performClick()

        composeRule.runOnIdle {
            assertEquals(true, backClicked)
        }
    }
}

/**
 * In-memory database for campsite UI tests.
 * Prevents tests from connecting to Firebase.
 */
private class FakeCampsiteDB(
    private val data: List<Campsite>
) : CampsiteDB {

    override fun addCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(campsite.id)
    }

    override fun removeCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(campsite.id)
    }

    override fun updateCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess()
    }

    override fun getCampsite(
        id: String,
        onSuccess: (Campsite?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(data.find { it.id == id })
    }

    override fun getCampsitesByOwner(
        ownerId: String,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(data.filter { it.ownerId == ownerId })
    }

    override fun getAllCampsites(
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(data)
    }

    override fun removeAllCampsitesByOwner(
        ownerId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess()
    }
}
