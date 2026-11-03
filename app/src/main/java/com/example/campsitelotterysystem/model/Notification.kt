package com.example.campsitelotterysystem.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Notification(
    val senderId: String = "",
    val recipientsIds: List<String> = listOf(),
    val text: String = "",
    val notificationType: String = "",
    val timestamp: Long = Date().time,


)