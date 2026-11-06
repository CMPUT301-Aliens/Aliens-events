
package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.Campsite

class FakeCampsiteDB(
    var campsites: List<Campsite> = emptyList()
) : CampsiteDB {

    override fun getAllCampsites(
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(campsites)
    }

    override fun addCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites = campsites + campsite
        onSuccess(campsite.id)
    }

    override fun removeCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites = campsites.filterNot { it.id == campsite.id }
        onSuccess(campsite.id)
    }

    override fun updateCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites = campsites.map {
            if (it.id == campsite.id) campsite else it
        }
        onSuccess()
    }

    override fun getCampsite(
        id: String,
        onSuccess: (Campsite?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(campsites.find { it.id == id })
    }

    override fun getCampsitesByOwner(
        ownerId: String,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        onSuccess(campsites.filter { it.ownerId == ownerId })
    }

    override fun removeAllCampsitesByOwner(
        ownerId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites = campsites.filterNot { it.ownerId == ownerId }
        onSuccess()
    }
}

