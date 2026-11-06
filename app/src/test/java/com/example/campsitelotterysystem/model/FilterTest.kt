
package com.example.campsitelotterysystem.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class FilterTest {

    @Test
    fun filterHasCorrectDefaultValues() {
        val filter = Filter()

        assertEquals("", filter.location)
        assertNull(filter.dateRange)
        assertEquals(0, filter.numberOfGuests)
    }

    @Test
    fun filterStoresLocation() {
        val filter = Filter(location = "Edmonton")

        assertEquals("Edmonton", filter.location)
    }

    @Test
    fun filterStoresDateRange() {
        val startDate = Date(1791676800000L)
        val endDate = Date(1791763200000L)

        val filter = Filter(
            dateRange = Pair(startDate, endDate)
        )

        assertEquals(startDate, filter.dateRange?.first)
        assertEquals(endDate, filter.dateRange?.second)
    }

    @Test
    fun filterStoresGuestCount() {
        val filter = Filter(numberOfGuests = 4)

        assertEquals(4, filter.numberOfGuests)
    }
}
