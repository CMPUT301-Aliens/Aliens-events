package com.example.campsitelotterysystem.model

import java.time.Instant

class NotificationLog(
    val id: String = "",
    val timestamp: Instant = Instant.now(),
    val senderId: String = "",
    val recipientId: String = "",
    val messageContent: String = "",
    val type: String = ""  //("lottery_win", "campsite_update", "cancellation")
)
