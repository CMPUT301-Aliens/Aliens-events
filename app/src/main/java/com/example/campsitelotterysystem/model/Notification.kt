package com.example.campsitelotterysystem.model

import java.util.Date

data class Notification(
    val id: String = "",
    val timestamp: Date = Date(),
    val senderId: String = "",
    val recipientId: String = "",
    val messageContent: String = "",
    val type: String = "" // e.g., "won", "lost", "lottery_loss", "campsite_update", "cancellation"
)