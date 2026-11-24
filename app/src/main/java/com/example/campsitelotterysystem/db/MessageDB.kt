package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.Message

/**
 * Purpose: Database access for direct messages between entrants and campsite owners.
 * Role: Reads and writes to the "messages" collection in Firestore through DBConnector.
 */
interface MessageDB {
    fun sendMessage(message: Message, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    fun getMessagesForReservation(reservationId: String, onSuccess: (List<Message>) -> Unit, onFailure: (Exception) -> Unit)
    fun getAllMessageLogs(onSuccess: (List<Message>) -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreMessageDB : MessageDB {
    private val messages = DBConnector.db.collection("messages")

    override fun sendMessage(message: Message, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val document = messages.document()
        val messageWithId = message.copy(id = document.id)
        document.set(messageWithId)
            .addOnSuccessListener{onSuccess(document.id)}
            .addOnFailureListener{onFailure(it)}
    }

    override fun getMessagesForReservation(reservationId: String, onSuccess: (List<Message>) -> Unit, onFailure: (Exception) -> Unit) {
        messages.whereEqualTo("reservationId", reservationId).get()
            .addOnSuccessListener { snapshot ->
                val thread = snapshot.toObjects(Message::class.java).sortedBy{it.timestamp}
                onSuccess(thread)
            }
            .addOnFailureListener{onFailure(it)}
    }

    override fun getAllMessageLogs(onSuccess: (List<Message>) -> Unit, onFailure: (Exception) -> Unit) {
        messages.get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.toObjects(Message::class.java).sortedByDescending{it.timestamp}
                onSuccess(logs)
            }
            .addOnFailureListener{onFailure(it)}
    }
}