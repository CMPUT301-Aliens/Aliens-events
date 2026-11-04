package com.example.campsitelotterysystem.model
import java.util.Date
/**
 * Purpose: Data model for one campsite lottery.
 * Role: Stored in the "lotteries" Firestore collection. Created by LotteryController
 * and read by entrants who join the waiting list.
 * Outstanding issues: waitlistCapacity is not used yet (#34 is not in this checkpoint).
 * status only changes from "open" once the draw (#35) is built.
 *
 * @property id Firestore document ID
 * @property ownerId device ID of the owner who created the lottery
 * @property campsiteIds IDs of the campsites in this lottery
 * @property registrationStartDate when entrants can start joining
 * @property registrationEndDate when joining closes and the draw can run
 * @property bookingStartDate first day winners can book
 * @property bookingEndDate last day winners can book
 * @property numberOfWinners how many entrants the draw picks
 * @property waitlistCapacity optional limit on the waiting list, or null for no limit
 * @property waitingList device IDs of entrants on the waiting list
 * @property winners device IDs of the drawn winners
 * @property status "open", "drawn", or "closed"
 */
data class Lottery(
    val id: String = "",
    val ownerId: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStartDate: Date = Date(),
    val registrationEndDate: Date = Date(),
    val  bookingStartDate: Date = Date(),
    val bookingEndDate: Date = Date(),
    val numberOfWinners: Int = 0,
    val waitlistCapacity: Int? = null,
    val waitingList: List<String> = emptyList(),
    val winners: List<String> = emptyList(),
    val status: String = "open"
) {

    /**
     * Checks whether entrants can join the waiting list right now.
     *
     * @param now the current time
     * @return true if the lottery is open and now is between the registration start and end, inclusive
     */
    fun isRegistrationOpen(now: Date): Boolean {
        return status == "open" &&
                !now.before(registrationStartDate) &&
                !now.after(registrationEndDate)
    }
}