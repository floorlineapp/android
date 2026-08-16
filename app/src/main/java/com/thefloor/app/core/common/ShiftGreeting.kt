package com.thefloor.app.core.common

import java.time.LocalTime

/**
 * Shift-aware greeting: the product speaks to workers in shifts, not mornings.
 * Pure function of local time — unit-tested.
 */
object ShiftGreeting {

    fun greetingFor(time: LocalTime): String = when (time.hour) {
        in 5..11 -> "Good morning shift."
        in 12..17 -> "Good afternoon shift."
        in 18..22 -> "Good evening shift."
        else -> "Good night shift."
    }
}
