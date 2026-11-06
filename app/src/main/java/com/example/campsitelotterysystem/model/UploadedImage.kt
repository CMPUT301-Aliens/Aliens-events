package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Purpose: holds information about an uploaded image.
 * Role: structure of image data in Firestore
 * Outstanding Issues: None
 */

/**
 * An uploaded image entity.
 *
 * Stores metadata and location information for an image file in Firestore.
 *
 * @property id Unique identifier of the image document in Firestore.
 * @property imageUrl URL of the uploaded image.
 * @property uploaderId Device or User ID of the uploader.
 * @property timestamp Date and time when the image was uploaded.
 * @property sourceId Associated campsite or entity ID related to the image.
 * @property category Category tag for the image (defaults to "campsite").
 */
data class UploadedImage(
    val id: String = "",
    val imageUrl: String = "",
    val uploaderId: String = "",
    val timestamp: Date = Date(),
    val sourceId: String = "",
    val category: String = "campsite"
)
