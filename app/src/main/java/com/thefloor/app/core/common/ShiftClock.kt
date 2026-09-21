package com.thefloor.app.core.common

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * The app is built for people who work in shifts, so Home is built around the
 * shift rather than around sections.
 *
 * Four phases, derived from local time. Pure functions — unit-tested, and no
 * Android dependency, so the phase can be computed anywhere.
 */
enum class ShiftPhase {
    /** Winding up to clock on. What happened while they were off. */
    BEFORE,

    /** On the floor. Short things, readable in a break. */
    ON_FLOOR,

    /** Clocked off. What their shift added up to. */
    AFTER,

    /** The graveyard. A different tone, and a different offer. */
    NIGHT,
}

object ShiftClock {

    fun phaseFor(time: LocalTime): ShiftPhase = when (time.hour) {
        in 4..8 -> ShiftPhase.BEFORE
        in 9..16 -> ShiftPhase.ON_FLOOR
        in 17..21 -> ShiftPhase.AFTER
        else -> ShiftPhase.NIGHT // 22:00–03:59
    }

    /**
     * The global handover the product is named for: the next hub to clock off.
     *
     * Each hub ends its main shift at 17:00 local. We return whichever is
     * closest to that moment without having passed it, so the line on Home
     * ("Manila hands over in 40 minutes") is always true rather than decorative.
     */
    data class Handover(val city: String, val minutes: Long)

    private val hubs = listOf(
        "Johannesburg" to "Africa/Johannesburg",
        "Manila" to "Asia/Manila",
        "Bogotá" to "America/Bogota",
        "Bengaluru" to "Asia/Kolkata",
        "Kraków" to "Europe/Warsaw",
        "Cairo" to "Africa/Cairo",
    )

    fun nextHandover(now: java.time.Instant = java.time.Instant.now()): Handover? =
        hubs.mapNotNull { (city, zone) ->
            runCatching {
                val local = ZonedDateTime.ofInstant(now, ZoneId.of(zone))
                val clockOff = local.truncatedTo(ChronoUnit.DAYS).withHour(17)
                val minutes = ChronoUnit.MINUTES.between(local, clockOff)
                if (minutes in 0..240) Handover(city, minutes) else null
            }.getOrNull()
        }.minByOrNull { it.minutes }
}
