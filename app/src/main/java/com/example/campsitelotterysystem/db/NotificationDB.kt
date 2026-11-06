package com.example.campsitelotterysystem.db



import com.example.campsitelotterysystem.model.Notification
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions


/**
 * Purpose : Database access for notifications, identified by device ID.
 * Role : Reads and writes to "notifications" collector through DBConnector.
 * Outstanding issues : Reading from database is not implemented.
 */
interface NotificationDB {

    fun saveIncomingNotification(recipientId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun saveOutgoingNotification(senderId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun logNotification(notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getAllNotificationLogs(onSuccess: (List<Notification>) -> Unit, onFailure: (Exception) -> Unit)
}
//The following class is adapted from Google Gemini
//"how do i convert the database date into a list if the items are grouped in a field in a single document?"
//11/5/2026
data class NotificationLogWrapper(val log: List<Notification> = emptyList())

class FirestoreNotificationDB : NotificationDB {
    private val notifications = DBConnector.db.collection("notifications")

    //The following function is adapted from Google Gemini
    //"how do i convert the database date into a list if the items are grouped in a field in a single document?"
    //11/5/2026
    override fun getAllNotificationLogs(onSuccess: (List<Notification>) -> Unit, onFailure: (Exception) -> Unit) {
        notifications.document("ALL NOTIFICATIONS").get()
            .addOnSuccessListener { document ->
                val wrapper = document.toObject(NotificationLogWrapper::class.java)
                onSuccess(wrapper?.log ?: emptyList())
            }
            .addOnFailureListener { onFailure(it) }
    }

    //The following functions are adapted from Anthropic, Claude,
    //"assume using kotlin firestore i want to append an item to the end of document if the document already exists and create the document then append to it if it does not exist yet. how do i do so",
    //11/3/2026


    override fun saveIncomingNotification(recipientId: String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)  {

        notifications.document(recipientId).set(mapOf("incoming" to FieldValue.arrayUnion(notification)), SetOptions.merge())
            .addOnSuccessListener { onSuccess }
            .addOnFailureListener { onFailure }
    }
    override fun saveOutgoingNotification(senderId : String, notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        notifications.document(senderId).set(mapOf("outgoing" to FieldValue.arrayUnion(notification)), SetOptions.merge())
            .addOnSuccessListener { onSuccess }
            .addOnFailureListener { onFailure }
    }
    override fun logNotification(notification: Notification, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        notifications.document("ALL NOTIFICATIONS").set(mapOf("log" to FieldValue.arrayUnion(notification)), SetOptions.merge())
            .addOnSuccessListener { onSuccess }
            .addOnFailureListener { onFailure }

    }

}



