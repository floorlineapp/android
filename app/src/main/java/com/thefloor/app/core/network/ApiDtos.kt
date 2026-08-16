package com.thefloor.app.core.network

import kotlinx.serialization.Serializable

/** Wire DTOs — mirror the Floor API. Mapped to domain models in repositories. */

@Serializable
data class ApiErrorDto(val code: String = "UNKNOWN", val message: String = "Something went wrong", val field: String? = null)

// ---- auth ----
@Serializable
data class SignupRequestDto(
    val email: String,
    val password: String,
    val displayName: String,
    val referralCode: String? = null,
    val referralSource: String? = null,
)

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class RefreshRequestDto(val refreshToken: String)

@Serializable
data class TokenPairDto(val accessToken: String, val refreshToken: String, val expiresInSeconds: Long)

@Serializable
data class AuthResponseDto(val userId: String, val emailVerified: Boolean, val tokens: TokenPairDto)

@Serializable
data class EmailBodyDto(val email: String)

@Serializable
data class TokenBodyDto(val token: String)

@Serializable
data class ResetRequestDto(val token: String, val newPassword: String)

@Serializable
data class OkDto(val ok: Boolean = true)

// ---- config / home ----
@Serializable
data class PublicConfigDto(
    val flags: Map<String, Boolean> = emptyMap(),
    val minAppVersion: Int = 1,
    val disclosures: List<String> = emptyList(),
    val legalUrls: Map<String, String> = emptyMap(),
)

@Serializable
data class CompletionCardDto(val title: String, val subtitle: String, val deepLink: String)

@Serializable
data class InviteMiniDto(val invited: Int = 0, val active: Int = 0)

@Serializable
data class HomeDto(
    val displayName: String = "",
    val presenceCount: Int = 0,
    val completionCards: List<CompletionCardDto> = emptyList(),
    val myFloors: List<CommunityDto> = emptyList(),
    val trendingPosts: List<PostDto> = emptyList(),
    val inviteEarn: InviteMiniDto = InviteMiniDto(),
    val creditsBalance: Long = 0,
    val flags: Map<String, Boolean> = emptyMap(),
)

// ---- profile ----
@Serializable
data class ProfileDto(
    val userId: String,
    val displayName: String = "",
    val photoUrl: String? = null,
    val country: String? = null,
    val city: String? = null,
    val ageRange: String? = null,
    val languages: List<String> = emptyList(),
    val employer: String? = null,
    val site: String? = null,
    val industry: String? = null,
    val role: String? = null,
    val careerLevel: String? = null,
    val experienceYears: Int? = null,
    val channels: List<String> = emptyList(),
    val workMode: String? = null,
    val skills: List<String> = emptyList(),
    val completeness: Int = 0,
    val emailVerified: Boolean = false,
    val visibility: Map<String, String> = emptyMap(),
)

@Serializable
data class UpdateProfileRequestDto(
    val displayName: String? = null,
    val country: String? = null,
    val city: String? = null,
    val languages: List<String>? = null,
    val employer: String? = null,
    val site: String? = null,
    val industry: String? = null,
    val role: String? = null,
    val careerLevel: String? = null,
    val experienceYears: Int? = null,
    val workMode: String? = null,
    val skills: List<String>? = null,
)

@Serializable
data class PrivacyRequestDto(val visibility: Map<String, String>)

@Serializable
data class DeleteAccountRequestDto(val password: String)

// ---- communities ----
@Serializable
data class CommunityDto(
    val id: String,
    val slug: String = "",
    val name: String = "",
    val description: String = "",
    val kind: String = "GLOBAL",
    val memberCount: Int = 0,
    val isRestricted: Boolean = false,
    val membershipState: String = "NOT_JOINED",
)

@Serializable
data class CommunityListDto(val items: List<CommunityDto> = emptyList())

@Serializable
data class MembershipDto(val membershipState: String, val memberCount: Int)

// ---- talk ----
@Serializable
data class CategoryDto(val id: String, val slug: String, val name: String)

@Serializable
data class CategoriesDto(val items: List<CategoryDto> = emptyList())

@Serializable
data class PostDto(
    val id: String,
    val authorId: String = "",
    val authorName: String = "",
    val authorLevel: String? = null,
    val categoryId: String = "",
    val categoryName: String = "",
    val communityId: String? = null,
    val body: String = "",
    val commentCount: Int = 0,
    val reactionCount: Int = 0,
    val myReaction: String? = null,
    val saved: Boolean = false,
    val createdAt: String = "",
)

@Serializable
data class PostPageDto(val items: List<PostDto> = emptyList(), val nextCursor: String? = null)

@Serializable
data class CommentDto(
    val id: String,
    val authorId: String = "",
    val authorName: String = "",
    val parentId: String? = null,
    val body: String = "",
    val createdAt: String = "",
)

@Serializable
data class CommentPageDto(val items: List<CommentDto> = emptyList(), val nextCursor: String? = null)

@Serializable
data class CreatePostRequestDto(val categoryId: String, val body: String, val communityId: String? = null)

@Serializable
data class CreateCommentRequestDto(
    val body: String,
    val parentId: String? = null,
    val mentionUserIds: List<String> = emptyList(),
)

@Serializable
data class ReactionRequestDto(val kind: String)

@Serializable
data class ReportRequestDto(
    val subjectType: String,
    val subjectId: String,
    val reason: String,
    val detail: String? = null,
)

// ---- referrals ----
@Serializable
data class MilestoneRewardDto(
    val kind: String,
    val credits: Int? = null,
    val amountMinor: Long? = null,
    val currency: String? = null,
    val statusGrant: String? = null,
    val perkText: String? = null,
)

@Serializable
data class NextMilestoneDto(
    val thresholdActive: Int,
    val currentActive: Int,
    val remaining: Int,
    val progressPct: Int,
    val rewards: List<MilestoneRewardDto> = emptyList(),
)

@Serializable
data class ReferralSummaryDto(
    val code: String = "",
    val link: String = "",
    val invited: Int = 0,
    val active: Int = 0,
    val credits: Long = 0,
    val cashEarnedMinor: Long = 0,
    val cashCurrency: String = "EUR",
    val ambassadorStatus: String = "NONE",
    val nextMilestone: NextMilestoneDto? = null,
    val disclosures: List<String> = emptyList(),
)

@Serializable
data class MilestoneTierDto(
    val thresholdActive: Int,
    val rewards: List<MilestoneRewardDto> = emptyList(),
    val state: String = "LOCKED",
)

@Serializable
data class ReferralHistoryItemDto(val id: String, val displayStatus: String, val createdAt: String)

@Serializable
data class ReferralListDto(val items: List<ReferralHistoryItemDto> = emptyList())

@Serializable
data class ResolveDto(val valid: Boolean = false, val inviterFirstName: String? = null)

@Serializable
data class ShareEventDto(val channel: String)

@Serializable
data class FaqItemDto(val q: String = "", val a: String = "")

// ---- rewards ----
@Serializable
data class WayToEarnDto(val title: String, val credits: Long, val deepLink: String)

@Serializable
data class RewardTransactionDto(val id: String, val deltaCredits: Long, val reason: String, val createdAt: String)

@Serializable
data class RewardsSummaryDto(
    val creditsBalance: Long = 0,
    val waysToEarn: List<WayToEarnDto> = emptyList(),
    val recentTransactions: List<RewardTransactionDto> = emptyList(),
)

@Serializable
data class RewardTransactionsDto(val items: List<RewardTransactionDto> = emptyList())

// ---- notifications ----
@Serializable
data class NotificationDto(
    val id: String,
    val type: String = "SYSTEM",
    val title: String = "",
    val body: String = "",
    val deepLink: String? = null,
    val read: Boolean = false,
    val createdAt: String = "",
)

@Serializable
data class NotificationsDto(val items: List<NotificationDto> = emptyList(), val unreadCount: Int = 0)

@Serializable
data class PrefsDto(val master: Boolean = true, val categories: Map<String, Boolean> = emptyMap())
