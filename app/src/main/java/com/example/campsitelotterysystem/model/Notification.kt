package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Purpose : Data model for a notification.
 * Role : Stored in the "Notifications" Firestore collection, categorized by sender and recipient.
 */
data class Notification(
    val id: String = "",
    val timestamp: Date = Date(),
    val senderId: String = "",  // either "system", or campsite owner's unique device ID
    val recipientIds: List<String> = listOf(""),
    val messageContent: String = "",
    val type: String = "", // e.g., "won", "lost", "lottery_loss", "campsite_update", "cancellation"
    val lotteryId: String = ""
)