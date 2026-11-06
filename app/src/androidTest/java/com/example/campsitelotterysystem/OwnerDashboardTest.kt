package com.example.campsitelotterysystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import com.example.campsitelotterysystem.CampsiteEditScreenTest.DummyCampsiteDB
import com.example.campsitelotterysystem.controller.CampsiteController
import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.ui.CampsiteEditScreen
import com.example.campsitelotterysystem.ui.OwnerDashboardScreen
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test



class OwnerDashboardTest {

    @get:Rule
    val draw = createComposeRule()

    private class DummyCampsiteDB : CampsiteDB {
        val savedData = mutableListOf<Campsite>()

        override fun addCampsite(
            campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
        ) {
            val id = "fake-id-${savedData.size + 1}"
            savedData.add(campsite.copy(id = id))
            onSuccess(id)
        }

        override fun updateCampsite(
            campsite: Campsite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
        ) {
            val index = savedData.indexOfFirst { it.id == campsite.id }
            savedData[index] = campsite
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
            onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit
        ) {
            onSuccess(savedData)
        }

        override fun removeCampsite(
            campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
        ) {
            onSuccess(campsite.id)
        }

        override fun removeAllCampsitesByOwner(
            ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
        ) {
            onSuccess()
        }
    }

    private val existing = Campsite(
        id = "c1",
        ownerId= "owner-1",
        name = "Lakeview",
        location = "Banff",
        maximumOccupancy = 4,
        pricePerNight = 65.0,
        tags = listOf("lake", "hiking")
    )

    @Test
    fun showsOnlyMyCampsites() {
        val db = DummyCampsiteDB()
        db.savedData.add(Campsite("c1",ownerId =  "owner-1", name = "Lakeview", location = "Banff"))
        db.savedData.add(Campsite("c2",ownerId =  "owner-2", name = "Riverbend", location = "Canmore"))

        draw.setContent { OwnerDashboardScreen(CampsiteController(db), "owner-1", 0) }

        draw.onNodeWithText("Lakeview").assertIsDisplayed()
        draw.onNodeWithText("Riverbend").assertDoesNotExist()

    }

    @Test
    fun noCampsitesShowsMessages() {
        val db = DummyCampsiteDB()

        draw.setContent { OwnerDashboardScreen(CampsiteController(db), "owner-1", 0) }

        draw.onNodeWithText("registered any campsites yet", substring = true, ignoreCase = true).assertIsDisplayed()

    }

}