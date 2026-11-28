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
import kotlin.random.Random
import com.example.campsitelotterysystem.model.Campsite

/** Unit tests for LotteryController's validation, saving, editing, waitlist, draw, waitlist notifications, and campsite lookups, using a fake database. */

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

        val detailsUpdated = mutableListOf<Lottery>()

        override fun updateLotteryDetails(
            lottery: Lottery,
            onSuccess: () -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            detailsUpdated.add(lottery)
            // Like Firestore's update(): only the editable fields change on the saved lottery.
            val index = added.indexOfFirst { it.id == lottery.id }
            if (index >= 0) {
                added[index] = added[index].copy(
                    campsiteIds = lottery.campsiteIds,
                    registrationStartDate = lottery.registrationStartDate,
                    registrationEndDate = lottery.registrationEndDate,
                    bookingStartDate = lottery.bookingStartDate,
                    bookingEndDate = lottery.bookingEndDate,
                    numberOfWinners = lottery.numberOfWinners,
                    waitlistCapacity = lottery.waitlistCapacity
                )
            }
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

    // Realistic test data: entrants are identified by their Android device ID (16 hex characters).
    private val avery = "5f2a9c81d3e04b76"
    private val maya = "3f2b8c1e0a7d4f65"
    private val jordan = "c81e728d9d4c2f63"
    private val sofia = "a87ff679a2f3e71d"
    private val liam = "e4da3b7fbbce2345"
    private val noProfile = "b6d767d2f8ed5d21"
    private val lotteryId = "k3XyP9qLm2RtV8wNb4Ze"

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
        userDB.users[avery] = User(deviceId = avery, name = "Avery")
        userDB.users[maya] = User(deviceId = maya, name = "Maya")
        var entrants = emptyList<User>()

        LotteryController(FakeLotteryDB(), userDB).getWaitlistEntrants(
            validLottery.copy(waitingList = listOf(maya, avery)),
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
            validLottery.copy(waitingList = listOf(noProfile)),
            onSuccess = { entrants = it },
            onFailure = { throw it }
        )

        assertEquals(1, entrants.size)
        assertEquals(noProfile, entrants[0].deviceId)
    }
    @Test
    fun pickWinnersNeverPicksMoreThanNumberOfWinners() {
        val winners = controller.pickWinners(listOf(avery, maya, jordan, sofia, liam), 3, Random(42))

        assertEquals(3, winners.size)
    }
    @Test
    fun fewerEntrantsThanWinnersPicksEveryone() {
        val winners = controller.pickWinners(listOf(avery, maya), 5, Random(42))

        assertEquals(setOf(avery, maya), winners.toSet())
    }

    @Test
    fun pickWinnersNeverPicksSameEntrantTwice() {
        val winners = controller.pickWinners(listOf(avery, avery, maya, jordan), 3, Random(42))

        assertEquals(winners.size, winners.distinct().size)
    }

    @Test
    fun winnersComeFromTheWaitingList() {
        val waitingList = listOf(avery, maya, jordan, sofia)
        val winners = controller.pickWinners(waitingList, 2, Random(42))

        assertTrue(winners.all { it in waitingList })
    }

    @Test
    fun zeroWinnersPicksNobody() {
        val winners = controller.pickWinners(listOf(avery, maya, jordan), 0, Random(42))

        assertTrue(winners.isEmpty())
    }
    @Test
    fun drawRunsAfterRegistrationEnds() {
        val db = FakeLotteryDB()
        var result: Lottery? = null

        LotteryController(db, FakeUserDB()).drawIfReady(
            validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya, jordan)),
            now = Date(2500),
            onSuccess = { result = it },
            onFailure = { throw it }
        )

        assertEquals("drawn", result?.status)
        assertEquals(2, result?.winners?.size)
        assertEquals(1, db.updated.size)
    }
    @Test
    fun drawDoesNotRunBeforeRegistrationEnds() {
        val db = FakeLotteryDB()
        var result: Lottery? = null

        LotteryController(db, FakeUserDB()).drawIfReady(
            validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya, jordan)),
            now = Date(1500),
            onSuccess = { result = it },
            onFailure = { throw it }
        )

        assertTrue(db.updated.isEmpty())
        assertEquals("open", result?.status)
    }

    @Test
    fun drawDoesNotRunTwice() {
        val db = FakeLotteryDB()

        LotteryController(db, FakeUserDB()).drawIfReady(
            validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya, jordan), status = "drawn"),
            now = Date(2500),
            onSuccess = { },
            onFailure = { throw it }
        )

        assertTrue(db.updated.isEmpty())
    }
    @Test
    fun validWaitlistMessageHasNoErrors() {
        val lottery = validLottery.copy(waitingList = listOf(avery, maya))
        val errors = controller.validateWaitlistMessage(lottery, "Fire ban starts Friday.", listOf(avery))
        assertTrue(errors.isEmpty())
    }

    @Test
    fun blankWaitlistMessageIsAnError() {
        val lottery = validLottery.copy(waitingList = listOf(avery, maya))
        val errors = controller.validateWaitlistMessage(lottery, "   ", listOf(avery))
        assertEquals(listOf("Message cannot be blank."), errors)
    }

    @Test
    fun noRecipientsIsAnError() {
        val lottery = validLottery.copy(waitingList = listOf(avery, maya))
        val errors = controller.validateWaitlistMessage(lottery, "Fire ban starts Friday.", emptyList())
        assertEquals(listOf("Select at least one entrant."), errors)
    }

    @Test
    fun recipientNotOnWaitlistIsAnError() {
        val lottery = validLottery.copy(waitingList = listOf(avery, maya))
        val errors = controller.validateWaitlistMessage(lottery, "Fire ban starts Friday.", listOf(jordan))
        assertEquals(listOf("Only entrants on the waitlist can be notified."), errors)
    }

    @Test
    fun notificationHasOwnerMessageAndLottery() {
        val lottery = validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya))
        val ownerId = "9e107d9d372bb682"
        val notification = controller.buildWaitlistNotification(
            lottery, ownerId, listOf(avery), "  Fire ban starts Friday.  ", Date(5000)
        )
        assertEquals(ownerId, notification.senderId)
        assertEquals("Fire ban starts Friday.", notification.messageContent)
        assertEquals("waitlist_message", notification.type)
        assertEquals(lotteryId, notification.lotteryId)
        assertEquals(Date(5000), notification.timestamp)
    }

    @Test
    fun notificationRemovesDuplicateRecipients() {
        val lottery = validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya))
        val notification = controller.buildWaitlistNotification(
            lottery, "9e107d9d372bb682", listOf(avery, avery, maya), "Fire ban starts Friday.", Date(5000)
        )
        assertEquals(listOf(avery, maya), notification.recipientIds)
    }

    @Test
    fun campsitesAlreadyInALotteryAreFound() {
        val db = FakeLotteryDB()
        db.added.add(validLottery.copy(campsiteIds = listOf("campsite-1", "campsite-2")))
        var taken = emptySet<String>()

        LotteryController(db, FakeUserDB()).findCampsitesInALottery(
            listOf("campsite-1", "campsite-2", "campsite-3"),
            onSuccess = { taken = it },
            onFailure = { throw it }
        )

        assertEquals(setOf("campsite-1", "campsite-2"), taken)
    }

    @Test
    fun closedLotteryDoesNotTakeACampsite() {
        val db = FakeLotteryDB()
        db.added.add(validLottery.copy(campsiteIds = listOf("campsite-1"), status = "closed"))
        var taken = setOf("not set")

        LotteryController(db, FakeUserDB()).findCampsitesInALottery(
            listOf("campsite-1"),
            onSuccess = { taken = it },
            onFailure = { throw it }
        )

        assertTrue(taken.isEmpty())
    }

    @Test
    fun noCampsitesMeansNoneAreTaken() {
        var taken = setOf("not set")

        controller.findCampsitesInALottery(
            emptyList(),
            onSuccess = { taken = it },
            onFailure = { throw it }
        )

        assertTrue(taken.isEmpty())
    }


    @Test
    fun campsiteNamesComeInLotteryOrder() {
        val campsites = listOf(
            Campsite(id = "campsite-1", name = "Lake Louise Pine Camp"),
            Campsite(id = "campsite-2", name = "Valley View Ridge")
        )
        val names = controller.campsiteNames(listOf("campsite-2", "campsite-1"), campsites)
        assertEquals(listOf("Valley View Ridge", "Lake Louise Pine Camp"), names)
    }

    @Test
    fun unknownCampsiteShowsItsId() {
        val campsites = listOf(Campsite(id = "campsite-1", name = "Lake Louise Pine Camp"))
        val names = controller.campsiteNames(listOf("campsite-1", "campsite-9"), campsites)
        assertEquals(listOf("Lake Louise Pine Camp", "campsite-9"), names)
    }

    @Test
    fun editKeepsTheWaitlistAndId() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya))
        val changes = validLottery.copy(numberOfWinners = 1, registrationEndDate = Date(2500))
        var result: Lottery? = null

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, changes, Date(1500),
            onSuccess = { result = it },
            onInvalid = { throw AssertionError("should be valid: $it") },
            onFailure = { throw it }
        )

        assertEquals(lotteryId, db.detailsUpdated[0].id)
        assertEquals(listOf(avery, maya), db.detailsUpdated[0].waitingList)
        assertEquals(1, db.detailsUpdated[0].numberOfWinners)
        assertEquals(Date(2500), db.detailsUpdated[0].registrationEndDate)
        assertEquals(db.detailsUpdated[0], result)
    }

    @Test
    fun editAfterRegistrationEndsIsRejected() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId)
        var errors = emptyList<String>()

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(numberOfWinners = 5), Date(2500),
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            onFailure = { throw it }
        )

        assertEquals(listOf("This lottery can't be changed after registration ends."), errors)
        assertTrue(db.detailsUpdated.isEmpty())
    }

    @Test
    fun editCannotMoveRegistrationEndIntoThePast() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId)
        var errors = emptyList<String>()

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(registrationEndDate = Date(1200)), Date(1500),
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            onFailure = { throw it }
        )

        assertEquals(listOf("Registration end can't be in the past."), errors)
        assertTrue(db.detailsUpdated.isEmpty())
    }

    @Test
    fun invalidEditIsRejected() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId)
        var errors = emptyList<String>()

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(numberOfWinners = 0), Date(1500),
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            onFailure = { throw it }
        )

        assertEquals(listOf("Number of winners must be greater than zero."), errors)
        assertTrue(db.detailsUpdated.isEmpty())
    }

    @Test
    fun editDoesNotRemoveSomeoneWhoJoinedMeanwhile() {
        val db = FakeLotteryDB()
        // The owner opened the edit form when only Avery and Maya had joined...
        val saved = validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya))
        // ...but Jordan joined before the owner pressed Save Changes.
        db.added.add(saved.copy(waitingList = listOf(avery, maya, jordan)))

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(numberOfWinners = 1), Date(1500),
            onSuccess = {},
            onInvalid = { throw AssertionError("should be valid: $it") },
            onFailure = { throw it }
        )

        assertEquals(listOf(avery, maya, jordan), db.added[0].waitingList)
        assertEquals(1, db.added[0].numberOfWinners)
    }

    @Test
    fun lotteryWithNoCapacityIsValid() {
        assertTrue(controller.validateLottery(validLottery.copy(waitlistCapacity = null)).isEmpty())
    }

    @Test
    fun zeroCapacityIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(waitlistCapacity = 0))
        assertEquals(listOf("Waitlist capacity must be greater than zero."), errors)
    }

    @Test
    fun negativeCapacityIsAnError() {
        val errors = controller.validateLottery(validLottery.copy(waitlistCapacity = -5))
        assertEquals(listOf("Waitlist capacity must be greater than zero."), errors)
    }

    @Test
    fun editCanChangeCapacity() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya))
        db.added.add(saved)

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(waitlistCapacity = 5), Date(1500),
            onSuccess = {},
            onInvalid = { throw AssertionError("should be valid: $it") },
            onFailure = { throw it }
        )

        assertEquals(5, db.added[0].waitlistCapacity)
        assertEquals(listOf(avery, maya), db.added[0].waitingList)
    }

    @Test
    fun editCanRemoveCapacity() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId, waitlistCapacity = 10)
        db.added.add(saved)

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(waitlistCapacity = null), Date(1500),
            onSuccess = {},
            onInvalid = { throw AssertionError("should be valid: $it") },
            onFailure = { throw it }
        )

        assertNull(db.added[0].waitlistCapacity)
    }

    @Test
    fun editCannotSetCapacityBelowWaitlist() {
        val db = FakeLotteryDB()
        val saved = validLottery.copy(id = lotteryId, waitingList = listOf(avery, maya, jordan))
        var errors = emptyList<String>()

        LotteryController(db, FakeUserDB()).editLotteryListing(
            saved, saved.copy(waitlistCapacity = 2), Date(1500),
            onSuccess = { throw AssertionError("should not save") },
            onInvalid = { errors = it },
            onFailure = { throw it }
        )

        assertEquals(
            listOf("Waitlist capacity can't be less than the number of people already on the waitlist (3)."),
            errors
        )
        assertTrue(db.detailsUpdated.isEmpty())
    }
}