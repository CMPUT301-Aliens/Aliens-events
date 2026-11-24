
package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.Campsite

/**
 * Purpose : Reads and writes campsites in the "campsites" Firebase collection.
 * Role : CampsiteController talks to CampsiteDB interface.
 * Design pattern : Interface + implementation , for tests to be able to swap in fake database without touching Firestore.
 * Outstanding issues : none
 */

interface CampsiteDB {

    /**
     * saves a campsite with a new Firestore ID and sends the new ID to onSuccess
     */
    fun addCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Deletes a campsite and sends it's ID to onSuccess
     */
    fun removeCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Overwrites and existing campsite with new values
     */
    fun updateCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Finds a campsite by its ID . If the campsite does not exist sends null to onSuccess
     */
    fun getCampsite(
        id: String,
        onSuccess: (Campsite?) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * Find all campsites using ownerId that belongs to an owner
     */
    fun getCampsitesByOwner(
        ownerId: String,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    )

    /**
     * gets all campsites from all owners
     */
    fun getAllCampsites(
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    )
    /**
     * deletes all campsites that belong to an owner
     */
    fun removeAllCampsitesByOwner(
        ownerId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
}

class FirestoreCampsiteDB : CampsiteDB {

    private val campsites = DBConnector.db.collection("campsites")

    // Add a new campsite
    override fun addCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val document = campsites.document()

        document.set(campsite.copy(id = document.id))
            .addOnSuccessListener {
                onSuccess(document.id)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    // Remove a campsite
    override fun removeCampsite(
        campsite: Campsite,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites.document(campsite.id).delete()
            .addOnSuccessListener {
                onSuccess(campsite.id)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    // Update an existing campsite
    override fun updateCampsite(
        campsite: Campsite,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites.document(campsite.id).set(campsite)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    // Get a campsite using its document ID
    override fun getCampsite(
        id: String,
        onSuccess: (Campsite?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites.document(id).get()
            .addOnSuccessListener { document ->
                onSuccess(document.toObject(Campsite::class.java))
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    // Get all campsites belonging to a specific owner
    override fun getCampsitesByOwner(
        ownerId: String,
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->
                onSuccess(
                    documents.toObjects(Campsite::class.java)
                )
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    // Get all campsites from Firestore
    override fun getAllCampsites(
        onSuccess: (List<Campsite>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites.get()
            .addOnSuccessListener { documents ->
                val campsiteList =
                    documents.toObjects(Campsite::class.java)

                onSuccess(campsiteList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Remove all campsites belonging to a specific owner
    override fun removeAllCampsitesByOwner(
        ownerId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsites.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                val batch = DBConnector.db.batch()

                for (doc in documents) {
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}
