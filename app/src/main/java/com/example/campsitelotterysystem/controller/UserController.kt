package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.db.UserDB

class UserController(
    private val userDB: UserDB,
    private val campsiteDB: CampsiteDB
) {
    fun deleteOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        userDB.deleteOwnerProfile(ownerId, campsiteDB, onSuccess, onFailure)
    }
}
