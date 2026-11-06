package com.example.campsitelotterysystem.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

/**
 * Purpose : Data model for one campsite listed by owner.
 * Role : Stored in the campsites Firestore collection.
 * Outstanding issues : qrCode , ratings and available dates are yet to be implemented in final checkpoint.
 */

data class Campsite(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val location: String = "",
    val coordinates: GeoPoint? = null,
    val description: String = "",
    val maximumOccupancy: Int = 0,
    val pricePerNight: Double = 0.0,
    val averageRating: Double = 0.0,
    val promotionalImages: List<String> = emptyList<String>(),
    val rulesAndPolicy: String = "",
    val tags: List<String> = emptyList<String>(),
    val availableDates: List<Date> = emptyList<Date>(),
    val qrCodeData: String = "",
    val published: Boolean = false
)
