package com.example.campsitelotterysystem.db


import com.example.campsitelotterysystem.model.Campsite


interface CampsiteDB {
    fun addCampsite (campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    fun removeCampsite (campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    fun updateCampsite(campsite: Campsite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getCampsite(id : String , onSuccess: (Campsite?) -> Unit, onFailure: (Exception) -> Unit)
    fun getCampsitesByOwner(ownerId: String, onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit)
    fun getAllCampsites(onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit)
    fun removeAllCampsitesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreCampsiteDB : CampsiteDB {

    private val campsites = DBConnector.db.collection("campsites")

    override fun addCampsite (campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val document = campsites.document()
        document.set(campsite.copy(id = document.id))
            .addOnSuccessListener { onSuccess(document.id) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun removeCampsite (campsite: Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        campsites.document(campsite.id).delete()
            .addOnSuccessListener { onSuccess(campsite.id) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun updateCampsite(campsite: Campsite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        campsites.document(campsite.id).set(campsite)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    override fun getCampsite(id : String, onSuccess: (Campsite?) -> Unit, onFailure: (Exception) -> Unit){
        campsites.document(id).get()
            .addOnSuccessListener { document ->
            onSuccess(document.toObject(Campsite::class.java))}
            .addOnFailureListener { onFailure(it) }
    }

    override fun getCampsitesByOwner(ownerId: String, onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit) {
        campsites.whereEqualTo("ownerId",ownerId).get()
            .addOnSuccessListener { documents -> onSuccess(documents.toObjects(Campsite::class.java)) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun getAllCampsites(onSuccess: (List<Campsite>) -> Unit, onFailure: (Exception) -> Unit) {
        campsites.get()
            .addOnSuccessListener { documents -> onSuccess(documents.toObjects(Campsite::class.java)) }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    override fun removeAllCampsitesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
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
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
}