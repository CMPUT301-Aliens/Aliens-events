package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery

/**
 * Purpose: Checks, saves, and finds lotteries.
 * Role: Sits between LotteryEditActivity and LotteryDB. Invalid lotteries are never saved.
 * Outstanding issues: The draw (#35), the waitlist (#41), and removeCampsiteFromLotteries
 * are not built yet.
 */
class LotteryController(private val lotteryDB: LotteryDB) {
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