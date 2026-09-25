package com.thefloor.app.core.common

/**
 * A two-letter country code as its flag.
 *
 * The Floor spans dozens of countries and the flag is the fastest way to read
 * "this person works somewhere like me, or somewhere very different" — which is
 * most of what makes the feed feel global rather than generic.
 *
 * Built from regional indicator symbols rather than a lookup table, so every
 * ISO code works without anyone maintaining a list.
 */
fun countryFlag(code: String?): String? {
    val c = code?.trim()?.uppercase() ?: return null
    if (c.length != 2 || c.any { it !in 'A'..'Z' }) return null
    val base = 0x1F1E6
    val first = base + (c[0] - 'A')
    val second = base + (c[1] - 'A')
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
