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




}