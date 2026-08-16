package com.thefloor.app.core.data

import com.thefloor.app.core.model.CareerLevel
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.model.MembershipState
import com.thefloor.app.core.model.MilestoneReward
import com.thefloor.app.core.model.MilestoneTier
import com.thefloor.app.core.model.Money
import com.thefloor.app.core.model.NextMilestone
import com.thefloor.app.core.model.Post
import com.thefloor.app.core.model.ReferralSummary
import com.thefloor.app.core.model.UserProfile
import com.thefloor.app.core.model.WorkMode
import com.thefloor.app.core.network.CommunityDto
import com.thefloor.app.core.network.MilestoneRewardDto
import com.thefloor.app.core.network.MilestoneTierDto
import com.thefloor.app.core.network.NextMilestoneDto
import com.thefloor.app.core.network.PostDto
import com.thefloor.app.core.network.ProfileDto
import com.thefloor.app.core.network.ReferralSummaryDto

/** DTO → domain. Enums parse defensively: unknown server values degrade, never crash. */

fun ProfileDto.toDomain() = UserProfile(
    userId = userId,
    displayName = displayName,
    photoUrl = photoUrl,
    country = country,
    city = city,
    languages = languages,
    employer = employer,
    site = site,
    industry = industry,
    role = role,
    careerLevel = careerLevel?.let { runCatching { CareerLevel.valueOf(it) }.getOrNull() },
    experienceYears = experienceYears,
    workMode = workMode?.let { runCatching { WorkMode.valueOf(it) }.getOrNull() },
    skills = skills,
    completeness = completeness,
    emailVerified = emailVerified,
    visibility = visibility,
)

fun CommunityDto.toDomain() = Community(
    id = id,
    slug = slug,
    name = name,
    description = description,
    kind = kind,
    memberCount = memberCount,
    isRestricted = isRestricted,
    membershipState = runCatching { MembershipState.valueOf(membershipState) }
        .getOrDefault(MembershipState.NOT_JOINED),
)

fun PostDto.toDomain() = Post(
    id = id,
    authorId = authorId,
    authorName = authorName,
    authorLevel = authorLevel?.let { runCatching { CareerLevel.valueOf(it) }.getOrNull() },
    categoryId = categoryId,
    categoryName = categoryName,
    communityId = communityId,
    body = body,
    commentCount = commentCount,
    reactionCount = reactionCount,
    myReaction = myReaction,
    saved = saved,
    createdAt = createdAt,
)

fun MilestoneRewardDto.toDomain() = MilestoneReward(
    kind = kind,
    credits = credits,
    cash = if (amountMinor != null && currency != null) Money(amountMinor, currency) else null,
    statusGrant = statusGrant,
    perkText = perkText,
)

fun NextMilestoneDto.toDomain() = NextMilestone(
    thresholdActive = thresholdActive,
    currentActive = currentActive,
    remaining = remaining,
    progressPct = progressPct,
    rewards = rewards.map { it.toDomain() },
)

fun MilestoneTierDto.toDomain() = MilestoneTier(
    thresholdActive = thresholdActive,
    rewards = rewards.map { it.toDomain() },
    state = state,
)

fun ReferralSummaryDto.toDomain() = ReferralSummary(
    code = code,
    link = link,
    invited = invited,
    active = active,
    credits = credits,
    cashEarned = Money(cashEarnedMinor, cashCurrency),
    ambassadorStatus = ambassadorStatus,
    nextMilestone = nextMilestone?.toDomain(),
    disclosures = disclosures,
)
