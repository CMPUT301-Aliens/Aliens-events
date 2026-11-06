
package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Filter
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class CampsiteControllerTest {

    private lateinit var fakeDB: FakeCampsiteDB
    private lateinit var controller: CampsiteController

    private lateinit var pineLake: Campsite
    private lateinit var riverCamp: Campsite
    private lateinit var forestCamp: Campsite

    @Before
    fun setUp() {
        pineLake = Campsite(
            id = "1",
            name = "Pine Lake Campground",
            location = "Edmonton",
            maximumOccupancy = 6,
            pricePerNight = 45.0,
            averageRating = 4.5,
            tags = listOf("lake", "fishing", "family"),
            availableDates = listOf(utcDate(2026, 10, 11))
        )

        riverCamp = Campsite(
            id = "2",
            name = "River Valley Camping",
            location = "Calgary",
            maximumOccupancy = 4,
            pricePerNight = 35.0,
            averageRating = 4.0,
            tags = listOf("river", "hiking"),
            availableDates = listOf(utcDate(2026, 10, 12))
        )

        forestCamp = Campsite(
            id = "3",
            name = "Forest Retreat",
            location = "Edmonton",
            maximumOccupancy = 10,
            pricePerNight = 60.0,
            tags = listOf("forest", "hiking"),
            availableDates = listOf(utcDate(2026, 10, 11))
        )

        fakeDB = FakeCampsiteDB(
            listOf(pineLake, riverCamp, forestCamp)
        )

        controller = CampsiteController(fakeDB)
    }

    // Helper to create dates in UTC.
    private fun utcDate(year: Int, month: Int, day: Int): Date {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            .apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
    }

    // SEARCH TESTS

    @Test
    fun searchByCampsiteNameReturnsMatchingCampsite() {
        var result = emptyList<Campsite>()

        controller.searchCampsites("Pine Lake", { result = it }, {})

        assertEquals(listOf(pineLake), result)
    }

    @Test
    fun searchByLocationReturnsMatchingCampsites() {
        var result = emptyList<Campsite>()

        controller.searchCampsites("Edmonton", { result = it }, {})

        assertEquals(listOf(pineLake, forestCamp), result)
    }

    @Test
    fun searchByTagReturnsMatchingCampsite() {
        var result = emptyList<Campsite>()

        controller.searchCampsites("fishing", { result = it }, {})

        assertEquals(listOf(pineLake), result)
    }

    @Test
    fun searchIsCaseInsensitive() {
        var result = emptyList<Campsite>()

        controller.searchCampsites("PINE LAKE", { result = it }, {})

        assertEquals(listOf(pineLake), result)
    }

    @Test
    fun blankSearchReturnsAllCampsites() {
        var result = emptyList<Campsite>()

        controller.searchCampsites("   ", { result = it }, {})

        assertEquals(3, result.size)
    }

    @Test
    fun searchWithNoMatchReturnsEmptyList() {
        var result = emptyList<Campsite>()

        controller.searchCampsites("Banff", { result = it }, {})

        assertTrue(result.isEmpty())
    }

    // FILTER TESTS

    @Test
    fun filterByLocationReturnsMatchingCampsites() {
        var result = emptyList<Campsite>()

        controller.filterCampsites(
            Filter(location = "Edmonton"),
            { result = it },
            {}
        )

        assertEquals(listOf(pineLake, forestCamp), result)
    }

    @Test
    fun filterByGuestCapacityReturnsSufficientCampsites() {
        var result = emptyList<Campsite>()

        controller.filterCampsites(
            Filter(numberOfGuests = 5),
            { result = it },
            {}
        )

        assertEquals(listOf(pineLake, forestCamp), result)
    }

    @Test
    fun filterExcludesCampsitesWithInsufficientCapacity() {
        var result = emptyList<Campsite>()

        controller.filterCampsites(
            Filter(numberOfGuests = 7),
            { result = it },
            {}
        )

        assertEquals(listOf(forestCamp), result)
    }

    @Test
    fun filterByDateReturnsAvailableCampsites() {
        var result = emptyList<Campsite>()

        controller.filterCampsites(
            Filter(date = utcDate(2026, 10, 11)),
            { result = it },
            {}
        )

        assertEquals(listOf(pineLake, forestCamp), result)
    }

    @Test
    fun filterCombinesLocationAndGuestCapacity() {
        var result = emptyList<Campsite>()

        controller.filterCampsites(
            Filter(
                location = "Edmonton",
                numberOfGuests = 7
            ),
            { result = it },
            {}
        )

        assertEquals(listOf(forestCamp), result)
    }

    @Test
    fun filterWithNoMatchingCampsitesReturnsEmptyList() {
        var result = emptyList<Campsite>()

        controller.filterCampsites(
            Filter(
                location = "Toronto",
                numberOfGuests = 5
            ),
            { result = it },
            {}
        )

        assertTrue(result.isEmpty())
    }

    // VALIDATION TESTS

    @Test
    fun validCampsiteReturnsNoValidationErrors() {
        val campsite = Campsite(
            name = "Test Camp",
            location = "Edmonton",
            maximumOccupancy = 5,
            pricePerNight = 40.0
        )

        assertTrue(controller.validateCampsite(campsite).isEmpty())
    }

    @Test
    fun blankNameReturnsValidationError() {
        val campsite = Campsite(
            name = "",
            location = "Edmonton",
            maximumOccupancy = 5,
            pricePerNight = 40.0
        )

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Campsite name cannot be blank.")
        )
    }

    @Test
    fun blankLocationReturnsValidationError() {
        val campsite = Campsite(
            name = "Test Camp",
            location = "",
            maximumOccupancy = 5,
            pricePerNight = 40.0
        )

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Campsite location cannot be blank.")
        )
    }

    @Test
    fun zeroOccupancyReturnsValidationError() {
        val campsite = Campsite(
            name = "Test Camp",
            location = "Edmonton",
            maximumOccupancy = 0,
            pricePerNight = 40.0
        )

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Capacity must be greater than zero.")
        )
    }

    @Test
    fun negativePriceReturnsValidationError() {
        val campsite = Campsite(
            name = "Test Camp",
            location = "Edmonton",
            maximumOccupancy = 5,
            pricePerNight = -10.0
        )

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Price per night cannot be negative.")
        )
    }

    @Test
    fun validCoordinatesPassValidation() {
        val campsite = Campsite(
            name = "Test Camp",
            location = "Edmonton",
            coordinates = GeoPoint(53.5461, -113.4938),
            maximumOccupancy = 5,
            pricePerNight = 40.0
        )

        assertTrue(controller.validateCampsite(campsite).isEmpty())
    }
}
