package com.thefloor.app.domain

/**
 * Client-side validation for immediate feedback. The server re-validates
 * everything — these exist for UX, not for trust.
 */
object Validators {

    fun email(value: String): String? = when {
        value.isBlank() -> "Enter your email"
        !Regex("^[A-Za-z0-9+_.\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$").matches(value.trim()) ->
            "That doesn't look like an email address"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "Choose a password"
        value.length < 10 -> "At least 10 characters"
        !value.any { it.isDigit() } && !value.any { !it.isLetterOrDigit() } ->
            "Add a number or symbol"
        else -> null
    }

    fun displayName(value: String): String? = when {
        value.isBlank() -> "Enter a display name"
        value.trim().length < 2 -> "At least 2 characters"
        value.trim().length > 60 -> "At most 60 characters"
        else -> null
    }

    fun postBody(value: String): String? = when {
        value.isBlank() -> "Write something first"
        value.trim().length < 2 -> "A little more, please"
        value.length > 5000 -> "Posts are limited to 5,000 characters"
        else -> null
    }

    private const val REFERRAL_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /** Mirrors the server's referral code format (8 chars, unambiguous alphabet). */
    fun referralCodeFormat(value: String): Boolean {
        val normalized = value.trim().uppercase()
        return normalized.length == 8 && normalized.all { it in REFERRAL_ALPHABET }
    }
}
