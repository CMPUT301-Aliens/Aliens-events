package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import org.junit.Assert.assertNull
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.db.CampsiteDB
import com.example.campsitelotterysystem.model.User


/** Unit tests for LotteryController's validation and saving, using a fake database. */
class LotteryControllerTest {

    /** A pretend database that remembers what it was asked to save. */
    private class FakeLotteryDB : LotteryDB {
        val added = mutableListOf<Lottery>()
        val updated = mutableListOf<Lottery>()

        override fun addLottery(
            lottery: Lottery,
            onSuccess: (String) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            added.add(lottery)
            onSuccess("fake-id")
        }

        override fun updateLottery(
            lottery: Lottery,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            updated.add(lottery)
            onSuccess()
        }
        override fun removeAllLotteriesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess()
        }
        override fun getLotteriesWithUserInWaitlist(userId: String, onSuccess: (List<Lottery>) -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess(emptyList())
        }
        override fun getLotteryForCampsite(campsiteId: String, onSuccess: (Lottery?) -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess(added.firstOrNull { campsiteId in it.campsiteIds && it.status != "closed" })
        }
    }
    private class FakeUserDB : UserDB {
        val users = mutableMapOf<String, User>()

        override fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess(users[userId])
        }

        override fun getOrCreateUser(deviceId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun updateProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun setNotificationPreference(userId: String, enabled: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun deleteProfile(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun deleteOwnerProfile(ownerId: String, campsiteDB: CampsiteDB, lotteryDB: LotteryDB, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {}
    }

    private val validLottery = Lottery(
        campsiteIds = listOf("campsite-1"),
        numberOfWinners = 2,
        registrationStartDate = Date(1000),
        registrationEndDate = Date(2000),
        bookingStartDate = Date(3000),
        bookingEndDate = Date(4000)
    )

    private val controller = LotteryController(FakeLotteryDB(),FakeUserDB())

    @Test
    fun validLotteryHasNoErrors() {
        assertTrue(controller.validateLottery(validLottery).isEmpty())
    }

    @Test
    fun noCampsiteIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(campsiteIds = emptyList()))
        assertEquals(listOf("Choose at least one campsite."), errors)
    }

    @Test
    fun zeroWinnersIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(numberOfWinners = 0))
        assertEquals(listOf("Number of winners must be greater than zero."), errors)
    }

    @Test
    fun registrationEndBeforeStartIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(registrationEndDate = Date(500)))
        assertTrue(errors.contains("Registration end must be after registration start."))
    }

    @Test
    fun createSavesValidLottery() {
        val db = FakeLotteryDB()
        var savedId = ""

        LotteryController(db, FakeUserDB()).createLotteryListing(
            validLottery,
            onSuccess = { id -> savedId = id },
            onInvalid = { throw AssertionError("should be valid") },
            onFailure = { throw it }
        )

        assertEquals("fake-id", savedId)
        assertEquals(1, db.added.size)
    }

    @Test
    fun createDoesNotSaveInvalidLottery() {
        val db = FakeLotteryDB()
        var errors = emptyList<String>()

        LotteryController(db, FakeUserDB()).createLotteryListing(
            validLottery.copy(numberOfWinners = 0),
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            onFailure = { throw it }
        )

        assertEquals(0, db.added.size)
        assertEquals(1, errors.size)
    }

    @Test
    fun updateNeedsAnId() {
        val db = FakeLotteryDB()
        var errors = emptyList<String>()

        LotteryController(db, FakeUserDB()).updateLotteryListing(
            validLottery,
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            onFailure = { throw it }
        )

        assertEquals(listOf("This lottery has no ID."), errors)
        assertEquals(0, db.updated.size)
    }

    @Test
    fun bookingStartBeforeRegistrationEndIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(bookingStartDate = Date(1500)))
        assertEquals(listOf("Booking start must be after registration end."), errors)
    }

    @Test
    fun bookingEndBeforeStartIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(bookingEndDate = Date(2500)))
        assertEquals(listOf("Booking end must be after booking start."), errors)
    }
    @Test
    fun getLotteryForCampsiteFindsSavedLottery() {
        val db = FakeLotteryDB()
        db.added.add(validLottery)
        var found: Lottery? = null

        LotteryController(db, FakeUserDB()).getLotteryForCampsite(
            "campsite-1",
            onSuccess = { found = it },
            onFailure = { throw it }
        )

        assertEquals(validLottery, found)
    }

    @Test
    fun getLotteryForCampsiteReturnsNullWhenNone(){
        val db = FakeLotteryDB()
        var found: Lottery? = null

        LotteryController(db, FakeUserDB()).getLotteryForCampsite(
            "campsite-1",
            onSuccess = { found = it },
            onFailure = { throw it }
        )
        assertNull(found)
    }
    @Test
    fun waitlistEntrantsComeBackInOrderWithNames() {
        val userDB = FakeUserDB()
        userDB.users["user-1"] = User(deviceId = "user-1", name = "Avery")
        userDB.users["user-2"] = User(deviceId = "user-2", name = "Maya")
        var entrants = emptyList<User>()

        LotteryController(FakeLotteryDB(), userDB).getWaitlistEntrants(
            validLottery.copy(waitingList = listOf("user-2", "user-1")),
            onSuccess = { entrants = it },
            onFailure = { throw it }
        )

        assertEquals(listOf("Maya", "Avery"), entrants.map { it.name })
    }
    @Test
    fun emptyWaitlistGivesNoEntrants() {
        val userDB = FakeUserDB()

        var entrants = emptyList<User>()

        LotteryController(FakeLotteryDB(), userDB).getWaitlistEntrants(
            validLottery,
            onSuccess = { entrants = it },
            onFailure = { throw it }
        )

        assertTrue(entrants.isEmpty())
    }
    @Test
    fun entrantWithNoProfileKeepsDeviceId() {
        val userDB = FakeUserDB()
        var entrants = emptyList<User>()
        LotteryController(FakeLotteryDB(), userDB).getWaitlistEntrants(
            validLottery.copy(waitingList = listOf("ghost")),
            onSuccess = { entrants = it },
            onFailure = { throw it }
        )

        assertEquals(1, entrants.size)
        assertEquals("ghost", entrants[0].deviceId)
    }



}