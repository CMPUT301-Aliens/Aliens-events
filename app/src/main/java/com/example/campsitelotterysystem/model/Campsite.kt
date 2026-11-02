package com.example.campsitelotterysystem.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

/**
 * Campsite (model): holds one campsite's data. Never talks to Firestore.
 *
 * FLOW: Screen builds it → Controller checks it → CampsiteDB saves it → Firestore "campsites"
 *
 * TODO
 * - Fields (each with a type + default value, so Firestore can read it back):
 *   id, ownerId, name, location, coordinates (GeoPoint), description, maximumOccupancy,
 *   pricePerNight, averageRating, promotionalImages, rulesAndPolicy, tags, availableDates,
 *   qrCodeData, published
 * - id → set by CampsiteDB | ownerId → set by Controller
 * - KDoc on fields
 *
 * NOT MINE: averageRating (final), qrCodeData (final). Keep the fields, don't build features.
*/


data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val campsiteName: String = "",
    val location: String = "",
    val coordinates: GeoPoint? = null,
    val description: String = "",
    val maximumOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val averageRating : Double = 0.0,
    val promotionalImages: List<String> = emptyList<String>(),
    val rulesAndPolicy: String = "",
    val tags: List<String> = emptyList<String>(),
    val availableDates: List<Date> = emptyList<Date>(),
    val qrCodeData: String = "",
    val published: Boolean = false
)
