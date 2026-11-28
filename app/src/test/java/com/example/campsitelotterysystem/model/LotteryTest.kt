package com.example.campsitelotterysystem.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/** Unit tests for Lottery.isRegistrationOpen, Lottery.isReadyToDraw, Lottery.canBeEdited, and Lottery.isWaitlistFull. */
class LotteryTest {

    private val lottery = Lottery(
        registrationStartDate = Date(1000),
        registrationEndDate = Date(2000)
    )

    @Test
    fun registrationIsClosedBeforeStart() {
        assertFalse(lottery.isRegistrationOpen(Date(999)))
    }

    @Test
    fun registrationIsOpenAtExactStart() {
        assertTrue(lottery.isRegistrationOpen(Date(1000)))
    }

    @Test
    fun registrationIsOpenDuringPeriod() {
        assertTrue(lottery.isRegistrationOpen(Date(1500)))
    }

    @Test
    fun registrationIsOpenAtExactEnd() {
        assertTrue(lottery.isRegistrationOpen(Date(2000)))
    }

    @Test
    fun registrationIsClosedAfterEnd() {
        assertFalse(lottery.isRegistrationOpen(Date(2001)))
    }

    @Test
    fun registrationIsClosedWhenDrawn() {
        val drawn = lottery.copy(status = "drawn")
        assertFalse(drawn.isRegistrationOpen(Date(1500)))
    }

    @Test
    fun registrationIsClosedWhenClosed() {
        val drawn = lottery.copy(status = "closed")
        assertFalse(drawn.isRegistrationOpen(Date(1500)))
    }
    @Test
    fun readyToDrawAfterEnd() {
        assertTrue(lottery.isReadyToDraw(Date(2001)))
    }
    @Test
    fun notReadyToDrawDuringRegistration() {
        assertFalse(lottery.isReadyToDraw(Date(1500)))
    }

    @Test
    fun notReadyToDrawAtExactEnd() {
        assertFalse(lottery.isReadyToDraw(Date(2000)))
    }

    @Test
    fun notReadyToDrawWhenAlreadyDrawn() {
        val drawn = lottery.copy(status = "drawn")
        assertFalse(drawn.isReadyToDraw(Date(3000)))
    }

    @Test
    fun canBeEditedBeforeRegistrationStarts() {
        assertTrue(lottery.canBeEdited(Date(500)))
    }

    @Test
    fun canBeEditedDuringRegistration() {
        assertTrue(lottery.canBeEdited(Date(1500)))
    }

    @Test
    fun cannotBeEditedAfterRegistrationEnds() {
        assertFalse(lottery.canBeEdited(Date(2500)))
    }

    @Test
    fun cannotBeEditedWhenDrawn() {
        val drawn = lottery.copy(status = "drawn")
        assertFalse(drawn.canBeEdited(Date(1500)))
    }

    @Test
    fun waitlistWithNoCapacityIsNeverFull() {
        val noLimit = lottery.copy(
            waitingList = listOf("5f2a9c81d3e04b76", "3f2b8c1e0a7d4f65", "c81e728d9d4c2f63"),
            waitlistCapacity = null
        )
        assertFalse(noLimit.isWaitlistFull())
    }

    @Test
    fun waitlistBelowCapacityIsNotFull() {
        val room = lottery.copy(waitingList = listOf("5f2a9c81d3e04b76"), waitlistCapacity = 2)
        assertFalse(room.isWaitlistFull())
    }

    @Test
    fun waitlistAtCapacityIsFull() {
        val full = lottery.copy(
            waitingList = listOf("5f2a9c81d3e04b76", "3f2b8c1e0a7d4f65"),
            waitlistCapacity = 2
        )
        assertTrue(full.isWaitlistFull())
    }
}