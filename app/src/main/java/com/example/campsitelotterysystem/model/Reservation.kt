package com.example.campsitelotterysystem.model

import java.util.Date

/**
 * Purpose: Represent a campsite reservation made by an entrant (US 01.05.01)
 * Role: Data model stored in Firestore and displayed in confirmation popups and booking lists
 */
data class Reservation(
    val id: String = "",
    val entrantId: String = "",
    val ownerId: String = "",
    val campsiteId: String = "",
    val campsiteName: String = "",
    val campsiteLocation: String = "",
    val reservedDates: List<Date> = emptyList(),
    val numberOfGuests: Int = 1,
    val totalCost: Double = 0.0,
    val status: String = "active", // "active", "past", "cancelled", "rejected"
    val rulesAccepted: Boolean = false,
    val refundStatus: String = "none", // "none", "pending", "refunded"
    val createdAt: Date = Date()
)