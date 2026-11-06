
package com.example.campsitelotterysystem.model

import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class CampsiteTest {

    @Test
    fun campsiteStoresCorrectInformation() {
        val date = Date(1791676800000L)

        val campsite = Campsite(
            id = "c1",
            ownerId = "owner1",
            name = "Lakeview",
            location = "Banff",
            coordinates = GeoPoint(51.1784, -115.5708),
            description = "Beautiful campsite",
            maximumOccupancy = 4,
            pricePerNight = 65.0,
            averageRating = 4.5,
            promotionalImages = listOf("image1.jpg"),
            rulesAndPolicy = "No smoking",
            tags = listOf("lake", "hiking"),
            availableDates = listOf(date),
            qrCodeData = "QR123",
            published = true
        )

        assertEquals("c1", campsite.id)
        assertEquals("owner1", campsite.ownerId)
        assertEquals("Lakeview", campsite.name)
        assertEquals("Banff", campsite.location)
        assertEquals(GeoPoint(51.1784, -115.5708), campsite.coordinates)
        assertEquals("Beautiful campsite", campsite.description)
        assertEquals(4, campsite.maximumOccupancy)
        assertEquals(65.0, campsite.pricePerNight, 0.001)
        assertEquals(4.5, campsite.averageRating, 0.001)
        assertEquals(listOf("image1.jpg"), campsite.promotionalImages)
        assertEquals("No smoking", campsite.rulesAndPolicy)
        assertEquals(listOf("lake", "hiking"), campsite.tags)
        assertEquals(listOf(date), campsite.availableDates)
        assertEquals("QR123", campsite.qrCodeData)
        assertEquals(true, campsite.published)
    }

    @Test
    fun campsiteHasCorrectDefaultValues() {
        val campsite = Campsite()

        assertEquals("", campsite.id)
        assertEquals("", campsite.ownerId)
        assertEquals("", campsite.name)
        assertEquals("", campsite.location)
        assertNull(campsite.coordinates)
        assertEquals("", campsite.description)
        assertEquals(0, campsite.maximumOccupancy)
        assertEquals(0.0, campsite.pricePerNight, 0.001)
        assertEquals(0.0, campsite.averageRating, 0.001)
        assertEquals(emptyList<String>(), campsite.promotionalImages)
        assertEquals("", campsite.rulesAndPolicy)
        assertEquals(emptyList<String>(), campsite.tags)
        assertEquals(emptyList<Date>(), campsite.availableDates)
        assertEquals("", campsite.qrCodeData)
        assertEquals(false, campsite.published)
    }

    @Test
    fun campsiteCopyCreatesCorrectCopy() {
        val original = Campsite(
            id = "c1",
            ownerId = "owner1",
            name = "Lakeview",
            location = "Banff",
            maximumOccupancy = 4
        )

        val copied = original.copy(name = "Mountain View")

        assertEquals("Lakeview", original.name)
        assertEquals("Mountain View", copied.name)
        assertEquals(original.id, copied.id)
        assertEquals(original.ownerId, copied.ownerId)
        assertEquals(original.location, copied.location)
        assertEquals(original.maximumOccupancy, copied.maximumOccupancy)
    }
}