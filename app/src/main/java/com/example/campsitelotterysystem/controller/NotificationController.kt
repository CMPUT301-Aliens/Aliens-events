package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.DBConnector
import com.example.campsitelotterysystem.db.NotificationDB
import com.example.campsitelotterysystem.model.Notification
import com.example.campsitelotterysystem.model.User

/**
 * Purpose : Controller for notifications.
 * Role : Facilitates sending notifications, and viewing received notifications.
 * Outstanding issues : Viewing received notifications is not implemented.
 */


class NotificationController (private val notificationDB: NotificationDB){

    fun sendNotification(recipients: List<User>, senderId: String,  notification: Notification,
                         onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

        //iterates across all recipients and sends to each one after checking their notification settings
        for (user in recipients) {
            if (user.notificationsEnabled) { notificationDB.saveIncomingNotification(recipientId = user.deviceId, notification = notification, onSuccess, onFailure)}
        }
        notificationDB.saveOutgoingNotification(senderId, notification, onSuccess, onFailure)
        notificationDB.logNotification(notification, onSuccess, onFailure)
    }




}