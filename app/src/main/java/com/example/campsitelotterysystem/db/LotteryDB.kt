package com.example.campsitelotterysystem.db

import com.example.campsitelotterysystem.model.Lottery

/**
 * Purpose: Saves lotteries to Firestore.
 * Role: LotteryController uses the LotteryDB interface, so tests can use a fake database.
 * FirestoreLotteryDB is the real version and uses DBConnector.
 * Outstanding issues: There are no read functions yet. These come with the draw (#35),
 * the waitlist (#41), and getOpenLotteryForCampsite.
 */
interface LotteryDB {
    fun addLottery(lottery: Lottery, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    fun updateLottery(lottery: Lottery, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun removeAllLotteriesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getLotteriesWithUserInWaitlist(userId: String, onSuccess: (List<Lottery>) -> Unit, onFailure: (Exception) -> Unit)
}

class FirestoreLotteryDB : LotteryDB {
    private val lotteries = DBConnector.db.collection("lotteries")

    override fun addLottery(
        lottery: Lottery,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val document = lotteries.document()
        document.set(lottery.copy(id = document.id))
            .addOnSuccessListener { onSuccess(document.id) }
            .addOnFailureListener { onFailure(it) }
    }

    override fun updateLottery(
        lottery: Lottery,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        lotteries.document(lottery.id).set(lottery)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    override fun removeAllLotteriesByOwner(ownerId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        lotteries.whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }
                val batch = DBConnector.db.batch()
                for (doc in documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }
    override fun getLotteriesWithUserInWaitlist(userId: String, onSuccess: (List<Lottery>) -> Unit, onFailure: (Exception) -> Unit) {
        lotteries.whereArrayContains("waitingList", userId).get()
            .addOnSuccessListener { documents -> onSuccess(documents.toObjects(Lottery::class.java)) }
            .addOnFailureListener { onFailure(it) }
    }
}