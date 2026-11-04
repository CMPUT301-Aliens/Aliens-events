package com.example.campsitelotterysystem.db

import android.content.Context
import android.provider.Settings
import com.example.campsitelotterysystem.model.User

/**
 * Purpose : Database access for user profiles.
 * Role : Reads and writes the "users" collection through DBConnector.
 * Outstanding issues : deleteProfile still needs removeUserFromWaitlists(written on deleteprofile function)
 */
interface UserDB {
    fun getOrCreateUser(deviceId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)
    fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit)
    fun updateProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun setNotificationPreference(userId: String, enabled: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun deleteProfile(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun deleteOwnerProfile(
        ownerId: String,
        campsiteDB: CampsiteDB,
        lotteryDB: LotteryDB,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
}

/** Returns this device's ID, used as the user ID everywhere in the app. */
fun getCurrentUserId(context: Context): String =
    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

class FirestoreUserDB : UserDB {
    private val users = DBConnector.db.collection("users")

    override fun getOrCreateUser(deviceId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        users.document(deviceId).get()
            .addOnSuccessListener { doc ->
                val existing = doc.toObject(User::class.java)
                if (existing != null) {
                    onSuccess(existing)
                } else {
                    val newUser = User(deviceId = deviceId)
                    users.document(deviceId).set(newUser)
                        .addOnSuccessListener { onSuccess(newUser) }
                        .addOnFailureListener { onFailure(it) }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    override fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        users.document(userId).get()
            .addOnSuccessListener { onSuccess(it.toObject(User::class.java)) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun updateProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        users.document(user.deviceId).set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    override fun setNotificationPreference(userId: String, enabled: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        users.document(userId).update("notificationsEnabled", enabled)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    override fun deleteProfile(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        //  TODO: call removeUserFromWaitlists(userId) first, once the lottery side provides it.
        users.document(userId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    override fun deleteOwnerProfile(
        ownerId: String,
        campsiteDB: CampsiteDB,
        lotteryDB: LotteryDB,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        campsiteDB.removeAllCampsitesByOwner(ownerId,
            onSuccess = {
                lotteryDB.removeAllLotteriesByOwner(ownerId,
                    onSuccess = {
                        deleteProfile(ownerId, onSuccess, onFailure)
                    },
                    onFailure = { onFailure(it) }
                )
            },
            onFailure = { onFailure(it) }
        )
    }
}