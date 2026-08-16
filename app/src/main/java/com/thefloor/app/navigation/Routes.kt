package com.thefloor.app.navigation

/**
 * All navigation routes. String-based patterns with helpers — one place to
 * change, no magic strings in feature code.
 */
object Routes {
    // Auth graph
    const val WELCOME = "welcome"
    const val SIGN_UP = "signup?code={code}"
    fun signUp(code: String? = null) = if (code != null) "signup?code=$code" else "signup"
    const val LOG_IN = "login"
    const val VERIFY_EMAIL = "verify_email?token={token}"
    fun verifyEmail(token: String? = null) = if (token != null) "verify_email?token=$token" else "verify_email"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password/{token}"
    fun resetPassword(token: String) = "reset_password/$token"
    const val ONBOARDING = "onboarding"

    // Main tabs
    const val HOME = "home"
    const val FLOOR = "floor"
    const val TALK = "talk"
    const val RADIO = "radio"
    const val MORE = "more"

    // Detail destinations
    const val COMMUNITY_DETAIL = "floor/{communityId}"
    fun communityDetail(id: String) = "floor/$id"
    const val POST_DETAIL = "talk/{postId}"
    fun postDetail(id: String) = "talk/$id"
    const val COMPOSE_POST = "talk_compose"

    const val INVITE_EARN = "invite_earn"
    const val INVITE_MILESTONES = "invite_earn/milestones"
    const val INVITE_HISTORY = "invite_earn/history"
    const val INVITE_FAQ = "invite_earn/faq"

    const val REWARDS = "rewards"
    const val REWARD_TRANSACTIONS = "rewards/transactions"

    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATION_PREFS = "notifications/preferences"

    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile/edit"
    const val PRIVACY = "profile/privacy"
    const val PUBLIC_PROFILE = "member/{userId}"
    fun publicProfile(id: String) = "member/$id"

    const val SETTINGS = "settings"
    const val DELETE_ACCOUNT = "settings/delete_account"

    // Phase-2 stubs (flag-gated, deep links never dead-end)
    const val JOBS = "jobs"
    const val ACADEMY = "academy"
    const val MARKETPLACE = "marketplace"
    const val INSIGHTS = "insights"
    const val ABOUT = "about"
}
