
package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Filter
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Date

class CampsiteControllerTest {

    private class FakeCampsiteDB(
        var campsites: List<Campsite> = emptyList()
    ) : CampsiteDB {

        val removedCampsites = mutableListOf<Campsite>()

        override fun getAllCampsites(
            onSuccess: (List<Campsite>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            onSuccess(campsites)
        }

        override fun removeCampsite(
            campsite: Campsite,
            onSuccess: (String) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            removedCampsites.add(campsite)
            onSuccess(campsite.id)
        }

        override fun addCampsite(
            campsite: Campsite,
            onSuccess: (String) -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun updateCampsite(
            campsite: Campsite,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun getCampsite(
            id: String,
            onSuccess: (Campsite?) -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun getCampsitesByOwner(
            ownerId: String,
            onSuccess: (List<Campsite>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {}

        override fun removeAllCampsitesByOwner(
            ownerId: String,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {}
    }

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
            availableDates = listOf(date(2026, 12, 11))
        )

        riverCamp = Campsite(
            id = "2",
            name = "River Valley Camping",
            location = "Calgary",
            maximumOccupancy = 4,
            pricePerNight = 35.0,
            averageRating = 4.0,
            tags = listOf("river", "hiking"),
            availableDates = listOf(date(2026, 12, 12))
        )

        forestCamp = Campsite(
            id = "3",
            name = "Forest Retreat",
            location = "Edmonton",
            maximumOccupancy = 10,
            pricePerNight = 60.0,
            tags = listOf("forest", "hiking"),
            availableDates = listOf(date(2026, 12, 11))
        )

        fakeDB = FakeCampsiteDB(
            listOf(pineLake, riverCamp, forestCamp)
        )

        controller = CampsiteController(fakeDB)
    }

    private fun date(
        year: Int,
        month: Int,
        day: Int,
        hour: Int = 12
    ): Date =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.time

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

        controller.searchCampsites("Toronto", { result = it }, {})

        assertTrue(result.isEmpty())
    }

    // FILTER TESTS

    @Test
    fun emptyFilterReturnsEveryCampsite() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter()
        )

        assertEquals(3, result.size)
    }

    @Test
    fun locationFilterMatchesText() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(location = "Edmonton")
        )

        assertEquals(listOf(pineLake, forestCamp), result)
    }

    @Test
    fun locationFilterIgnoresCaseAndSpaces() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(location = "  calgary ")
        )

        assertEquals(listOf(riverCamp), result)
    }

    @Test
    fun locationFilterWithNoMatchReturnsNothing() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(location = "Toronto")
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun guestFilterKeepsCampsitesThatFitEveryone() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(numberOfGuests = 5)
        )

        assertEquals(listOf(pineLake, forestCamp), result)
    }

    @Test
    fun guestFilterAllowsExactCapacity() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(numberOfGuests = 4)
        )

        assertEquals(3, result.size)
    }

    @Test
    fun zeroGuestsMeansAny() {
        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(numberOfGuests = 0)
        )

        assertEquals(3, result.size)
    }

    @Test
    fun dateRangeKeepsCampsitesWithADateInside() {
        val range = Pair(date(2026, 12, 10), date(2026, 12, 12))

        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(dateRange = range)
        )

        assertEquals(3, result.size)
    }

    @Test
    fun dateRangeIncludesTheEndDay() {
        val range = Pair(date(2026, 12, 12), date(2026, 12, 12))

        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(dateRange = range)
        )

        assertEquals(listOf(riverCamp), result)
    }

    @Test
    fun dateRangeExcludesDatesAfterTheEndDay() {
        val range = Pair(date(2026, 12, 13), date(2026, 12, 14))

        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(dateRange = range)
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun filtersCombineWithAnd() {
        val range = Pair(date(2026, 12, 10), date(2026, 12, 12))

        val result = controller.filterCampsites(
            listOf(pineLake, riverCamp, forestCamp),
            Filter(
                location = "Edmonton",
                dateRange = range,
                numberOfGuests = 7
            )
        )

        assertEquals(listOf(forestCamp), result)
    }

    // VALIDATION TESTS

    private fun validCampsite() = Campsite(
        name = "Test Camp",
        location = "Edmonton",
        maximumOccupancy = 5,
        pricePerNight = 40.0,
        tags = listOf("family")
    )

    @Test
    fun validCampsiteReturnsNoValidationErrors() {
        assertTrue(controller.validateCampsite(validCampsite()).isEmpty())
    }

    @Test
    fun blankNameReturnsValidationError() {
        val campsite = validCampsite().copy(name = "")

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Campsite name cannot be blank.")
        )
    }

    @Test
    fun blankLocationReturnsValidationError() {
        val campsite = validCampsite().copy(location = "")

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Campsite location cannot be blank.")
        )
    }

    @Test
    fun zeroOccupancyReturnsValidationError() {
        val campsite = validCampsite().copy(maximumOccupancy = 0)

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Capacity must be greater than zero.")
        )
    }

    @Test
    fun negativePriceReturnsValidationError() {
        val campsite = validCampsite().copy(pricePerNight = -10.0)

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("Price per night cannot be negative.")
        )
    }

    @Test
    fun missingTagsReturnsValidationError() {
        val campsite = validCampsite().copy(tags = emptyList())

        assertTrue(
            controller.validateCampsite(campsite)
                .contains("At least one tag required.")
        )
    }

    @Test
    fun validCoordinatesPassValidation() {
        val campsite = validCampsite().copy(
            coordinates = GeoPoint(53.5461, -113.4938)
        )

        assertTrue(controller.validateCampsite(campsite).isEmpty())
    }

}
