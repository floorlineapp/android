package com.thefloor.app.core.demo

import com.thefloor.app.core.network.AuthResponseDto
import com.thefloor.app.core.network.CategoriesDto
import com.thefloor.app.core.network.CommentDto
import com.thefloor.app.core.network.CommentPageDto
import com.thefloor.app.core.network.CommunityDto
import com.thefloor.app.core.network.CommunityListDto
import com.thefloor.app.core.network.CompletionCardDto
import com.thefloor.app.core.network.CreateCommentRequestDto
import com.thefloor.app.core.network.CreatePostRequestDto
import com.thefloor.app.core.network.CreatePulseRequestDto
import com.thefloor.app.core.network.CreateSpotlightRequestDto
import com.thefloor.app.core.network.FaqItemDto
import com.thefloor.app.core.network.HomeDto
import com.thefloor.app.core.network.InviteMiniDto
import com.thefloor.app.core.network.MembershipDto
import com.thefloor.app.core.network.MilestoneRewardDto
import com.thefloor.app.core.network.MilestoneTierDto
import com.thefloor.app.core.network.NextMilestoneDto
import com.thefloor.app.core.network.NotificationDto
import com.thefloor.app.core.network.NotificationsDto
import com.thefloor.app.core.network.OkDto
import com.thefloor.app.core.network.PostDto
import com.thefloor.app.core.network.PostPageDto
import com.thefloor.app.core.network.PrefsDto
import com.thefloor.app.core.network.PrivacyRequestDto
import com.thefloor.app.core.network.ProfileDto
import com.thefloor.app.core.network.PublicConfigDto
import com.thefloor.app.core.network.PulseDto
import com.thefloor.app.core.network.PulsePageDto
import com.thefloor.app.core.network.ReferralListDto
import com.thefloor.app.core.network.ReferralSummaryDto
import com.thefloor.app.core.network.RewardTransactionDto
import com.thefloor.app.core.network.RewardTransactionsDto
import com.thefloor.app.core.network.RewardsSummaryDto
import com.thefloor.app.core.network.SpotlightListDto
import com.thefloor.app.core.network.SpotlightSubmissionDto
import com.thefloor.app.core.network.SupportConversationDto
import com.thefloor.app.core.network.SupportMessageDto
import com.thefloor.app.core.network.SupportSendRequestDto
import com.thefloor.app.core.network.TokenPairDto
import com.thefloor.app.core.network.UpdateProfileRequestDto
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class DemoBackend @Inject constructor() {
    private val json = Json { encodeDefaults = true; explicitNulls = false }
    private val lock = Any()

    private var profile = seedProfile()
    private val communities = seedCommunities().toMutableList()
    private val posts = seedPosts().toMutableList()
    private val comments = seedComments()
    private val pulses = seedPulses().toMutableList()
    private val notifications = seedNotifications().toMutableList()
    private val transactions = seedTransactions().toMutableList()
    private val spotlight = seedSpotlight().toMutableList()
    private val supportMessages = seedSupport().toMutableList()
    private var supportStatus = "open"
    private var supportReference: String? = null
    private var prefs = PrefsDto(master = true, categories = mapOf("talk" to true, "rewards" to true, "floors" to true))
    private var nextId = 1000

    /** Talk discussions are capped at two a day, enforced server-side. */
    private var discussionsToday = 0

    private fun id(prefix: String): String = synchronized(lock) { "$prefix${nextId++}" }

    private val balance: Long get() = transactions.sumOf { it.deltaCredits }

    fun handle(method: String, seg: List<String>, query: (String) -> String?, body: String): String =
        synchronized(lock) { route(method, seg, query, body) }

    private fun route(method: String, seg: List<String>, query: (String) -> String?, body: String): String {
        val ok = json.encodeToString(OkDto.serializer(), OkDto(true))
        return when {
            seg.startsWith("auth", "login") || seg.startsWith("auth", "signup") ->
                json.encodeToString(
                    AuthResponseDto.serializer(),
                    AuthResponseDto(DemoMode.USER_ID, emailVerified = true, tokens = tokens()),
                )
            seg.startsWith("auth", "refresh") -> json.encodeToString(TokenPairDto.serializer(), tokens())
            seg.firstOrNull() == "auth" -> ok

            seg == listOf("config") -> json.encodeToString(PublicConfigDto.serializer(), config())

            seg == listOf("home") -> json.encodeToString(HomeDto.serializer(), home())

            seg == listOf("users", "me") -> json.encodeToString(ProfileDto.serializer(), profile)
            seg.startsWith("users", "me") && method != "GET" -> {
                when (seg.getOrNull(2)) {
                    "profile" -> applyProfileUpdate(body)
                    "privacy" -> applyPrivacyUpdate(body)
                }
                json.encodeToString(ProfileDto.serializer(), profile)
            }
            seg.size == 2 && seg[0] == "users" -> json.encodeToString(ProfileDto.serializer(), profile)
            seg.size == 3 && seg[0] == "users" && seg[2] == "block" -> ok

            seg == listOf("communities") || seg == listOf("communities", "suggested") -> {
                val term = query("query")?.trim()?.lowercase().orEmpty()
                val kind = query("kind")
                val filtered = communities.filter { c ->
                    (kind == null || c.kind == kind) &&
                        (term.isEmpty() || c.name.lowercase().contains(term) || c.description.lowercase().contains(term))
                }
                json.encodeToString(CommunityListDto.serializer(), CommunityListDto(filtered))
            }
            seg.size == 3 && seg[0] == "communities" && seg[2] == "join" ->
                json.encodeToString(MembershipDto.serializer(), setMembership(seg[1], join = method == "POST"))
            seg.size == 2 && seg[0] == "communities" ->
                json.encodeToString(
                    CommunityDto.serializer(),
                    communities.firstOrNull { it.id == seg[1] } ?: communities.first(),
                )

            seg == listOf("talk", "categories") ->
                json.encodeToString(CategoriesDto.serializer(), CategoriesDto(CATEGORIES))
            seg == listOf("talk", "posts") && method == "POST" ->
                json.encodeToString(PostDto.serializer(), createPost(body))
            seg == listOf("talk", "posts") ->
                json.encodeToString(
                    PostPageDto.serializer(),
                    PostPageDto(
                        query("category").let { cat -> posts.filter { cat == null || it.categoryId == cat } },
                    ),
                )
            seg.size == 4 && seg[0] == "talk" && seg[3] == "comments" && method == "POST" ->
                json.encodeToString(CommentDto.serializer(), createComment(seg[2], body))
            seg.size == 4 && seg[0] == "talk" && seg[3] == "comments" ->
                json.encodeToString(
                    CommentPageDto.serializer(),
                    CommentPageDto(comments[seg[2]].orEmpty()),
                )
            seg.size == 4 && seg[0] == "talk" && seg[3] == "reaction" -> {
                setReaction(seg[2], method == "PUT"); ok
            }
            seg.size == 4 && seg[0] == "talk" && seg[3] == "save" -> {
                setSaved(seg[2], method == "POST"); ok
            }
            seg.size == 3 && seg[0] == "talk" && seg[1] == "posts" && method == "DELETE" -> {
                posts.removeAll { it.id == seg[2] }; ok
            }
            seg.size == 3 && seg[0] == "talk" && seg[1] == "posts" ->
                json.encodeToString(
                    PostDto.serializer(),
                    posts.firstOrNull { it.id == seg[2] } ?: posts.first(),
                )
            seg.size == 3 && seg[0] == "talk" && seg[1] == "comments" && method == "DELETE" -> {
                comments.values.forEach { list -> list.removeAll { it.id == seg[2] } }; ok
            }

            seg == listOf("pulse") && method == "POST" ->
                json.encodeToString(PulseDto.serializer(), createPulse(body))
            seg == listOf("pulse") ->
                json.encodeToString(PulsePageDto.serializer(), PulsePageDto(pulses))
            seg.size == 3 && seg[0] == "pulse" && seg[2] == "like" -> {
                setLiked(seg[1], method == "PUT"); ok
            }
            seg.size == 2 && seg[0] == "pulse" && method == "DELETE" -> {
                pulses.removeAll { it.id == seg[1] }; ok
            }

            seg == listOf("rewards", "summary") ->
                json.encodeToString(
                    RewardsSummaryDto.serializer(),
                    RewardsSummaryDto(balance, WAYS_TO_EARN, transactions.take(5)),
                )
            seg == listOf("rewards", "transactions") ->
                json.encodeToString(RewardTransactionsDto.serializer(), RewardTransactionsDto(transactions))

            seg == listOf("referrals", "summary") ->
                json.encodeToString(ReferralSummaryDto.serializer(), referrals())
            seg == listOf("referrals", "milestones") ->
                json.encodeToString(ListSerializer(MilestoneTierDto.serializer()), MILESTONES)
            seg == listOf("referrals") ->
                json.encodeToString(ReferralListDto.serializer(), ReferralListDto(REFERRAL_HISTORY))
            seg == listOf("referrals", "faq") ->
                json.encodeToString(ListSerializer(FaqItemDto.serializer()), FAQ)
            seg.firstOrNull() == "referrals" -> ok

            seg == listOf("spotlight", "submissions") && method == "POST" ->
                json.encodeToString(SpotlightSubmissionDto.serializer(), createSubmission(body))
            seg == listOf("spotlight", "submissions") ->
                json.encodeToString(SpotlightListDto.serializer(), SpotlightListDto(spotlight))

            seg == listOf("support", "conversation") ->
                json.encodeToString(SupportConversationDto.serializer(), conversation())
            seg == listOf("support", "messages") ->
                json.encodeToString(SupportConversationDto.serializer(), handleSupport(body))

            seg == listOf("notifications", "preferences") && method != "GET" -> {
                runCatching { prefs = json.decodeFromString(PrefsDto.serializer(), body) }
                json.encodeToString(PrefsDto.serializer(), prefs)
            }
            seg == listOf("notifications", "preferences") ->
                json.encodeToString(PrefsDto.serializer(), prefs)
            seg == listOf("notifications", "read-all") -> {
                markAllRead(); ok
            }
            seg.size == 3 && seg[0] == "notifications" && seg[2] == "read" -> {
                markRead(seg[1]); ok
            }
            seg == listOf("notifications") ->
                json.encodeToString(
                    NotificationsDto.serializer(),
                    NotificationsDto(notifications, notifications.count { !it.read }),
                )

            else -> ok
        }
    }

    private fun List<String>.startsWith(vararg parts: String): Boolean =
        size >= parts.size && parts.withIndex().all { (i, p) -> this[i] == p }

    private fun createPost(body: String): PostDto {
        val req = runCatching {
            json.decodeFromString(CreatePostRequestDto.serializer(), body)
        }.getOrNull()
        val categoryId = req?.categoryId?.ifBlank { null } ?: CATEGORIES.first().id
        val post = PostDto(
            id = id("p"),
            authorId = DemoMode.USER_ID,
            authorName = profile.displayName,
            authorLevel = profile.careerLevel,
            authorTier = myTier(),
            authorCountry = countryCode(profile.country),
            categoryId = categoryId,
            categoryName = CATEGORIES.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            communityId = "za",
            body = req?.body.orEmpty(),
            commentCount = 0,
            reactionCount = 0,
            createdAt = now(),
        )
        posts.add(0, post)
        if (discussionsToday < 2) {
            discussionsToday++
            award(10, "Discussion posted in ${post.categoryName}")
        }
        return post
    }

    private fun createComment(postId: String, body: String): CommentDto {
        val req = runCatching {
            json.decodeFromString(CreateCommentRequestDto.serializer(), body)
        }.getOrNull()
        val comment = CommentDto(
            id = id("cm"),
            authorId = DemoMode.USER_ID,
            authorName = profile.displayName,
            authorTier = myTier(),
            authorCountry = countryCode(profile.country),
            parentId = req?.parentId,
            body = req?.body.orEmpty(),
            createdAt = now(),
        )
        comments.getOrPut(postId) { mutableListOf() }.add(comment)
        replacePost(postId) { it.copy(commentCount = it.commentCount + 1) }
        return comment
    }

    private fun createPulse(body: String): PulseDto {
        val req = runCatching {
            json.decodeFromString(CreatePulseRequestDto.serializer(), body)
        }.getOrNull()
        val pulse = PulseDto(
            id = id("x"),
            authorId = DemoMode.USER_ID,
            authorName = profile.displayName,
            authorTier = myTier(),
            authorCountry = countryCode(profile.country),
            body = req?.body.orEmpty().take(220),
            mediaUrl = req?.mediaUrl,
            mediaType = req?.mediaType,
            likeCount = 0,
            liked = false,
            createdAt = now(),
        )
        pulses.add(0, pulse)
        return pulse
    }

    private fun setMembership(communityId: String, join: Boolean): MembershipDto {
        val index = communities.indexOfFirst { it.id == communityId }.takeIf { it >= 0 } ?: 0
        val current = communities[index]
        val updated = current.copy(
            membershipState = if (join) "JOINED" else "NOT_JOINED",
            memberCount = (current.memberCount + if (join) 1 else -1).coerceAtLeast(0),
        )
        communities[index] = updated
        return MembershipDto(updated.membershipState, updated.memberCount)
    }

    private fun setReaction(postId: String, on: Boolean) = replacePost(postId) {
        it.copy(
            myReaction = if (on) "LIKE" else null,
            reactionCount = (it.reactionCount + if (on) 1 else -1).coerceAtLeast(0),
        )
    }

    private fun setSaved(postId: String, on: Boolean) = replacePost(postId) { it.copy(saved = on) }

    private fun setLiked(pulseId: String, on: Boolean) {
        val i = pulses.indexOfFirst { it.id == pulseId }
        if (i < 0) return
        val p = pulses[i]
        pulses[i] = p.copy(liked = on, likeCount = (p.likeCount + if (on) 1 else -1).coerceAtLeast(0))
    }

    private fun replacePost(postId: String, transform: (PostDto) -> PostDto) {
        val i = posts.indexOfFirst { it.id == postId }
        if (i >= 0) posts[i] = transform(posts[i])
    }

    private fun markRead(notificationId: String) {
        val i = notifications.indexOfFirst { it.id == notificationId }
        if (i >= 0) notifications[i] = notifications[i].copy(read = true)
    }

    private fun markAllRead() {
        for (i in notifications.indices) notifications[i] = notifications[i].copy(read = true)
    }

    /** A submission is a record, not a screen state. */
    private fun createSubmission(body: String): SpotlightSubmissionDto {
        val req = runCatching {
            json.decodeFromString(CreateSpotlightRequestDto.serializer(), body)
        }.getOrNull()
        val submission = SpotlightSubmissionDto(
            id = id("sp"),
            category = req?.category.orEmpty(),
            title = req?.title.orEmpty(),
            story = req?.story.orEmpty(),
            proofText = req?.proofText.orEmpty(),
            mediaUrl = req?.mediaUrl,
            company = profile.employer.orEmpty(),
            country = profile.country.orEmpty(),
            status = "pending",
            createdAt = now(),
        )
        spotlight.add(0, submission)
        notifications.add(
            0,
            NotificationDto(
                id = id("n"),
                type = "SPOTLIGHT",
                title = "Your Spotlight is in review",
                body = submission.title,
                deepLink = "thefloor://insights",
                read = false,
                createdAt = now(),
            ),
        )
        return submission
    }

    private fun conversation() = SupportConversationDto(
        id = "conv-demo",
        status = supportStatus,
        reference = supportReference,
        messages = supportMessages,
    )

    /** Walker's first line. */
    private fun handleSupport(body: String): SupportConversationDto {
        val req = runCatching {
            json.decodeFromString(SupportSendRequestDto.serializer(), body)
        }.getOrNull()
        val text = req?.body.orEmpty().trim()

        if (text.isNotEmpty()) {
            supportMessages.add(SupportMessageDto(id("m"), "user", text, now()))
        }

        if (req?.escalate == true) {
            if (supportStatus != "escalated") {
                supportStatus = "escalated"
                supportReference = "WLK-%04d".format((1000..9999).random())
                supportMessages.add(
                    SupportMessageDto(
                        id("m"), "agent",
                        "Passed to the Walker team. Your reference is $supportReference — quote it " +
                            "if you follow up. Replies land in this thread; support runs across " +
                            "timezones, so it may not be instant.",
                        now(),
                    ),
                )
            }
            return conversation()
        }

        if (text.isNotEmpty()) {
            supportMessages.add(SupportMessageDto(id("m"), "ai", walkerAnswer(text), now()))
            if (supportStatus == "open") supportStatus = "ai_handled"
        }
        return conversation()
    }

    private fun walkerAnswer(question: String): String {
        val t = question.lowercase()
        fun has(vararg keys: String) = keys.any { t.contains(it) }
        return when {
            has("point", "balance", "pts", "credit") ->
                "Your balance is %,d Floor Points right now. Every award carries a source and a timestamp, so a missing one can be traced — tell me which action and roughly when.".format(balance)
            has("verif", "workplace", "badge") -> {
                val verified = profile.emailVerified && !profile.employer.isNullOrBlank()
                if (verified) {
                    "Your workplace is verified as ${profile.employer}. That is what unlocks Recognised Member and Spotlight submission."
                } else {
                    "Your workplace is not verified yet. Add your employer under Profile → Edit; a work email on your employer's domain is the quickest route."
                }
            }
            has("tier", "level", "recognition", "ambassador", "floor voice") ->
                "You are ${myTier()} at %,d points. The ladder is Contributor 1,000, Recognised Member 5,000 with a verified workplace, Floor Voice 10,000, Workplace Ambassador 15,000 with a trusted history.".format(balance)
            has("spotlight", "submit", "story") ->
                "Spotlight submission unlocks at Floor Voice — 10,000 points and a verified workplace. Every submission is read by a person, and an approved story pays +75 back into your ledger."
            has("floor", "communit", "join", "group") ->
                "Joining a Floor is one tap with no approval step. If the one you want does not exist, use Suggest a Floor on The Floor tab and it comes to us."
            has("refer", "invite", "commission", "payout") ->
                "Invite & Grow is a separate record from Floor Points on purpose. Referrals never add points and never buy stature — no pay-to-play, no downlines, no commissions."
            has("radio", "pass", "listen", "subscri") ->
                "Floor Radio runs six regional feeds and stays free. The Radio Pass is not on sale yet — no price and no payment provider — so nothing can be charged to you today."
            has("password", "login", "log in", "sign in", "locked") ->
                "I cannot reset a password from here. The Forgot password link on the login screen emails a reset link; if it does not arrive, check spam and tell me which address you used."
            has("delete", "close my account", "leave") ->
                "Settings → Delete account removes your profile and your posts. If you would rather just step back quietly, Profile → Privacy lets you narrow who sees what instead."
            has("pulse") ->
                "Pulse earns no Floor Points — deliberately. It is the low-stakes side of the app. A considered opinion on Talk earns +10, and an answer someone marks Helpful earns +25."
            has("human", "person", "agent", "someone", "real") ->
                "I can hand this to the Walker team — use Talk to a person below and the whole conversation goes with it, along with a reference number."
            else ->
                "I have not got a confident answer for that one. Use Talk to a person below and the Walker team picks it up with this conversation attached."
        }
    }

    /** My tier, from the same computation Profile and Spotlight use. */
    private fun myTier(): String {
        val workplaceVerified = profile.emailVerified && !profile.employer.isNullOrBlank()
        return com.thefloor.app.core.model.recognitionLevel(
            floorPoints = balance.toInt(),
            workplaceVerified = workplaceVerified,
            trustedHistory = workplaceVerified && profile.completeness >= 80,
        ).label
    }

    /** Profiles store a country name; the flag needs the ISO code. */
    private fun countryCode(name: String?): String? = when (name?.lowercase()) {
        null -> null
        "south africa" -> "ZA"
        "philippines" -> "PH"
        "india" -> "IN"
        "kenya" -> "KE"
        "colombia" -> "CO"
        "mexico" -> "MX"
        "egypt" -> "EG"
        "poland" -> "PL"
        "brazil" -> "BR"
        "united states" -> "US"
        else -> name.takeIf { it.length == 2 }?.uppercase()
    }

    /** The only path that writes the ledger — as on the real backend. */
    private fun award(amount: Long, reason: String) {
        transactions.add(0, RewardTransactionDto(id("t"), amount, reason, now()))
        notifications.add(
            0,
            NotificationDto(
                id = id("n"),
                type = "REWARD",
                title = "You earned $amount Floor Points",
                body = reason,
                deepLink = "thefloor://rewards",
                read = false,
                createdAt = now(),
            ),
        )
    }

    private fun applyProfileUpdate(body: String) {
        val u = runCatching {
            json.decodeFromString(UpdateProfileRequestDto.serializer(), body)
        }.getOrNull() ?: return
        profile = profile.copy(
            displayName = u.displayName ?: profile.displayName,
            country = u.country ?: profile.country,
            city = u.city ?: profile.city,
            ageRange = u.ageRange ?: profile.ageRange,
            languages = u.languages ?: profile.languages,
            employer = u.employer ?: profile.employer,
            site = u.site ?: profile.site,
            industry = u.industry ?: profile.industry,
            role = u.role ?: profile.role,
            careerLevel = u.careerLevel ?: profile.careerLevel,
            experienceYears = u.experienceYears ?: profile.experienceYears,
            channels = u.channels ?: profile.channels,
            workMode = u.workMode ?: profile.workMode,
            skills = u.skills ?: profile.skills,
        )
        profile = profile.copy(completeness = completeness(profile))
    }

    /** Visibility is a merge, not a replace: the screen sends the one field the member just changed, and the… */
    private fun applyPrivacyUpdate(body: String) {
        val request = runCatching {
            json.decodeFromString(PrivacyRequestDto.serializer(), body)
        }.getOrNull() ?: return
        profile = profile.copy(visibility = profile.visibility + request.visibility)
    }

    private fun completeness(p: ProfileDto): Int {
        val filled = listOf(
            p.country, p.city, p.ageRange, p.employer, p.site, p.industry, p.role, p.careerLevel, p.workMode,
        ).count { !it.isNullOrBlank() } +
            listOf(p.languages, p.skills, p.channels).count { it.isNotEmpty() }
        return (filled * 100 / 12).coerceIn(0, 100)
    }

    private fun tokens() = TokenPairDto("demo.access.token", "demo.refresh.token", 86_400)

    private fun config() = PublicConfigDto(
        flags = mapOf(
            "radio" to true, "academy" to true, "jobs" to true,
            "insights" to true, "marketplace" to true, "payouts" to true,
        ),
        minAppVersion = 1,
        disclosures = listOf(
            "Demo build — all data on this device is sample content.",
            "Free to refer. Nobody pays to unlock earning.",
        ),
        legalUrls = mapOf(
            "terms" to "https://thefloor.app/legal/terms",
            "privacy" to "https://thefloor.app/legal/privacy",
        ),
    )

    private fun home() = HomeDto(
        displayName = profile.displayName,
        presenceCount = 2_347,
        activeConversations = posts.sumOf { it.commentCount },
        newOpportunities = 24,
        pointsAvailableToday = 160,
        presenceNames = listOf("Thabo N.", "Grace A.", "Owen K.", "Priya S.", "Mika R.", "Carmen V.", "Joan D.", "Lerato K."),
        postsSinceYesterday = posts.count { within(it.createdAt, hours = 24) },
        mentions = notifications.count { !it.read && it.type == "TALK" },
        pointsEarnedToday = transactions
            .filter { it.deltaCredits > 0 && within(it.createdAt, hours = 24) }
            .sumOf { it.deltaCredits }
            .toInt(),
        startedDiscussion = posts.any { it.authorId == DemoMode.USER_ID },
        completionCards = if (profile.completeness >= 100) {
            emptyList()
        } else {
            listOf(
                CompletionCardDto(
                    "Complete your profile",
                    "Add your workplace to see who else works there.",
                    "thefloor://profile/edit",
                ),
            )
        },
        myFloors = communities.filter { it.membershipState == "JOINED" },
        trendingPosts = posts.sortedByDescending { it.reactionCount }.take(3),
        inviteEarn = InviteMiniDto(invited = 12, active = 5),
        creditsBalance = balance,
        flags = config().flags,
    )

    private fun referrals() = ReferralSummaryDto(
        code = "FLOORDEMO",
        link = "https://thefloor.app/invite/FLOORDEMO",
        invited = 12,
        active = 5,
        credits = 0,
        cashEarnedMinor = 0,
        cashCurrency = "EUR",
        ambassadorStatus = "CONNECTOR",
        nextMilestone = NextMilestoneDto(
            thresholdActive = 25,
            currentActive = 5,
            remaining = 20,
            progressPct = 20,
            rewards = listOf(
                MilestoneRewardDto(kind = "STATUS", statusGrant = "AMBASSADOR"),
                MilestoneRewardDto(kind = "PERK", perkText = "Founding Ambassador badge on your profile"),
            ),
        ),
        disclosures = listOf(
            "Invite & Grow never adds Floor Points and never buys professional stature.",
            "No pay-to-play. No downlines. No commissions from other people's referrals.",
            "Founding tiers are recognition. The Community Growth Fund is not funded yet.",
        ),
    )

    /** True when an ISO timestamp is inside the last [hours]. */
    private fun within(iso: String, hours: Long): Boolean = runCatching {
        Instant.parse(iso).isAfter(Instant.now().minus(hours, ChronoUnit.HOURS))
    }.getOrDefault(false)
}
