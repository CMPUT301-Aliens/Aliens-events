package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery

class LotteryController(private val lotteryDB: LotteryDB) {
    fun validateLottery(lottery: Lottery): List<String> {
        val errors = mutableListOf<String>()

        if (lottery.campsiteIds.isEmpty()) {
            errors.add("Choose at least one campsite.")
        }

        if (!lottery.registrationEndDate.after(lottery.registrationStartDate)) {
            errors.add("Registration end must be after registration start.")
        }

        if (lottery.numberOfWinners <= 0){
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

    fun updateLotteryListing(lottery: Lottery, onSuccess: () -> Unit,onInvalid: (List<String>) -> Unit, onFailure: (Exception) -> Unit) {

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




}