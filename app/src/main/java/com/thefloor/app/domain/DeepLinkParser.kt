package com.thefloor.app.domain

/**
 * Single source of truth for deep-link → destination mapping.
 * Handles https://thefloor.example/... and thefloor://... uniformly.
 * Pure and unit-tested; unknown links resolve to Home, never crash.
 */
object DeepLinkParser {

    sealed interface Target {
        data class Invite(val referralCode: String) : Target
        data class Floor(val communityId: String) : Target
        data class TalkPost(val postId: String) : Target
        data class Job(val jobId: String) : Target
        data class Course(val courseId: String) : Target
        data class Deal(val dealId: String) : Target
        data class Profile(val userId: String) : Target
        data object ProfileEdit : Target
        data class ResetPassword(val token: String) : Target
        data class VerifyEmail(val token: String) : Target
        data object InviteEarn : Target
        data object Rewards : Target
        data object FloorTab : Target
        data object Home : Target
    }

    private val hosts = setOf("thefloor.example", "www.thefloor.example")

    fun parse(uri: String): Target {
        val trimmed = uri.trim()
        val path: String = when {
            trimmed.startsWith("thefloor://") -> "/" + trimmed.removePrefix("thefloor://").trimStart('/')
            trimmed.startsWith("https://") || trimmed.startsWith("http://") -> {
                val withoutScheme = trimmed.substringAfter("://")
                val host = withoutScheme.substringBefore("/")
                if (host !in hosts) return Target.Home
                "/" + withoutScheme.substringAfter("/", "").substringBefore("?")
            }
            else -> return Target.Home
        }
        val segments = path.trim('/').split('/').filter { it.isNotBlank() }
        return when {
            segments.size == 2 && segments[0] == "invite" -> Target.Invite(segments[1].uppercase())
            segments.size == 1 && segments[0] == "invite" -> Target.InviteEarn
            segments.size == 2 && segments[0] == "floor" -> Target.Floor(segments[1])
            segments.size == 1 && segments[0] == "floor" -> Target.FloorTab
            segments.size == 2 && segments[0] == "talk" -> Target.TalkPost(segments[1])
            segments.size == 2 && segments[0] == "job" -> Target.Job(segments[1])
            segments.size == 2 && segments[0] == "course" -> Target.Course(segments[1])
            segments.size == 2 && segments[0] == "marketplace" -> Target.Deal(segments[1])
            // Own-profile editor (backend-emitted thefloor://profile/edit) — must
            // not be mistaken for a member profile with id "edit".
            segments.size == 2 && segments[0] == "profile" && segments[1] == "edit" -> Target.ProfileEdit
            segments.size == 2 && segments[0] == "profile" -> Target.Profile(segments[1])
            segments.size == 2 && segments[0] == "reset" -> Target.ResetPassword(segments[1])
            segments.size == 2 && segments[0] == "verify" -> Target.VerifyEmail(segments[1])
            segments.size == 1 && segments[0] == "rewards" -> Target.Rewards
            else -> Target.Home
        }
    }
}
