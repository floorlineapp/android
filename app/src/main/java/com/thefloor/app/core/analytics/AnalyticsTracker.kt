package com.thefloor.app.core.analytics

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics abstraction. MVP logs locally in debug and no-ops in release;
 * the server emits the money/referral truth events itself so funnels can't
 * be spoofed. A batching HTTP or Firebase implementation slots in behind
 * this interface without touching call sites.
 */
interface AnalyticsTracker {
    fun track(event: String, props: Map<String, String> = emptyMap())
}

@Singleton
class LoggingAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun track(event: String, props: Map<String, String>) {
        // Only event names + coarse props — never PII.
        Timber.tag("analytics").d("%s %s", event, props)
    }
}

object Events {
    const val APP_OPEN = "app_open"
    const val SIGNUP_STARTED = "signup_started"
    const val SIGNUP_COMPLETED = "signup_completed"
    const val PROFILE_COMPLETED = "profile_completed"
    const val COMMUNITY_JOINED = "community_joined"
    const val POST_CREATED = "post_created"
    const val POST_VIEWED = "post_viewed"
    const val INVITE_LINK_SHARED = "invite_link_shared"
    const val REFERRAL_CODE_CAPTURED = "referral_code_captured"
    const val NOTIFICATION_OPENED = "notification_opened"
    const val SCREEN_VIEW = "screen_view"
}
