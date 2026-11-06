package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import org.junit.Assert.assertEquals
import org.junit.Test

class CampsiteControllerTest {

    private class FakeCampsiteDB : CampsiteDB {
        var returnedCampsites = listOf<Campsite>()
        val removedCampsites = mutableListOf<Campsite>()

        override fun getAllCampsites(onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess(returnedCampsites)
        }

        override fun removeCampsite(campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
            removedCampsites.add(campsite)
            onSuccess(campsite.id)
        }

        //currently untested methods in the interface
        override fun addCampsite(campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun updateCampsite(campsite: Campsite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getCampsite(id: String, onSuccess: (Campsite?) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getCampsitesByOwner(ownerId: String, onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun removeAllCampsitesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
    }

    @Test
    fun getAllCampsitesReturnsList() {
        val fakeDb = FakeCampsiteDB()
        val controller = CampsiteController(fakeDb)

        val expectedList = listOf(
            Campsite(id = "camp1", name = "Banff Spot"),
            Campsite(id = "camp2", name = "Jasper Spot")
        )
        fakeDb.returnedCampsites = expectedList

        var actualList: List<Campsite>? = null
        controller.getAllCampsites(
            onSuccess = { campsites -> actualList = campsites },
            onFailure = { throw it }
        )

        assertEquals(expectedList, actualList)
        assertEquals(2, actualList?.size)
    }

    @Test
    fun removeCampsiteCallsDatabase() {
        val fakeDb = FakeCampsiteDB()
        val controller = CampsiteController(fakeDb)
        val campsite = Campsite(id = "camp-101", name = "Test Camp")

        var removedId = ""
        controller.removeCampsite(
            campsite = campsite,
            onSuccess = { id -> removedId = id },
            onFailure = { throw it }
        )

        assertEquals("camp-101", removedId)
        assertEquals(1, fakeDb.removedCampsites.size)
        assertEquals("camp-101", fakeDb.removedCampsites[0].id)
    }
}
