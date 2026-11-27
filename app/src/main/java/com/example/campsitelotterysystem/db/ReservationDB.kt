package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.Reservation
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Purpose: Database interface and Firestore implementation for campsite reservations.
 * Role: Persists and retrieves Reservation documents in the "reservations" Firestore collection.
 * Outstanding issues: none
 */
interface ReservationDB{
    fun addReservation(
        reservation: Reservation,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    )
    fun getReservationsByEntrant(
        entrantId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    )
    fun getReservationsByOwner(
        ownerId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    )
}

class FirestoreReservationDB(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReservationDB {

    private val collection = db.collection("reservations")

    override fun addReservation(
        reservation: Reservation,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val docRef = if (reservation.id.isNotBlank()) {
            collection.document(reservation.id)
        } else {
            collection.document()
        }
        val toSave = reservation.copy(id = docRef.id)
        docRef.set(toSave)
            .addOnSuccessListener { onSuccess(docRef.id) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun getReservationsByEntrant(
        entrantId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        collection.whereEqualTo("entrantId", entrantId)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObjects(Reservation::class.java))
            }
            .addOnFailureListener { onFailure(it) }
    }

    override fun getReservationsByOwner(
        ownerId: String,
        onSuccess: (List<Reservation>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        collection.whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { snapshot ->
                onSuccess(snapshot.toObjects(Reservation::class.java))
            }
            .addOnFailureListener { onFailure(it) }
    }
}