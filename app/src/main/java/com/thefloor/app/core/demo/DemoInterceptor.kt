package com.thefloor.app.core.demo

import com.thefloor.app.core.network.AuthResponseDto
import com.thefloor.app.core.network.CategoriesDto
import com.thefloor.app.core.network.CategoryDto
import com.thefloor.app.core.network.CommentDto
import com.thefloor.app.core.network.CommentPageDto
import com.thefloor.app.core.network.CommunityDto
import com.thefloor.app.core.network.CommunityListDto
import com.thefloor.app.core.network.CompletionCardDto
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
import com.thefloor.app.core.network.TokenPairDto
import com.thefloor.app.core.network.WayToEarnDto
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Answers every API call locally when [DemoMode] is on, so the app is fully
 * explorable with no backend.
 *
 * This is a small in-memory server, not a table of canned strings: it keeps
 * state and honours the HTTP method, so posting a discussion, commenting,
 * joining a Floor, reacting, editing a profile and redeeming all behave the way
 * they will against the real API — the new row comes back and the feed it
 * belongs to shows it on the next read.
 *
 * Responses are built from the real DTO types, so a shape change in the API
 * contract breaks this at compile time rather than at runtime in someone's
 * hands.
 *
 * The points rules live here rather than in the app, exactly as the process
 * guide requires of the real backend: the client never writes the ledger, it
 * only reads it back. The daily cap on Talk discussions is enforced here too.
 */
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

/**
 * The demo's mutable world. Singleton so it survives navigation; reset only by
 * a process restart, which is the right lifetime for a build people pass
 * around — an evening of poking at it keeps everything they did.
 */
@Singleton
class DemoBackend @Inject constructor() {

    private val json = Json { encodeDefaults = true; explicitNulls = false }
    private val lock = Any()

    // ---- state ----------------------------------------------------------
    private var profile = seedProfile()
    private val communities = seedCommunities().toMutableList()
    private val posts = seedPosts().toMutableList()
    private val comments = seedComments()
    private val pulses = seedPulses().toMutableList()
    private val notifications = seedNotifications().toMutableList()
    private val transactions = seedTransactions().toMutableList()
    private var prefs = PrefsDto(master = true, categories = mapOf("talk" to true, "rewards" to true, "floors" to true))
    private var nextId = 1000

    /** Talk discussions are capped at two a day, enforced server-side. */
    private var discussionsToday = 0

    private fun id(prefix: String): String = synchronized(lock) { "$prefix${nextId++}" }

    private val balance: Long get() = transactions.sumOf { it.deltaCredits }

    // ---- routing --------------------------------------------------------

    fun handle(method: String, seg: List<String>, query: (String) -> String?, body: String): String =
        synchronized(lock) { route(method, seg, query, body) }

    private fun route(method: String, seg: List<String>, query: (String) -> String?, body: String): String {
        val ok = json.encodeToString(OkDto.serializer(), OkDto(true))
        return when {
            // ---- auth ----
            seg.startsWith("auth", "login") || seg.startsWith("auth", "signup") ->
                json.encodeToString(
                    AuthResponseDto.serializer(),
                    AuthResponseDto(DemoMode.USER_ID, emailVerified = true, tokens = tokens()),
                )
            seg.startsWith("auth", "refresh") -> json.encodeToString(TokenPairDto.serializer(), tokens())
            seg.firstOrNull() == "auth" -> ok

            // ---- config ----
            seg == listOf("config") -> json.encodeToString(PublicConfigDto.serializer(), config())

            // ---- home ----
            seg == listOf("home") -> json.encodeToString(HomeDto.serializer(), home())

            // ---- profile ----
            seg == listOf("users", "me") -> json.encodeToString(ProfileDto.serializer(), profile)
            seg.startsWith("users", "me") && method != "GET" -> {
                if (seg.getOrNull(2) == "profile") applyProfileUpdate(body)
                json.encodeToString(ProfileDto.serializer(), profile)
            }
            seg.size == 2 && seg[0] == "users" -> json.encodeToString(ProfileDto.serializer(), profile)
            seg.size == 3 && seg[0] == "users" && seg[2] == "block" -> ok

            // ---- communities ----
            seg == listOf("communities") || seg == listOf("communities", "suggested") -> {
                // Search and the kind chips are server-side against the real API,
                // so the demo filters here too rather than quietly ignoring them.
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

            // ---- talk ----
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

            // ---- pulse ----
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

            // ---- rewards ----
            seg == listOf("rewards", "summary") ->
                json.encodeToString(
                    RewardsSummaryDto.serializer(),
                    RewardsSummaryDto(balance, WAYS_TO_EARN, transactions.take(5)),
                )
            seg == listOf("rewards", "transactions") ->
                json.encodeToString(RewardTransactionsDto.serializer(), RewardTransactionsDto(transactions))

            // ---- referrals ----
            seg == listOf("referrals", "summary") ->
                json.encodeToString(ReferralSummaryDto.serializer(), referrals())
            seg == listOf("referrals", "milestones") ->
                json.encodeToString(kotlinx.serialization.builtins.ListSerializer(MilestoneTierDto.serializer()), MILESTONES)
            seg == listOf("referrals") ->
                json.encodeToString(ReferralListDto.serializer(), ReferralListDto(REFERRAL_HISTORY))
            seg == listOf("referrals", "faq") ->
                json.encodeToString(kotlinx.serialization.builtins.ListSerializer(FaqItemDto.serializer()), FAQ)
            seg.firstOrNull() == "referrals" -> ok

            // ---- notifications ----
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

    // ---- writes ---------------------------------------------------------

    private fun createPost(body: String): PostDto {
        val req = runCatching {
            json.decodeFromString(com.thefloor.app.core.network.CreatePostRequestDto.serializer(), body)
        }.getOrNull()
        val categoryId = req?.categoryId?.ifBlank { null } ?: CATEGORIES.first().id
        val post = PostDto(
            id = id("p"),
            authorId = DemoMode.USER_ID,
            authorName = profile.displayName,
            authorLevel = profile.careerLevel,
            categoryId = categoryId,
            categoryName = CATEGORIES.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            communityId = "za",
            body = req?.body.orEmpty(),
            commentCount = 0,
            reactionCount = 0,
            createdAt = now(),
        )
        posts.add(0, post)
        // +10 per discussion, at most twice a day — the cap is the server's job.
        if (discussionsToday < 2) {
            discussionsToday++
            award(10, "Discussion posted in ${post.categoryName}")
        }
        return post
    }

    private fun createComment(postId: String, body: String): CommentDto {
        val req = runCatching {
            json.decodeFromString(com.thefloor.app.core.network.CreateCommentRequestDto.serializer(), body)
        }.getOrNull()
        val comment = CommentDto(
            id = id("cm"),
            authorId = DemoMode.USER_ID,
            authorName = profile.displayName,
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
            json.decodeFromString(com.thefloor.app.core.network.CreatePulseRequestDto.serializer(), body)
        }.getOrNull()
        val pulse = PulseDto(
            id = id("x"),
            authorId = DemoMode.USER_ID,
            authorName = profile.displayName,
            body = req?.body.orEmpty().take(220),
            likeCount = 0,
            liked = false,
            createdAt = now(),
        )
        pulses.add(0, pulse)
        // Deliberately no award() here. Pulse never touches the ledger.
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
            json.decodeFromString(com.thefloor.app.core.network.UpdateProfileRequestDto.serializer(), body)
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

    private fun completeness(p: ProfileDto): Int {
        val filled = listOf(
            p.country, p.city, p.ageRange, p.employer, p.site, p.industry, p.role, p.careerLevel, p.workMode,
        ).count { !it.isNullOrBlank() } +
            listOf(p.languages, p.skills, p.channels).count { it.isNotEmpty() }
        return (filled * 100 / 12).coerceIn(0, 100)
    }

    // ---- reads ----------------------------------------------------------

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

    // ---- seed data ------------------------------------------------------

    private fun now(): String = Instant.now().toString()

    private fun ago(hours: Long): String = Instant.now().minus(hours, ChronoUnit.HOURS).toString()

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
    ) = PostDto(
        id = id, authorId = "u$id", authorName = author, authorLevel = level,
        categoryId = cat, categoryName = CATEGORIES.first { it.id == cat }.name,
        communityId = "za", body = body, commentCount = comments,
        reactionCount = reactions, saved = false, createdAt = ago(hours),
    )

    private fun seedPosts() = listOf(
        post("p1", "Mika R.", "TEAM_LEADER", "c1", "Should agents be penalised for AHT when the customer genuinely needs more time?", 118, 296, 3),
        post("p2", "Thabo N.", "SENIOR_AGENT", "c2", "When does coaching become micromanagement? Where should a Team Leader draw the line?", 76, 184, 8),
        post("p3", "Priya S.", "SME", "c3", "Are BPO salaries keeping pace with what companies now expect agents to handle?", 129, 341, 14),
        post("p4", "Owen K.", "AGENT", "c4", "AI quality scoring is here. Should an algorithm be allowed to affect an agent bonus?", 143, 267, 22),
        post("p5", "Grace A.", "AGENT", "c3", "What should a real career path from Agent to Team Leader actually look like?", 82, 219, 30),
        post("p6", "Carmen V.", "MANAGER", "c5", "Is the industry ready to give frontline employees a stronger voice in how operations are designed?", 105, 198, 46),
    )

    private fun seedComments(): MutableMap<String, MutableList<CommentDto>> = mutableMapOf(
        "p1" to mutableListOf(
            CommentDto("cm1", "u9", "Sipho D.", null, "We measure AHT but never measure whether the problem actually got solved. That is the real gap.", ago(2)),
            CommentDto("cm2", "u4", "Carmen V.", null, "My TL protects us from the stopwatch and our CSAT is the highest on site. It can be done.", ago(1)),
        ),
        "p2" to mutableListOf(
            CommentDto("cm3", "u5", "Joan D.", null, "Coaching is asking what I need. Micromanagement is telling me what I did wrong after the fact.", ago(4)),
        ),
    )

    private fun seedPulses() = listOf(
        PulseDto("x1", "u2", "Thabo N.", "Third escalation before 9am and the coffee machine is broken. Send help.", 24, false, ago(2)),
        PulseDto("x2", DemoMode.USER_ID, "Naledi M.", "Just closed the longest call of my life. 74 minutes. We got there.", 61, true, ago(5)),
        PulseDto("x3", "u3", "Grace A.", "Night shift crew — what are we listening to tonight?", 12, false, ago(9)),
        PulseDto("x4", "u7", "Owen K.", "Passed my QA review with 96%. Six months ago I was at 71%.", 88, false, ago(20)),
    )

    private fun seedNotifications() = listOf(
        NotificationDto("n1", "TALK", "Mika R. replied to your comment", "\"That is exactly the point I was making.\"", "thefloor://talk/p1", false, ago(4)),
        NotificationDto("n2", "REWARD", "You earned 50 Floor Points", "Verified profile completed.", "thefloor://rewards", false, ago(26)),
        NotificationDto("n3", "FLOOR", "Welcome to the South Africa Floor", "23,461 people are already here.", "thefloor://floor/za", true, ago(50)),
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
