package com.example.campsitelotterysystem.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/** Unit tests for Lottery.isRegistrationOpen and Lottery.isReadyToDraw. */
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
}