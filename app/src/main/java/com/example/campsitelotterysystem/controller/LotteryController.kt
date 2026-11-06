package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.model.User
import kotlin.random.Random
import java.util.Date
/**
 * Purpose: Checks, saves, and finds lotteries, gets the entrants on a waitlist, and draws the winners (#35).
 * Role: Sits between the lottery screens and LotteryDB and UserDB. Invalid lotteries are never saved.
 * Outstanding issues: removeCampsiteFromLotteries is not built yet. There is no server, so the draw runs when a screen calls drawIfReady after registration ends.
 */
class LotteryController(private val lotteryDB: LotteryDB, private val userDB: UserDB)  {
    /** Returns a list of problems with the lottery, or an empty list if it is valid. */
    fun validateLottery(lottery: Lottery): List<String> {
        val errors = mutableListOf<String>()

        if (lottery.campsiteIds.isEmpty()) {
            errors.add("Choose at least one campsite.")
        }

        if (!lottery.registrationEndDate.after(lottery.registrationStartDate)) {
            errors.add("Registration end must be after registration start.")
        }

        if (lottery.numberOfWinners <= 0) {
            errors.add("Number of winners must be greater than zero.")
        }
        if (!lottery.bookingStartDate.after(lottery.registrationEndDate)) {
            errors.add("Booking start must be after registration end.")
        }
        if (!lottery.bookingEndDate.after(lottery.bookingStartDate)) {

            errors.add("Booking end must be after booking start.")
        }



        return errors
    }

    /** Saves a new lottery if it is valid. Otherwise, sends the problems to onInvalid. */
    fun createLotteryListing(
        lottery: Lottery,
        onSuccess: (String) -> Unit,
        onInvalid: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val errors = validateLottery(lottery)

        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }

        lotteryDB.addLottery(lottery, onSuccess, onFailure)
    }

    /** Saves changes to an existing lottery if it is valid and has an ID. Otherwise, sends the problems to onInvalid. */
    fun updateLotteryListing(
        lottery: Lottery,
        onSuccess: () -> Unit,
        onInvalid: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val errors = validateLottery(lottery).toMutableList()
        if (lottery.id.isBlank()) {
            errors.add("This lottery has no ID.")
        }
        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }

        lotteryDB.updateLottery(lottery, onSuccess, onFailure)


    }
    /** Finds the campsite's current lottery, or null if it has none. */
    fun getLotteryForCampsite(campsiteId: String, onSuccess: (Lottery?) -> Unit, onFailure: (Exception) -> Unit)
    {
        lotteryDB.getLotteryForCampsite(campsiteId, onSuccess, onFailure)


    }
    /** Gets the users on the lottery's waitlist, in the order they joined. An entrant with no profile comes back with only their device ID. */
    fun getWaitlistEntrants(lottery: Lottery, onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        val ids = lottery.waitingList
        if (ids.isEmpty()) {
            onSuccess(emptyList())
            return
        }

        val found = mutableMapOf<String, User>()
        var processedCount = 0

        ids.forEach { id ->
            userDB.getUserById(
                id,
                onSuccess = { user ->

                    found[id] = user ?: User(deviceId = id)
                    processedCount++
                    if (processedCount == ids.size) {
                        onSuccess(ids.map { found[it]!! })}
                },
                onFailure = { onFailure(it) }
            )
        }
    }
    /**
     * Picks up to numberOfWinners different entrants from the waiting list at random.
     * If there are fewer entrants than winners, everyone on the list wins.
     */
    fun pickWinners(waitingList: List<String>, numberOfWinners: Int, random: Random = Random.Default): List<String> {
        if (numberOfWinners <= 0) return emptyList()
        return waitingList.distinct().shuffled(random).take(numberOfWinners)
    }
    /**
     * Runs the draw (#35) if registration has ended and it hasn't run yet.
     * Saves the winners and sets status to "drawn", then sends back the drawn lottery.
     * If it isn't time yet, sends back the lottery unchanged.
     */
    fun drawIfReady(lottery: Lottery, now: Date, onSuccess: (Lottery) -> Unit, onFailure: (Exception) -> Unit) {
        if (!lottery.isReadyToDraw(now)) {
            onSuccess(lottery)
            return
        }
        val drawn = lottery.copy(
            winners = pickWinners(lottery.waitingList, lottery.numberOfWinners),
            status = "drawn"
        )
        lotteryDB.updateLottery(drawn, onSuccess = { onSuccess(drawn) }, onFailure = onFailure)
    }






    fun removeUserFromWaitlists(userId: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        lotteryDB.getLotteriesWithUserInWaitlist(userId, onSuccess = { userLotteries ->
            if (userLotteries.isEmpty()) {
                onSuccess()
                return@getLotteriesWithUserInWaitlist
            }
            var processedCount = 0
            userLotteries.forEach { lottery ->
                // Create a new list without the deleted user's ID
                val updatedWaitlist = lottery.waitingList.filter { it != userId }
                val updatedLottery = lottery.copy(waitingList = updatedWaitlist)
                // Save the updated lottery back to Datbase
                lotteryDB.updateLottery(updatedLottery, onSuccess = {
                    processedCount++
                    if (processedCount == userLotteries.size) onSuccess()
                }, onFailure = {
                    onFailure(it)
                })
            }
        }, onFailure = {
            onFailure(it)
        })
    }



}