package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.MessageDB
import com.example.campsitelotterysystem.model.Message
import java.util.Date

/**
 * Purpose: Controller for direct messages between entrants and campsite owners.
 * Role: Validates messages, sends them to MessageDB, and retrieves conversation threads or full admin logs.
 */
class MessageController(private val messageDB: MessageDB){
    fun sendMessage(
        senderId: String,
        recipientId: String,
        reservationId: String,
        content: String,
        onSuccess: (String) -> Unit,
        onInvalid: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        val trimmedContent = content.trim()

        if (trimmedContent.isEmpty()){
            onInvalid("Message cannot be empty.")
            return
        }
        if (senderId.isBlank() || recipientId.isBlank()){
            onInvalid("Both sender and recipient are required.")
            return
        }
        if (reservationId.isBlank()){
            onInvalid("A valid reservation is required to send a message.")
            return
        }
        val message = Message(senderId = senderId, recipientId = recipientId, reservationId = reservationId, content = trimmedContent, timestamp = Date())
        messageDB.sendMessage(message, onSuccess, onFailure)
    }

    fun getMessagesForReservation(
        reservationId: String,
        onSuccess: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        messageDB.getMessagesForReservation(reservationId, onSuccess, onFailure)
    }

    fun getAllMessageLogs(
        onSuccess: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ){
        messageDB.getAllMessageLogs(onSuccess, onFailure)
    }
}