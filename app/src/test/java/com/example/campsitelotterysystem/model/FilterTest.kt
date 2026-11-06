
package com.example.campsitelotterysystem.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class FilterTest {

    @Test
    fun filterHasCorrectDefaultValues() {
        val filter = Filter()

        assertEquals("", filter.location)
        assertNull(filter.date)
        assertNull(filter.numberOfGuests)
    }

    @Test
    fun filterStoresLocation() {
        val filter = Filter(location = "Edmonton")

        assertEquals("Edmonton", filter.location)
    }

    @Test
    fun filterStoresDate() {
        val date = Date(1791676800000L)
        val filter = Filter(date = date)

        assertEquals(date, filter.date)
    }

    @Test
    fun filterStoresGuestCount() {
        val filter = Filter(numberOfGuests = 4)

        assertEquals(4, filter.numberOfGuests)
    }
}

