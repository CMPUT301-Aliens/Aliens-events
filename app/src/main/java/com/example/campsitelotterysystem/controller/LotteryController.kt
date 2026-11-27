package com.example.campsitelotterysystem.controller

import com.example.campsitelotterysystem.db.LotteryDB
import com.example.campsitelotterysystem.model.Lottery
import com.example.campsitelotterysystem.db.UserDB
import com.example.campsitelotterysystem.model.User
import kotlin.random.Random
import java.util.Date
import com.example.campsitelotterysystem.model.Notification
import com.example.campsitelotterysystem.model.Campsite
/**
 * Purpose: Checks, saves, and finds lotteries, gets the entrants on a waitlist, draws the winners (#35),
 * checks and builds waitlist notifications (#43), finds which campsites are already in a lottery (#16), and saves owners' edits (#33).
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

    /**
     * Saves an owner's changes to a lottery that already exists (#33).
     * Only the campsites, dates, and number of winners change. The ID, owner, waiting list,
     * and winners stay the same. Edits are only allowed until registration ends, and the new
     * registration end can't be in the past.
     * Sends the saved lottery to onSuccess, or the problems to onInvalid.
     */
    fun editLotteryListing(
        saved: Lottery,
        changes: Lottery,
        now: Date,
        onSuccess: (Lottery) -> Unit,
        onInvalid: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!saved.canBeEdited(now)) {
            onInvalid(listOf("This lottery can't be changed after registration ends."))
            return
        }
        val updated = saved.copy(
            campsiteIds = changes.campsiteIds,
            registrationStartDate = changes.registrationStartDate,
            registrationEndDate = changes.registrationEndDate,
            bookingStartDate = changes.bookingStartDate,
            bookingEndDate = changes.bookingEndDate,
            numberOfWinners = changes.numberOfWinners
        )
        val errors = validateLottery(updated).toMutableList()
        if (updated.registrationEndDate.before(now)) {
            errors.add("Registration end can't be in the past.")
        }
        if (errors.isNotEmpty()) {
            onInvalid(errors)
            return
        }
        lotteryDB.updateLottery(updated, onSuccess = { onSuccess(updated) }, onFailure = onFailure)
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

    /**
     * Checks a waitlist message before the owner sends it (#43).
     * Returns a list of problems, or an empty list if it can be sent.
     */
    fun validateWaitlistMessage(
        lottery: Lottery,
        message: String,
        recipientIds: List<String>
    ): List<String> {
        val errors = mutableListOf<String>()
        if (message.isBlank()) {
            errors.add("Message cannot be blank.")
        }
        if (recipientIds.isEmpty()) {
            errors.add("Select at least one entrant.")
        }
        if (recipientIds.any { it !in lottery.waitingList }) {
            errors.add("Only entrants on the waitlist can be notified.")
        }
        return errors
    }

    /**
     * Builds the notification the owner sends to the chosen waitlist entrants (#43).
     * The time is passed in so tests can use a fixed date.
     */
    fun buildWaitlistNotification(
        lottery: Lottery,
        senderId: String,
        recipientIds: List<String>,
        message: String,
        now: Date
    ): Notification {
        return Notification(
            timestamp = now,
            senderId = senderId,
            recipientIds = recipientIds.distinct(),
            messageContent = message.trim(),
            type = "waitlist_message",
            lotteryId = lottery.id
        )
    }

    /*returns array of lotteries with user in waitlist*/
    fun getLotteriesWithUserInWaitlist(
        userId: String,
        onSuccess: (List<Lottery>) -> Unit,
        onFailure: (Exception) -> Unit = {}
    ) {
        lotteryDB.getLotteriesWithUserInWaitlist(userId, onSuccess, onFailure)
    }

    /**
     * Finds which of the given campsites are already in an active lottery (#16).
     * Sends back their IDs, so the form can stop the owner from adding them to a second lottery.
     */
    fun findCampsitesInALottery(
        campsiteIds: List<String>,
        onSuccess: (Set<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (campsiteIds.isEmpty()) {
            onSuccess(emptySet())
            return
        }
        val taken = mutableSetOf<String>()
        var checked = 0
        var failed = false
        campsiteIds.forEach { id ->
            lotteryDB.getLotteryForCampsite(
                id,
                onSuccess = { lottery ->
                    if (lottery != null) taken.add(id)
                    checked++
                    if (checked == campsiteIds.size && !failed) onSuccess(taken)
                },
                onFailure = { error ->
                    if (!failed) {
                        failed = true
                        onFailure(error)
                    }
                }
            )
        }
    }

    /**
     * Turns a lottery's campsite IDs into campsite names for the screen (#16).
     * Shows the ID instead if a campsite's name isn't known.
     */
    fun campsiteNames(campsiteIds: List<String>, campsites: List<Campsite>): List<String> {
        return campsiteIds.map { id -> campsites.firstOrNull { it.id == id }?.name ?: id }
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