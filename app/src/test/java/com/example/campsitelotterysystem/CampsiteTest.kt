package com.example.campsitelotterysystem
import com.example.campsitelotterysystem.model.Campsite
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

/**
 * Purpose : Unit test for campsite model.
 * Role : checks the default values for Firestore and that copy() keeps the id and owner.
 * Outstanding issues : none
 */

class CampsiteTest {

    @Test
    fun newCampsiteHasEmptyDefaults() {
        val campsite = Campsite()
        assertEquals("", campsite.id)
        assertEquals("", campsite.name)
        assertEquals(0, campsite.maximumOccupancy)
        assertEquals(0.0, campsite.pricePerNight, 0.0)
        assertTrue(campsite.tags.isEmpty())
        assertTrue(campsite.promotionalImages.isEmpty())
        assertNull(campsite.coordinates)
        assertFalse(Campsite().published)
    }


    @Test
    fun CopyKeepsIdAndOwnerWhenEditing() {
        val original = Campsite(id = "c1", ownerId= "owner-1", name ="Lakeview")
        val edited = original.copy(name = "Lakeview Ridge")
        assertEquals("c1", edited.id)
        assertEquals("owner-1", edited.ownerId)
        assertEquals("Lakeview Ridge", edited.name)
    }
}



