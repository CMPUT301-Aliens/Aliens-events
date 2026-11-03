package com.example.campsitelotterysystem.model
import java.util.Date
data class Lottery(
    val id : String = "",
    val ownerId: String = "",
    val campsiteIds: List<String> = emptyList(),
    val registrationStartDate: Date = Date(),
    val registrationEndDate: Date = Date(),
    val  bookingStartDate:   Date = Date(),
    val bookingEndDate: Date = Date(),

    val numberOfWinners: Int = 0,
    val waitlistCapacity: Int? = null,
    val waitingList: List<String> = emptyList(),
    val winners: List<String> = emptyList(),
    val status: String = "open"
) {
    fun isRegistrationOpen(now: Date): Boolean {
        return status == "open" &&
                !now.before(registrationStartDate) &&
                !now.after(registrationEndDate)
    }
}