package com.example.campsitelotterysystem.model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

/**
 * Represents a campsite available for users to search, browse, and reserve.
 *
 * Stores campsite information including its owner, location, capacity,
 * pricing, images, policies, tags, and available dates.
 *
 * @property id Unique identifier of the campsite.
 * @property ownerId ID of the user who owns the campsite.
 * @property name Name of the campsite.
 * @property location Physical location of the campsite.
 * @property coordinates Geographical coordinates of the campsite.
 * @property description Description of the campsite.
 * @property maximumOccupancy Maximum number of guests allowed.
 * @property pricePerNight Cost of staying one night.
 * @property averageRating Average rating given by users.
 * @property promotionalImages URLs of campsite promotional images.
 * @property rulesAndPolicy Rules and policies for the campsite.
 * @property tags Searchable tags associated with the campsite.
 * @property availableDates Dates when the campsite is available.
 * @property qrCodeData Data encoded in the campsite QR code.
 * @property published Indicates whether the campsite is published.
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
