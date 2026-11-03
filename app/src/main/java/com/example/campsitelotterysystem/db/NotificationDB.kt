package com.example.campsitelotterysystem.db



import com.example.campsitelotterysystem.model.Notification

interface NotificationDB {

    fun saveIncomingNotification(recipientId : String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun saveOutgoingNotification(recipientId : String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun logNotification(notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreNotificationDB : NotificationDB {


    override fun saveIncomingNotification(recipientId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {


    }
    override fun saveOutgoingNotification(recipientId : String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

    }
    override fun logNotification(notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

    }

}



