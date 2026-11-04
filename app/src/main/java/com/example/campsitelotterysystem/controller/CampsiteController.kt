package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Campsite



class CampsiteController (private val campsiteDB: CampsiteDB){

    fun validateCampsite(campsite: Campsite) : List<String> {
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
            if (point.latitude !in -90.0..90.0 || point.longitude !in -180.0..180.0) {
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

        val newCampsite = campsite.copy(ownerId = "OwnerID", published = true)

        campsiteDB.addCampsite(newCampsite, onSuccess, onFailure)
    }

    fun updateCampsite(campsite: Campsite,
                       onSuccess: () -> Unit,
                       onInvalid : (List<String>) -> Unit,
                       onFailure: (Exception) -> Unit){
        val errors = validateCampsite(campsite)

        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }

        campsiteDB.updateCampsite(campsite, onSuccess, onFailure)

    }

    fun getCampsite(id : String, onSuccess: (Campsite?) -> Unit, onFailure: (Exception) -> Unit){
        campsiteDB.getCampsite(id, onSuccess, onFailure)
    }

    fun getOwnerCampsites(ownerId: String, onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit){
        campsiteDB.getCampsitesByOwner(ownerId, onSuccess, onFailure)
    }

}