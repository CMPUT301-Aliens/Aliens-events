package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.*
import com.example.campsitelotterysystem.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserControllerTest {

    private class FakeUserDB : UserDB {
        var users = listOf<User>()
        val deletedUserIds = mutableListOf<String>()
        var ownerProfileDeleted = false

        override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
            onSuccess(users)
        }

        override fun deleteProfile(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            deletedUserIds.add(userId)
            onSuccess()
        }

        override fun deleteOwnerProfile(ownerId: String, campsiteDB: CampsiteDB, lotteryDB: LotteryDB, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
            ownerProfileDeleted = true
            onSuccess()
        }

        //currently untested methods in the interface
        override fun getOrCreateUser(deviceId: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun updateProfile(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun setNotificationPreference(userId: String, enabled: Boolean, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
    }

    // fake CampsiteDB to fill UserController's constructor parameters
    private val dummyCampsiteDB = object : CampsiteDB {
        override fun addCampsite(campsite: com.example.campsitelotterysystem.model.Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun removeCampsite(campsite: com.example.campsitelotterysystem.model.Campsite, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun updateCampsite(campsite: com.example.campsitelotterysystem.model.Campsite, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getCampsite(id: String, onSuccess: (com.example.campsitelotterysystem.model.Campsite?) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getCampsitesByOwner(ownerId: String, onSuccess: (List<com.example.campsitelotterysystem.model.Campsite>) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getAllCampsites(onSuccess: (List<com.example.campsitelotterysystem.model.Campsite>) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun removeAllCampsitesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
    }

    // fake LotteryDB to fill UserController's constructor parameters
    private val dummyLotteryDB = object : LotteryDB {
        override fun addLottery(lottery: com.example.campsitelotterysystem.model.Lottery, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun updateLottery(lottery: com.example.campsitelotterysystem.model.Lottery, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun removeAllLotteriesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getLotteriesWithUserInWaitlist(userId: String, onSuccess: (List<com.example.campsitelotterysystem.model.Lottery>) -> Unit, onFailure: (Exception) -> Unit) {}
        override fun getLotteryForCampsite(campsiteId: String, onSuccess: (com.example.campsitelotterysystem.model.Lottery?) -> Unit, onFailure: (Exception) -> Unit) {}
    }

    @Test
    fun getAllProfilesReturnsList() {
        val fakeDb = FakeUserDB()
        val controller = UserController(fakeDb, dummyCampsiteDB, dummyLotteryDB, LotteryController(dummyLotteryDB, fakeDb), CampsiteController(dummyCampsiteDB))

        fakeDb.users = listOf(
            User(deviceId = "123", role = "entrant"),
            User(deviceId = "456", role = "owner")
        )

        var fetchedUsers: List<User>? = null
        controller.getAllProfiles(
            onSuccess = { fetchedUsers = it },
            onFailure = { throw it }
        )

        assertEquals(2, fetchedUsers?.size)
    }

    @Test
    fun removeProfileDeletesEntrantNormally() {
        val fakeDb = FakeUserDB()
        val controller = UserController(fakeDb, dummyCampsiteDB, dummyLotteryDB, LotteryController(dummyLotteryDB, fakeDb), CampsiteController(dummyCampsiteDB))
        val entrant = User(deviceId = "user-1", role = "entrant")

        controller.removeProfile(
            user = entrant,
            onSuccess = {},
            onFailure = { throw it }
        )

        assertTrue(fakeDb.deletedUserIds.contains("user-1"))
        assertEquals(false, fakeDb.ownerProfileDeleted)
    }

    @Test
    fun removeProfileTriggersDeleteOwnerProfileForOwners() {
        val fakeDb = FakeUserDB()
        // Updated to use the correct 5-parameter constructor and 2-parameter LotteryController
        val controller = UserController(
            userDB = fakeDb,
            campsiteDB = dummyCampsiteDB,
            lotteryDB = dummyLotteryDB,
            lotteryController = LotteryController(dummyLotteryDB, fakeDb),
            campsiteController = CampsiteController(dummyCampsiteDB)
        )
        val owner = User(deviceId = "owner-1", role = "owner")

        controller.removeProfile(
            user = owner,
            onSuccess = {},
            onFailure = { throw it }
        )

        assertTrue(fakeDb.ownerProfileDeleted)
    }
}