package com.thefloor.app.core.common

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/** The app is built for people who work in shifts, so Home is built around the shift rather than around sections. */
enum class ShiftPhase {
    /** Winding up to clock on. */
    BEFORE,

    /** On the floor. */
    ON_FLOOR,

    /** Clocked off. */
    AFTER,

    /** The graveyard. */
    NIGHT,
}

object ShiftClock {
    fun phaseFor(time: LocalTime): ShiftPhase = when (time.hour) {
        in 4..8 -> ShiftPhase.BEFORE
        in 9..16 -> ShiftPhase.ON_FLOOR
        in 17..21 -> ShiftPhase.AFTER
        else -> ShiftPhase.NIGHT
    }

    /** The global handover the product is named for: the next hub to clock off. */
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
