package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Represents the search criteria used to filter available campsites.
 *
 * Users can filter campsites by location, availability date range,
 * and minimum guest capacity.
 *
 * @property location Location keyword used to filter campsites.
 * @property dateRange Start and end dates for campsite availability.
 * @property numberOfGuests Minimum number of guests the campsite must accommodate.
 */
data class Filter(
    val location: String = "",
    val dateRange: Pair<Date, Date>? = null,
    val numberOfGuests: Int = 0
)