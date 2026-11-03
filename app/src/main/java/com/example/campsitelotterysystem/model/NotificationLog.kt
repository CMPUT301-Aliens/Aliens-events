package com.example.campsitelotterysystem.model

import java.util.Date


class NotificationLog(
    id: String,
    timestamp: Date,
    senderId: String,
    recipientId: String,
    messageContent: String,
    type: String  //("lottery_win", "campsite_update", "cancellation")
)
