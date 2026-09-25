package com.thefloor.app.core.demo

import androidx.compose.runtime.getValue
import com.thefloor.app.core.network.AuthResponseDto
import com.thefloor.app.core.network.CategoriesDto
import com.thefloor.app.core.network.CategoryDto
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
import com.thefloor.app.core.network.ReferralHistoryItemDto
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
import com.thefloor.app.core.network.WayToEarnDto
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/** Answers every API call locally when [DemoMode] is on, so the app is fully explorable with no backend. */
@Singleton
class DemoInterceptor @Inject constructor(
    private val backend: DemoBackend,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!DemoMode.enabled) return chain.proceed(request)

        val segments = request.url.encodedPath.trim('/').split('/').filter { it.isNotBlank() }
            .let { if (it.firstOrNull() == "v1") it.drop(1) else it }
        val requestBody = request.body?.let { body ->
            okio.Buffer().also { runCatching { body.writeTo(it) } }.readUtf8()
        }.orEmpty()

        val json = backend.handle(request.method, segments, request.url::queryParameter, requestBody)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK (demo)")
            .body(json.toResponseBody(JSON_MEDIA))
            .build()
    }

    private companion object {
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}

/** The demo's mutable world. */
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

    private fun now(): String = Instant.now().toString()

    private fun ago(hours: Long): String = Instant.now().minus(hours, ChronoUnit.HOURS).toString()

    /** True when an ISO timestamp is inside the last [hours]. */
    private fun within(iso: String, hours: Long): Boolean = runCatching {
        Instant.parse(iso).isAfter(Instant.now().minus(hours, ChronoUnit.HOURS))
    }.getOrDefault(false)

    private fun seedProfile() = ProfileDto(
        userId = DemoMode.USER_ID,
        displayName = "Naledi M.",
        country = "South Africa",
        city = "Johannesburg",
        ageRange = "25–34",
        languages = listOf("English", "Zulu"),
        employer = "Meridian Contact Solutions",
        site = "Rosebank Contact Center",
        industry = "Telecom",
        role = "Senior Agent",
        careerLevel = "SENIOR_AGENT",
        experienceYears = 4,
        channels = listOf("Voice", "Chat"),
        workMode = "HYBRID",
        skills = listOf("De-escalation", "Billing systems", "CRM", "Coaching new hires"),
        completeness = 82,
        emailVerified = true,
        visibility = mapOf("employer" to "MEMBERS", "city" to "PUBLIC", "country" to "PUBLIC", "role" to "PUBLIC"),
    )

    private fun community(
        id: String, name: String, desc: String, kind: String,
        members: Int, state: String, photo: String,
    ) = CommunityDto(
        id = id, slug = id, name = name, description = desc, kind = kind,
        memberCount = members, isRestricted = false, membershipState = state,
        imageUrl = "https://images.unsplash.com/$photo?w=1200&q=80&auto=format&fit=crop",
    )

    private fun seedCommunities() = listOf(
        community("za", "South Africa Floor", "From Cape Town to Joburg, Durban to everywhere in between.", "COUNTRY", 23_461, "JOINED", "photo-1602578984228-c98a9b995f3e"),
        community("ph", "Philippines Floor", "The heart of global outsourcing. Always on, always awake.", "COUNTRY", 41_208, "NOT_JOINED", "photo-1623518761090-089a985ab17f"),
        community("co", "Colombia Floor", "Passion. Resilience. World-class customer experience.", "COUNTRY", 17_894, "NOT_JOINED", "photo-1681145553138-816eb004b846"),
        community("in", "India Floor", "Talent. Technology. Tomorrow starts here.", "COUNTRY", 142_388, "NOT_JOINED", "photo-1545562083-c583d014b4f2"),
        community("mx", "Mexico Floor", "Bridging cultures. Delivering excellence.", "COUNTRY", 15_662, "NOT_JOINED", "photo-1591049433264-618fa2f4558f"),
        community("eg", "Egypt Floor", "Connecting the world from the heart of Egypt.", "COUNTRY", 12_807, "NOT_JOINED", "photo-1568322445389-f64ac2515020"),
        community("pl", "Poland Floor", "European quality. Global impact.", "COUNTRY", 8_945, "NOT_JOINED", "photo-1651062108412-36a68f3748dd"),
        community("br", "Brazil Floor", "Empathy. Energy. Extraordinary customer connections.", "COUNTRY", 19_774, "NOT_JOINED", "photo-1483729558449-99ef09a8c325"),
        community("ke", "Kenya Floor", "Africa's rising delivery hub.", "COUNTRY", 6_410, "NOT_JOINED", "photo-1611348586804-61bf6c080437"),
        community("us", "United States Floor", "Innovation. Service. Leadership.", "COUNTRY", 31_589, "NOT_JOINED", "photo-1485738422979-f5c462d49f74"),
        community("rm", "Remote Floor", "Work from anywhere. Belong everywhere.", "GLOBAL", 21_270, "JOINED", "photo-1616531770192-6eaea74c2456"),
        community("ns", "Night Shift Warriors", "For the people keeping the world awake.", "SHIFT", 9_328, "NOT_JOINED", "photo-1544202482-5e970f02d8e7"),
        community("na", "New Agents", "First week on the phones? Start here.", "CAREER_LEVEL", 12_044, "NOT_JOINED", "photo-1600880292203-757bb62b4baf"),
        community("tl", "Team Leaders", "Coaching, metrics and leading a floor of your own.", "CAREER_LEVEL", 7_781, "NOT_JOINED", "photo-1542744173-8e7e53415bb0"),
        community("gl", "The Floor — Global", "The global home of the people behind every customer conversation.", "GLOBAL", 128_400, "JOINED", "photo-1521737604893-d14cc237f11d"),
    )

    private fun post(
        id: String, author: String, level: String, cat: String,
        body: String, comments: Int, reactions: Int, hours: Long,
        tier: String = "Contributor", country: String = "ZA",
    ) = PostDto(
        id = id, authorId = "u$id", authorName = author, authorLevel = level,
        authorTier = tier, authorCountry = country,
        categoryId = cat, categoryName = CATEGORIES.first { it.id == cat }.name,
        communityId = "za", body = body, commentCount = comments,
        reactionCount = reactions, saved = false, createdAt = ago(hours),
    )

    private fun seedPosts() = listOf(
        post("p1", "Mika R.", "TEAM_LEADER", "c1", "Should agents be penalised for AHT when the customer genuinely needs more time?", 118, 296, 3, "Workplace Ambassador", "ZA"),
        post("p2", "Thabo N.", "SENIOR_AGENT", "c2", "When does coaching become micromanagement? Where should a Team Leader draw the line?", 76, 184, 8, "Floor Voice", "ZA"),
        post("p3", "Priya S.", "SME", "c3", "Are BPO salaries keeping pace with what companies now expect agents to handle?", 129, 341, 14, "Floor Voice", "IN"),
        post("p4", "Owen K.", "AGENT", "c4", "AI quality scoring is here. Should an algorithm be allowed to affect an agent bonus?", 143, 267, 22, "Recognised Member", "PH"),
        post("p5", "Grace A.", "AGENT", "c3", "What should a real career path from Agent to Team Leader actually look like?", 82, 219, 30, "Contributor", "KE"),
        post("p6", "Carmen V.", "MANAGER", "c5", "Is the industry ready to give frontline employees a stronger voice in how operations are designed?", 105, 198, 46, "Workplace Ambassador", "CO"),
    )

    private fun seedComments(): MutableMap<String, MutableList<CommentDto>> = mutableMapOf(
        "p1" to mutableListOf(
            CommentDto(
                id = "cm1", authorId = "u9", authorName = "Sipho D.",
                authorTier = "Recognised Member", authorCountry = "ZA",
                body = "We measure AHT but never measure whether the problem actually got solved. That is the real gap.",
                createdAt = ago(2),
            ),
            CommentDto(
                id = "cm2", authorId = "u4", authorName = "Carmen V.",
                authorTier = "Workplace Ambassador", authorCountry = "CO",
                body = "My TL protects us from the stopwatch and our CSAT is the highest on site. It can be done.",
                createdAt = ago(1),
            ),
        ),
        "p2" to mutableListOf(
            CommentDto(
                id = "cm3", authorId = "u5", authorName = "Joan D.",
                authorTier = "Floor Voice", authorCountry = "PH",
                body = "Coaching is asking what I need. Micromanagement is telling me what I did wrong after the fact.",
                createdAt = ago(4),
            ),
        ),
    )

    private fun seedPulses() = listOf(
        PulseDto(
            id = "x1", authorId = "u2", authorName = "Thabo N.",
            authorTier = "Floor Voice", authorCountry = "ZA",
            body = "Third escalation before 9am and the coffee machine is broken. Send help.",
            likeCount = 24, liked = false, createdAt = ago(2),
        ),
        PulseDto(
            id = "x2", authorId = DemoMode.USER_ID, authorName = "Naledi M.",
            authorTier = "Floor Voice", authorCountry = "ZA",
            body = "Just closed the longest call of my life. 74 minutes. We got there.",
            likeCount = 61, liked = true, createdAt = ago(5),
        ),
        PulseDto(
            id = "x3", authorId = "u3", authorName = "Grace A.",
            authorTier = "Contributor", authorCountry = "KE",
            body = "Night shift crew — what are we listening to tonight?",
            likeCount = 12, liked = false, createdAt = ago(9),
        ),
        PulseDto(
            id = "x4", authorId = "u7", authorName = "Owen K.",
            authorTier = "Recognised Member", authorCountry = "PH",
            body = "Passed my QA review with 96%. Six months ago I was at 71%.",
            likeCount = 88, liked = false, createdAt = ago(20),
        ),
    )

    private fun seedNotifications() = listOf(
        NotificationDto("n1", "TALK", "Mika R. replied to your comment", "\"That is exactly the point I was making.\"", "thefloor://talk/p1", false, ago(4)),
        NotificationDto("n2", "REWARD", "You earned 50 Floor Points", "Verified profile completed.", "thefloor://rewards", false, ago(26)),
        NotificationDto("n3", "FLOOR", "Welcome to the South Africa Floor", "23,461 people are already here.", "thefloor://floor/za", true, ago(50)),
    )

    private fun seedSpotlight() = listOf(
        SpotlightSubmissionDto(
            id = "sp1",
            category = "People & Culture",
            title = "Night shift finally got a proper canteen",
            story = "Sixteen of us signed the case and facilities opened the kitchen from 10pm.",
            proofText = "Internal announcement and photographs.",
            company = "Meridian Contact Solutions",
            country = "South Africa",
            status = "approved",
            createdAt = ago(400),
        ),
        SpotlightSubmissionDto(
            id = "sp2",
            category = "Career Growth",
            title = "Four of my team made Team Leader this year",
            story = "Every one of them started on voice in the same intake as me.",
            proofText = "HR confirmation.",
            company = "Meridian Contact Solutions",
            country = "South Africa",
            status = "rejected",
            reviewerNote = "Needs the names removed or their written agreement attached.",
            createdAt = ago(300),
        ),
    )

    private fun seedSupport() = listOf(
        SupportMessageDto(
            id = "m0",
            sender = "ai",
            body = "Hi — I'm Walker. I can answer from what The Floor actually knows: your points, " +
                "your verification, your tier, how a feature works. If I can't, I'll pass it to a " +
                "person with a reference number.",
            createdAt = ago(1),
        ),
    )

    private fun seedTransactions() = listOf(
        RewardTransactionDto("t1", 150, "Friday Trivia winner", ago(48)),
        RewardTransactionDto("t2", -155, "Reward redemption", ago(72)),
        RewardTransactionDto("t3", 75, "Approved Workplace Spotlight", ago(96)),
        RewardTransactionDto("t4", 10, "Discussion posted in The Job", ago(120)),
        RewardTransactionDto("t5", 25, "Floor-verified learning: Soft Skills", ago(144)),
        RewardTransactionDto("t6", 50, "Verified profile completed", ago(200)),
        RewardTransactionDto("t7", 12_325, "Founding member balance carried over", ago(400)),
    )

    private companion object {
        val CATEGORIES = listOf(
            CategoryDto("c1", "the-job", "The Job"),
            CategoryDto("c2", "leadership", "Leadership"),
            CategoryDto("c3", "pay-progression", "Pay & Progression"),
            CategoryDto("c4", "ai-future", "AI & The Future"),
            CategoryDto("c5", "big-questions", "The Big Questions"),
        )

        val WAYS_TO_EARN = listOf(
            WayToEarnDto("Complete your verified profile — once", 50, "thefloor://profile/edit"),
            WayToEarnDto("Start a Talk discussion — up to 2 a day", 10, "thefloor://talk"),
            WayToEarnDto("Give an answer marked Helpful — capped daily", 25, "thefloor://talk"),
            WayToEarnDto("Floor-verified Academy learning — per item", 25, "thefloor://academy"),
            WayToEarnDto("Attend a verified Floor event — per event", 50, "thefloor://events"),
            WayToEarnDto("Workplace Spotlight approved — per story", 75, "thefloor://insights"),
            WayToEarnDto("Win an official game or competition", 150, "thefloor://rewards"),
        )

        val MILESTONES = listOf(
            MilestoneTierDto(5, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "CONNECTOR")), "REACHED"),
            MilestoneTierDto(25, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "AMBASSADOR")), "LOCKED"),
            MilestoneTierDto(100, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "SCOUT")), "LOCKED"),
            MilestoneTierDto(500, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "CAPTAIN")), "LOCKED"),
            MilestoneTierDto(1000, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "BUILDER")), "LOCKED"),
        )

        val REFERRAL_HISTORY = listOf(
            ReferralHistoryItemDto("r1", "Qualified", "2026-09-02T09:00:00Z"),
            ReferralHistoryItemDto("r2", "Qualified", "2026-09-05T09:00:00Z"),
            ReferralHistoryItemDto("r3", "Verified", "2026-09-09T09:00:00Z"),
            ReferralHistoryItemDto("r4", "Joined", "2026-09-14T09:00:00Z"),
            ReferralHistoryItemDto("r5", "Held for review", "2026-09-18T09:00:00Z"),
        )

        val FAQ = listOf(
            FaqItemDto("Does a referral ever add Floor Points?", "No. Invite & Grow is a separate record on purpose — referrals never add points and never buy professional stature."),
            FaqItemDto("What makes a referral 'qualified'?", "The person you invited was active on three separate days. A signup that never comes back does not count."),
            FaqItemDto("My referral says 'held for review'. Is it rejected?", "No — it is a delay, not a rejection. Referrals from shared workplace networks get a human sense-check, and most clear."),
            FaqItemDto("Is the Community Growth Fund paying out?", "Not yet. It is labelled BETA and NOT FUNDED because there is no commercial revenue behind it yet."),
        )
    }
}
