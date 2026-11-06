
package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Filter
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class CampsiteController(private val campsiteDB: CampsiteDB) {

    fun validateCampsite(campsite: Campsite): List<String> {
        val errors = mutableListOf<String>()

        if (campsite.name.isBlank()) {
            errors.add("Campsite name cannot be blank.")
        }

        if (campsite.location.isBlank()) {
            errors.add("Campsite location cannot be blank.")
        }

        if (campsite.maximumOccupancy <= 0) {
            errors.add("Capacity must be greater than zero.")
        }

        if (campsite.pricePerNight < 0.0) {
            errors.add("Price per night cannot be negative.")
        }

        val point = campsite.coordinates
        if (point != null) {
            if (point.latitude !in -90.0..90.0 ||
                point.longitude !in -180.0..180.0
            ) {
                errors.add("Map location is invalid.")
            }
        }

        return errors
    }

    fun registerCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onInvalid: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val errors = validateCampsite(campsite)

        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }

        // TODO(#7): Replace test owner with the current user's ID.
        val newCampsite = campsite.copy(
            ownerId = "test-owner",
            published = true
        )

        campsiteDB.addCampsite(newCampsite, onSuccess, onFailure)
    }

    fun updateCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit,
        onInvalid: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val errors = validateCampsite(campsite)

        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }

        campsiteDB.updateCampsite(campsite, onSuccess, onFailure)
    }

    fun getCampsite(
        id: String,
        onSuccess: (Campsite?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.getCampsite(id, onSuccess, onFailure)
    }

    fun getOwnerCampsites(
        ownerId: String,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.getCampsitesByOwner(ownerId, onSuccess, onFailure)
    }

    fun removeCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.removeCampsite(campsite, onSuccess, onFailure)
    }

    fun getAllCampsites(
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.getAllCampsites(onSuccess, onFailure)
    }

    // US 01.01.01 (#20): Search campsites
    fun searchCampsites(
        keyword: String,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.getAllCampsites(
            onSuccess = { campsites ->
                val searchText = keyword.trim()

                val results = campsites.filter { campsite ->
                    searchText.isBlank() ||
                            campsite.name.contains(searchText, ignoreCase = true) ||
                            campsite.location.contains(searchText, ignoreCase = true) ||
                            campsite.tags.any { tag ->
                                tag.contains(searchText, ignoreCase = true)
                            }
                }

                onSuccess(results)
            },
            onFailure = onFailure
        )
    }

    // US 01.01.02 (#21): Filter campsites
    fun filterCampsites(
        filter: Filter,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.getAllCampsites(
            onSuccess = { campsites ->
                val results = campsites.filter { campsite ->

                    val locationMatches =
                        filter.location.isBlank() ||
                                campsite.location.contains(
                                    filter.location.trim(),
                                    ignoreCase = true
                                )

                    val guestsMatch =
                        filter.numberOfGuests == null ||
                                campsite.maximumOccupancy >= filter.numberOfGuests

                    val dateMatches =
                        filter.date == null ||
                                campsite.availableDates.any { availableDate ->
                                    isSameCalendarDay(availableDate, filter.date)
                                }

                    locationMatches && guestsMatch && dateMatches
                }

                onSuccess(results)
            },
            onFailure = onFailure
        )
    }

    // Compare calendar days using UTC to avoid timezone mismatches.
    private fun isSameCalendarDay(
        first: Date,
        second: Date
    ): Boolean {
        val utcTimeZone = TimeZone.getTimeZone("UTC")

        val firstCalendar = Calendar.getInstance(utcTimeZone).apply {
            time = first
        }

        val secondCalendar = Calendar.getInstance(utcTimeZone).apply {
            time = second
        }

        return firstCalendar.get(Calendar.YEAR) ==
                secondCalendar.get(Calendar.YEAR) &&
                firstCalendar.get(Calendar.DAY_OF_YEAR) ==
                secondCalendar.get(Calendar.DAY_OF_YEAR)
    }
}
