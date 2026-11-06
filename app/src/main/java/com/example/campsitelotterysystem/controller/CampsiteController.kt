package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.db.FirestoreCampsiteDB
import com.example.campsitelotterysystem.model.Campsite
import com.example.campsitelotterysystem.model.Filter
import java.util.Calendar
import java.util.Date

class CampsiteController(private val campsiteDB: CampsiteDB = FirestoreCampsiteDB()) {

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

        if (campsite.tags.isEmpty()) {
            errors.add("At least one tag required.")
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


    fun registerCampsite(campsite: Campsite,
                         ownerId : String,
                         onSuccess: (String) -> Unit,
                         onInvalid : (List<String>) -> Unit,
                         onFailure: (Exception) -> Unit){

        val errors = validateCampsite(campsite)

        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }


        val newCampsite = campsite.copy(ownerId = ownerId, published = true)


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

    /**
     * US 01.01.02 (#21): Returns the campsites that match the filter. Pure function, so it can be
     * tested without Firebase.
     * Location: contains the text, ignoring case. Guests: maximumOccupancy is at least the guests.
     * Dates: at least one availableDate falls inside the range (both end days included).
     */
    fun filterCampsites(campsites: List<Campsite>, filter: Filter): List<Campsite> =
        campsites.filter { c ->
            val locationOk = filter.location.isBlank() ||
                    c.location.contains(filter.location.trim(), ignoreCase = true)
            val guestsOk = filter.numberOfGuests <= 0 ||
                    c.maximumOccupancy >= filter.numberOfGuests
            val datesOk = filter.dateRange?.let { (start, end) ->
                val from = startOfDay(start)
                val until = Date(startOfDay(end).time + 24L * 60 * 60 * 1000)
                c.availableDates.any { !it.before(from) && it.before(until) }
            } ?: true
            locationOk && guestsOk && datesOk
        }

    private fun startOfDay(d: Date): Date = Calendar.getInstance().apply {
        time = d
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.time
}