package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.db.NotificationDB
import com.example.campsitelotterysystem.db.ReservationDB
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.model.Notification
import com.example.campsitelotterysystem.model.Reservation
import com.example.campsitelotterysystem.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * Purpose: Unit tests for ReservationController validation, cost calculation, persistence, and confirmation notifications.
 * Role: Uses fake DB implementations instead of Firestore to verify reservation confirmation logic offline.
 * Outstanding issues: none
 */

class ReservationControllerTest {

    private class FakeReservationDB : ReservationDB {
        val saved = mutableListOf<Reservation>()

        override fun addReservation(
            reservation: Reservation,
            onSuccess: (String) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            val id = "res-${saved.size + 1}"
            saved.add(reservation.copy(id = id))
            onSuccess(id)
        }

        override fun getReservationsByEntrant(
            entrantId: String,
            onSuccess: (List<Reservation>) -> Unit,
            onFailure: (Exception) -> Unit
        ) = onSuccess(saved.filter { it.entrantId == entrantId })

        override fun getReservationsByOwner(
            ownerId: String,
            onSuccess: (List<Reservation>) -> Unit,
            onFailure: (Exception) -> Unit
        ) = onSuccess(saved.filter { it.ownerId == ownerId })
    }

    private class FakeNotificationDB : NotificationDB {
        val incoming = mutableListOf<Pair<String, Notification>>()
        val outgoing = mutableListOf<Pair<String, Notification>>()
        val sent = mutableListOf<Notification>()

        override fun saveIncomingNotification(
            recipientId: String,
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            incoming.add(recipientId to notification)
            onSuccess()
        }

        override fun saveOutgoingNotification(
            senderId: String,
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            outgoing.add(senderId to notification)
            onSuccess()
        }

        override fun logNotification(
            notification: Notification,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            sent.add(notification)
            onSuccess()
        }

        override fun getIncomingNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) = onSuccess(emptyList())

        override fun getOutgoingNotifications(
            userId: String,
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) = onSuccess(emptyList())

        override fun getAllNotificationLogs(
            onSuccess: (List<Notification>) -> Unit,
            onFailure: (Exception) -> Unit
        ) = onSuccess(sent)
    }

    private class FakeUserDB(private val user: User?) : UserDB {
        override fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) = onSuccess(user)
        override fun getOrCreateUser(deviceId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun updateProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun setNotificationPreference(userId: String, enabled: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun deleteProfile(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun deleteOwnerProfile(ownerId: String, campsiteDB: CampsiteDB, lotteryDB: LotteryDB, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {}
    }

    @Test
    fun confirmReservationSavesCalculatesCostAndNotifies() {
        val resDB = FakeReservationDB()
        val notifDB = FakeNotificationDB()
        val entrant = User(deviceId = "user-1", notificationsEnabled = true)
        val controller = ReservationController(
            reservationDB = resDB,
            notificationController = NotificationController(notifDB),
            userDB = FakeUserDB(entrant)
        )

        val checkIn = Date(1_000_000L)
        val checkOut = Date(1_000_000L + 2 * 86_400_000L)
        var confirmed: Reservation? = null

        controller.confirmReservation(
            reservation = Reservation(
                entrantId = "user-1",
                ownerId = "owner-1",
                campsiteId = "camp-1",
                campsiteName = "Banff Pine Ridge",
                campsiteLocation = "Banff, AB",
                reservedDates = listOf(checkIn, checkOut),
                numberOfGuests = 2,
                rulesAccepted = true
            ),
            pricePerNight = 50.0,
            onSuccess = { confirmed = it },
            onInvalid = { throw AssertionError("Expected valid reservation, got: $it") },
            onFailure = { throw it }
        )

        assertEquals("res-1", confirmed?.id)
        assertEquals("active", confirmed?.status)
        assertEquals(50.0, confirmed?.totalCost ?: 0.0, 0.01)
        assertEquals(1, resDB.saved.size)
        assertEquals(1, notifDB.sent.size)
    }

    @Test
    fun confirmReservationRejectsWhenRulesNotAccepted() {
        val controller = ReservationController(
            reservationDB = FakeReservationDB(),
            notificationController = NotificationController(FakeNotificationDB()),
            userDB = FakeUserDB(null)
        )

        var validationErrors: List<String> = emptyList()
        controller.confirmReservation(
            reservation = Reservation(
                entrantId = "user-1",
                campsiteId = "camp-1",
                reservedDates = listOf(Date()),
                numberOfGuests = 2,
                rulesAccepted = false
            ),
            onSuccess = { throw AssertionError("Should not confirm without accepting rules") },
            onInvalid = { validationErrors = it },
            onFailure = { throw it }
        )

        assertTrue(validationErrors.any { it.contains("accept the campsite rules") })
    }
}