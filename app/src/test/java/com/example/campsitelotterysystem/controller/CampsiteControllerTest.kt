package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.experimental.theories.suppliers.TestedOn


class CampsiteControllerTest {

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

    private val validCampsite = Campsite(
        name = "Lakeview",
        location = "Banff",
        maximumOccupancy = 4,
        pricePerNight = 65.0,
        tags = listOf("lake", "hiking")
    )

    private val controller = CampsiteController(DummyCampsiteDB())

    @Test
    fun validCampsiteHasNoErrors() {
        assertTrue(controller.validateCampsite(validCampsite).isEmpty())
    }

    @Test
    fun blankNameIsAnError() {
        val errors = controller.validateCampsite(validCampsite.copy(name = ""))
        assertEquals(listOf("Campsite name cannot be blank."), errors)
    }

    @Test
    fun zeroMaxOccupancyIsAnError() {
        val errors = controller.validateCampsite(validCampsite.copy(maximumOccupancy = 0))
        assertEquals(listOf("Capacity must be greater than zero."), errors)
    }

    @Test
    fun NegativePricePerNightIsAnError() {
        val errors = controller.validateCampsite(validCampsite.copy(pricePerNight = -65.0))
        assertEquals(listOf("Price per night cannot be negative."), errors)
    }

    @Test
    fun noTagsIsAnError() {
        val errors = controller.validateCampsite(validCampsite.copy(tags = emptyList()))
        assertEquals(listOf("At least one tag required."), errors)
    }


    @Test
    fun RegisterSavesValidCampsites() {

        val db = DummyCampsiteDB()
        var savedId = ""

        CampsiteController(db).registerCampsite(
            validCampsite,
            "owner-1",
            onSuccess = { id -> savedId = id },
            onInvalid = { throw AssertionError("should be valid") },
            { throw it })
        assertEquals("fake-id-1", savedId)
        assertEquals(1, db.savedData.size)
    }

    @Test
    fun RegisterSetsOwnerAndPublished() {

        val db = DummyCampsiteDB()

        CampsiteController(db).registerCampsite(
            validCampsite,
            "owner-1",
            onSuccess = {},
            onInvalid = { throw AssertionError("should be valid") },
            { throw it })
        assertEquals("owner-1", db.savedData[0].ownerId)
        assertTrue(db.savedData[0].published)
    }

    @Test
    fun RegisterDoesNotSaveInvalidCampsites() {

        val db = DummyCampsiteDB()
        var errors = emptyList<String>()

        CampsiteController(db).registerCampsite(
            validCampsite.copy(name = ""),
            "owner-1",
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            { throw it })
        assertEquals(0, db.savedData.size)
        assertEquals(listOf("Campsite name cannot be blank."), errors)
    }

    @Test
    fun updateChangesCampsiteButKeepsSameId() {
        val db = DummyCampsiteDB()
        val controller = CampsiteController(db)

        controller.registerCampsite(validCampsite, "owner-1", {}, {}, {})
        val original = db.savedData[0]

        controller.updateCampsite(
            original.copy(name = "Lakeview Ridge"),
            onSuccess = {},
            { throw AssertionError("should be valid") },
            onFailure = { throw it })
        assertEquals(1, db.savedData.size)
        assertEquals("Lakeview Ridge", db.savedData[0].name)
        assertEquals(original.id, db.savedData[0].id)
        assertEquals("owner-1", db.savedData[0].ownerId)
    }

    @Test
    fun OwnerOnlyGetsTheirOwnCampsites() {
        val db = DummyCampsiteDB()
        val controller = CampsiteController(db)

        controller.registerCampsite(validCampsite, "owner-1", {}, {}, {})
        controller.registerCampsite(validCampsite.copy(name = "Riverbend"), "owner-2", {}, {}, {})

        var result = emptyList<Campsite>()
        CampsiteController(db).getOwnerCampsites(
            "owner-1",
            onSuccess = { result = it },
            { throw it })

        assertEquals(1, result.size)
        assertEquals("Lakeview", result[0].name)
    }
}