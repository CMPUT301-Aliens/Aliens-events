package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.model.User

class UserController(
    private val userDB: UserDB,
    private val campsiteDB: CampsiteDB,
    private val lotteryDB: LotteryDB,
    private val lotteryController: LotteryController,
    private val campsiteController: CampsiteController
) {
    fun deleteOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        userDB.deleteOwnerProfile(ownerId, campsiteDB, lotteryDB, onSuccess, onFailure)
    }

    fun getAllProfiles(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        userDB.getAllUsers(onSuccess, onFailure)
    }

    fun removeProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Remove user from all waitlists (method to be provided by the other team)
        lotteryController.removeUserFromWaitlists(user.deviceId)
        // If the user is an owner, handle their campsites
        if (user.role == "owner") {
            deleteOwner(user.deviceId, onSuccess, onFailure)
        } else {
            //delete the profile document from Firestore
            userDB.deleteProfile(user.deviceId, onSuccess, onFailure)
        }
    }
}
