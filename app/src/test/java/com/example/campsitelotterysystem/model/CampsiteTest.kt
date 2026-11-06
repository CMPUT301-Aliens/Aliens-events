
package com.example.campsitelotterysystem.model

import org.junit.Assert.*
import org.junit.Test

class CampsiteTest {

    @Test
    fun campsiteStoresCorrectInformation() {
        val campsite = Campsite(
            id = "1",
            name = "Pine Lake",
            location = "Edmonton",
            maximumOccupancy = 6,
            pricePerNight = 45.0,
            averageRating = 4.5,
            tags = listOf("lake", "fishing")
        )

        assertEquals("1", campsite.id)
        assertEquals("Pine Lake", campsite.name)
        assertEquals("Edmonton", campsite.location)
        assertEquals(6, campsite.maximumOccupancy)
        assertEquals(45.0, campsite.pricePerNight, 0.0)
        assertEquals(4.5, campsite.averageRating, 0.0)
        assertEquals(listOf("lake", "fishing"), campsite.tags)
    }

    @Test
    fun campsiteHasCorrectDefaultValues() {
        val campsite = Campsite()

        assertEquals("", campsite.name)
        assertEquals("", campsite.location)
        assertEquals(0, campsite.maximumOccupancy)
        assertEquals(0.0, campsite.pricePerNight, 0.0)
        assertTrue(campsite.tags.isEmpty())
        assertTrue(campsite.promotionalImages.isEmpty())
        assertTrue(campsite.availableDates.isEmpty())
        assertFalse(campsite.published)
    }
}

