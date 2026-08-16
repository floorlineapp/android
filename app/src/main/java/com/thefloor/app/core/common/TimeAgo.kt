package com.thefloor.app.core.common

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

/** Compact relative timestamps for feed items. Falls back safely on bad input. */
object TimeAgo {

    fun format(isoTimestamp: String, now: Instant = Instant.now()): String {
        val instant = try {
            OffsetDateTime.parse(isoTimestamp).toInstant()
        } catch (_: DateTimeParseException) {
            return ""
        }
        val d = Duration.between(instant, now)
        return when {
            d.isNegative -> "now"
            d.toMinutes() < 1 -> "now"
            d.toMinutes() < 60 -> "${d.toMinutes()}m"
            d.toHours() < 24 -> "${d.toHours()}h"
            d.toDays() < 7 -> "${d.toDays()}d"
            d.toDays() < 365 -> "${d.toDays() / 7}w"
            else -> "${d.toDays() / 365}y"
        }
    }
}
