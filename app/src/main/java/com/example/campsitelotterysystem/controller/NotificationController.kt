package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.NotificationDB
import com.example.campsitelotterysystem.model.Notification


class NotificationController (private val notificationDB: NotificationDB){
    private val receivedNotifications = mutableMapOf<String, MutableList<Notification>>()
    private val sentNotifications = mutableMapOf<String, MutableList<Notification>>()

    fun sendNotification(recipientIds: List<String>, senderId: String,  notification: Notification,
                         onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        //iterates across all recipients and sends to each one after checking their notification settings
        for (id in recipientIds) {
            notificationDB.saveIncomingNotification(recipientId = id, notification = notification, onSuccess, onFailure)
        }
        notificationDB.saveOutgoingNotification(senderId, notification, onSuccess, onFailure)
        notificationDB.logNotification(notification, onSuccess, onFailure)
    }


}