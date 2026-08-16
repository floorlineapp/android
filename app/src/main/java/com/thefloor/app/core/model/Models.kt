package com.thefloor.app.core.model

/**
 * Domain models — immutable, UI-agnostic, mapped from network DTOs in repositories.
 * Money is always minor units + currency; the client only formats, never computes.
 */

data class Money(val amountMinor: Long, val currency: String) {
    fun format(): String {
        val units = amountMinor / 100
        val cents = (amountMinor % 100).toInt()
        val symbol = when (currency) {
            "EUR" -> "€"
            "USD" -> "$"
            "GBP" -> "£"
            else -> "$currency "
        }
        return if (cents == 0) "$symbol$units" else "$symbol$units.${cents.toString().padStart(2, '0')}"
    }
}

enum class CareerLevel(val label: String) {
    AGENT("Agent"),
    SENIOR_AGENT("Senior Agent"),
    SME("SME"),
    TEAM_LEADER("Team Leader"),
    SUPERVISOR("Supervisor"),
    MANAGER("Manager"),
    OPERATIONS_MANAGER("Operations Manager"),
    DIRECTOR("Director"),
}

enum class WorkMode(val label: String) { ONSITE("On-site"), HYBRID("Hybrid"), REMOTE("Remote") }

data class UserProfile(
    val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val country: String?,
    val city: String?,
    val languages: List<String>,
    val employer: String?,
    val site: String?,
    val industry: String?,
    val role: String?,
    val careerLevel: CareerLevel?,
    val experienceYears: Int?,
    val workMode: WorkMode?,
    val skills: List<String>,
    val completeness: Int,
    val emailVerified: Boolean,
    val visibility: Map<String, String>,
)

enum class MembershipState { NOT_JOINED, JOINED, PENDING, MUTED, RESTRICTED }

data class Community(
    val id: String,
    val slug: String,
    val name: String,
    val description: String,
    val kind: String,
    val memberCount: Int,
    val isRestricted: Boolean,
    val membershipState: MembershipState,
)

data class TalkCategory(val id: String, val slug: String, val name: String)

data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorLevel: CareerLevel?,
    val categoryId: String,
    val categoryName: String,
    val communityId: String?,
    val body: String,
    val commentCount: Int,
    val reactionCount: Int,
    val myReaction: String?,
    val saved: Boolean,
    val createdAt: String,
)

data class Comment(
    val id: String,
    val authorId: String,
    val authorName: String,
    val parentId: String?,
    val body: String,
    val createdAt: String,
)

// ---------- Invite & Earn ----------

data class MilestoneReward(
    val kind: String,           // CREDITS | CASH | STATUS
    val credits: Int?,
    val cash: Money?,
    val statusGrant: String?,
    val perkText: String?,
)

data class NextMilestone(
    val thresholdActive: Int,
    val currentActive: Int,
    val remaining: Int,
    val progressPct: Int,
    val rewards: List<MilestoneReward>,
)

data class MilestoneTier(
    val thresholdActive: Int,
    val rewards: List<MilestoneReward>,
    val state: String,          // LOCKED | UNLOCKED | PAID
)

data class ReferralSummary(
    val code: String,
    val link: String,
    val invited: Int,
    val active: Int,
    val credits: Long,
    val cashEarned: Money,
    val ambassadorStatus: String,
    val nextMilestone: NextMilestone?,
    val disclosures: List<String>,
)

data class ReferralHistoryItem(
    val id: String,
    val displayStatus: String,
    val createdAt: String,
)

data class FaqItem(val question: String, val answer: String)

// ---------- Rewards ----------

data class WayToEarn(val title: String, val credits: Long, val deepLink: String)

data class RewardTransaction(
    val id: String,
    val deltaCredits: Long,
    val reason: String,
    val createdAt: String,
)

data class RewardsSummary(
    val creditsBalance: Long,
    val waysToEarn: List<WayToEarn>,
    val recentTransactions: List<RewardTransaction>,
)

// ---------- Notifications ----------

data class AppNotification(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val deepLink: String?,
    val read: Boolean,
    val createdAt: String,
)

// ---------- Home ----------

data class CompletionCard(val title: String, val subtitle: String, val deepLink: String)

data class HomeContent(
    val displayName: String,
    val presenceCount: Int,
    val completionCards: List<CompletionCard>,
    val myFloors: List<Community>,
    val trendingPosts: List<Post>,
    val invitedCount: Int,
    val activeReferrals: Int,
    val creditsBalance: Long,
    val flags: Map<String, Boolean>,
)
