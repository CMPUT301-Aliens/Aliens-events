package com.example.campsitelotterysystem.model

/**
 * Purpose : Data model for an app user, identified by device ID.
 * Role : Stored in the "users" Firestore collection (document ID = deviceId).
 * Outstanding issues : profileImageUrl is not used until image upload is built.
 */
data class User(
    val deviceId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val notificationsEnabled: Boolean = true,
    val role: String = "entrant" // "entrant", "owner" or "admin"
)