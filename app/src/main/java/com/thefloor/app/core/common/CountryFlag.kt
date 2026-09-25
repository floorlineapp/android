package com.thefloor.app.core.common

/** A two-letter country code as its flag. */
fun countryFlag(code: String?): String? {
    val c = code?.trim()?.uppercase() ?: return null
    if (c.length != 2 || c.any { it !in 'A'..'Z' }) return null
    val base = 0x1F1E6
    val first = base + (c[0] - 'A')
    val second = base + (c[1] - 'A')
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
