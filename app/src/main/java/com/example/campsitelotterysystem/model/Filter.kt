package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Purpose : Search criteria an entrant sets when filtering campsites (US 01.01.02).
 * Role : Built by the filter box on the search screen, applied by CampsiteController.filterCampsites.
 * Outstanding issues : None. Blank, null or 0 means "don't filter by this".
 */
data class Filter(
    val location: String = "",
    val dateRange: Pair<Date, Date>? = null,
    val numberOfGuests: Int = 0
)