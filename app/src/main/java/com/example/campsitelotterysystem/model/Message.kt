package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Purpose: Data model for a direct message between an entrant and campsite owner.
 * Role: Stored in the "messages" Firestore collection and displayed in MessageActivity and AdminLogsScreen.
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val reservationId: String = "",
    val content: String = "",
    val timestamp: Date = Date()
)