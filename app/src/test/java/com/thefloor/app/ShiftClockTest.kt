package com.thefloor.app

import com.thefloor.app.core.common.ShiftClock
import com.thefloor.app.core.common.ShiftPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class ShiftClockTest {

    @Test
    fun `every hour of the day lands in exactly one phase`() {
        val phases = (0..23).map { ShiftClock.phaseFor(LocalTime.of(it, 0)) }
        assertEquals(24, phases.size)
        // All four phases are reachable — no dead branch.
        assertEquals(ShiftPhase.entries.toSet(), phases.toSet())
    }

    @Test
    fun `the boundaries are where they are meant to be`() {
        assertEquals(ShiftPhase.NIGHT, ShiftClock.phaseFor(LocalTime.of(3, 59)))
        assertEquals(ShiftPhase.BEFORE, ShiftClock.phaseFor(LocalTime.of(4, 0)))
        assertEquals(ShiftPhase.BEFORE, ShiftClock.phaseFor(LocalTime.of(8, 59)))
        assertEquals(ShiftPhase.ON_FLOOR, ShiftClock.phaseFor(LocalTime.of(9, 0)))
        assertEquals(ShiftPhase.ON_FLOOR, ShiftClock.phaseFor(LocalTime.of(16, 59)))
        assertEquals(ShiftPhase.AFTER, ShiftClock.phaseFor(LocalTime.of(17, 0)))
        assertEquals(ShiftPhase.AFTER, ShiftClock.phaseFor(LocalTime.of(21, 59)))
        assertEquals(ShiftPhase.NIGHT, ShiftClock.phaseFor(LocalTime.of(22, 0)))
    }

    @Test
    fun `a handover is never reported as already past or absurdly far off`() {
        // Sampled across a whole day so no hour can produce a negative or stale figure.
        for (hour in 0..23) {
            val now = java.time.ZonedDateTime.of(
                2026, 9, 21, hour, 0, 0, 0, java.time.ZoneId.of("UTC"),
            ).toInstant()
            val handover = ShiftClock.nextHandover(now) ?: continue
            assertTrue("negative at $hour:00 UTC", handover.minutes >= 0)
            assertTrue("too far at $hour:00 UTC", handover.minutes <= 240)
            assertTrue(handover.city.isNotBlank())
        }
    }

    @Test
    fun `the hub closest to clocking off is the one reported`() {
        // 14:30 UTC: Manila is 22:30 (past), Johannesburg 16:30 — 30 minutes out.
        val now = java.time.ZonedDateTime.of(
            2026, 9, 21, 14, 30, 0, 0, java.time.ZoneId.of("UTC"),
        ).toInstant()
        val handover = ShiftClock.nextHandover(now)
        assertEquals("Johannesburg", handover?.city)
        assertEquals(30L, handover?.minutes)
    }
}
