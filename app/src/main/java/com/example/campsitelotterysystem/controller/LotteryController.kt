package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery

class LotteryController(private val lotteryDB: LotteryDB) {

    fun createLotteryListing(lottery: Lottery, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        lotteryDB.addLottery(lottery, onSuccess, onFailure)
    }

    fun updateLotteryListing(lottery: Lottery, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        lotteryDB.updateLottery(lottery, onSuccess, onFailure)
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