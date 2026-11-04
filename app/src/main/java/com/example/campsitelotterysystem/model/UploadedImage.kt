package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Purpose : Data model for an image uploaded within the app.
 * Role : Stored in the "images" Firestore collection, or aggregated from campsite/user promotional images.
 */
data class UploadedImage(
    val id: String = "",
    val imageUrl: String = "",
    val uploaderId: String = "",
    val timestamp: Date = Date(),
    val sourceId: String = "",
    val category: String = "campsite"
)
