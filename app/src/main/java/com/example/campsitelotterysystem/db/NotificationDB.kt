package com.example.campsitelotterysystem.db



import com.example.campsitelotterysystem.model.Notification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

interface NotificationDB {

    fun saveIncomingNotification(recipientId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun saveOutgoingNotification(senderId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun logNotification(notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreNotificationDB : NotificationDB {
    private val notifications = DBConnector.db.collection("notifications")

    //The following functions are adapted from Anthropic, Claude,
    // "assume using kotlin firestore i want to append an item to the end of document if the document already exists and create the document then append to it if it does not exist yet. how do i do so",
    // 11/3/2025
    override fun saveIncomingNotification(recipientId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)  {
        notifications.document(recipientId).set(mapOf("incoming" to FieldValue.arrayUnion(notifications)), SetOptions.merge())
    }
    override fun saveOutgoingNotification(senderId : String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        notifications.document(senderId).set(mapOf("outgoing" to FieldValue.arrayUnion(notifications)), SetOptions.merge())
    }
    override fun logNotification(notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        notifications.document("ALL NOTIFICATIONS").set(mapOf("log" to FieldValue.arrayUnion(notifications)), SetOptions.merge())

    }

}



