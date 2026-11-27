package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.FirestoreNotificationDB
import com.example.campsitelotterysystem.db.FirestoreReservationDB
import com.example.campsitelotterysystem.db.FirestoreUserDB
import com.example.campsitelotterysystem.db.ReservationDB
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.model.Notification
import com.example.campsitelotterysystem.model.Reservation
import java.util.Date
import java.util.UUID

/**
 * Purpose: Validates, calculates cost, confirms campsite reservations, and sends confirmation notifications (US 01.05.01).
 * Role: Mediates between the booking UI, ReservationDB, and NotificationController.
 * Outstanding issues: Date availability conflict checking and CSV export are handled by other team members' tasks.
 */

class ReservationController(
    private val reservationDB: ReservationDB = FirestoreReservationDB(),
    private val notificationController: NotificationController = NotificationController(FirestoreNotificationDB()),
    private val userDB: UserDB = FirestoreUserDB()
) {
    fun validateReservation(reservation: Reservation): List<String> {
        val errors = mutableListOf<String>()
        if (reservation.entrantId.isBlank()) errors.add("Entrant ID is required.")
        if (reservation.campsiteId.isBlank()) errors.add("Campsite ID is required.")
        if (reservation.reservedDates.isEmpty()) errors.add("Please select check-in and check-out dates.")
        if (reservation.numberOfGuests <= 0) errors.add("Number of guests must be at least 1.")
        if (!reservation.rulesAccepted) errors.add("You must accept the campsite rules before confirming.")
        return errors
    }

    /**
     * US 01.05.01, confirms and saves a reservation, triggers a confirmation notification,
     * and returns the confirmed reservation containing the booking ID for the popup
     */
    fun confirmReservation(
        reservation: Reservation,
        pricePerNight: Double = 0.0,
        onSuccess: (Reservation) -> Unit,
        onInvalid: (List<String>) -> Unit = {},
        onFailure: (Exception) -> Unit
    ) {
        val errors = validateReservation(reservation)
        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }

        val nights = maxOf(1, reservation.reservedDates.size - 1)
        val computedCost = if (reservation.totalCost > 0.0) {
            reservation.totalCost
        } else {
            nights * pricePerNight
        }

        val confirmed = reservation.copy(
            status = "active",
            totalCost = computedCost
        )

        reservationDB.addReservation(
            confirmed,
            onSuccess = { savedId ->
                val finalReservation = confirmed.copy(id = savedId)
                sendReservationConfirmationNotification(finalReservation)
                onSuccess(finalReservation)
            },
            onFailure = onFailure
        )
    }

    private fun sendReservationConfirmationNotification(reservation: Reservation) {
        userDB.getUserById(
            reservation.entrantId,
            onSuccess = { entrant ->
                if (entrant != null) {
                    val label = reservation.campsiteName.ifBlank { reservation.campsiteId }
                    val notification = Notification(
                        id = UUID.randomUUID().toString(),
                        timestamp = Date(),
                        senderId = "system",
                        recipientIds = listOf(entrant.deviceId),
                        messageContent = "Reservation confirmed for $label (Booking ID: ${reservation.id}).",
                        type = "reservation_confirmation"
                    )
                    notificationController.sendNotification(
                        recipients = listOf(entrant),
                        senderId = "system",
                        notification = notification,
                        onSuccess = {},
                        onFailure = {}
                    )
                }
            },
            onFailure = {}
        )
    }
}