package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.Lottery

interface LotteryDB {
    fun addLottery(lottery: Lottery, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    fun updateLottery(lottery: Lottery, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreLotteryDB : LotteryDB {
    private val lotteries = DBConnector.db.collection("lotteries")

    override fun addLottery(lottery: Lottery, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val document = lotteries.document()
        document.set(lottery.copy(id = document.id))
            .addOnSuccessListener { onSuccess(document.id) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun updateLottery(lottery: Lottery, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        lotteries.document(lottery.id).set(lottery)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }


    }

}