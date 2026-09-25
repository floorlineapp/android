package com.thefloor.app.core.demo

import com.thefloor.app.core.network.CategoryDto
import com.thefloor.app.core.network.CommentDto
import com.thefloor.app.core.network.CommunityDto
import com.thefloor.app.core.network.FaqItemDto
import com.thefloor.app.core.network.MilestoneRewardDto
import com.thefloor.app.core.network.MilestoneTierDto
import com.thefloor.app.core.network.NotificationDto
import com.thefloor.app.core.network.PostDto
import com.thefloor.app.core.network.ProfileDto
import com.thefloor.app.core.network.PulseDto
import com.thefloor.app.core.network.ReferralHistoryItemDto
import com.thefloor.app.core.network.RewardTransactionDto
import com.thefloor.app.core.network.SpotlightSubmissionDto
import com.thefloor.app.core.network.SupportMessageDto
import com.thefloor.app.core.network.WayToEarnDto
import java.time.Instant
import java.time.temporal.ChronoUnit

/** The demo world's starting state. Swap this out and the app is empty; nothing else changes. */

internal fun now(): String = Instant.now().toString()

internal fun ago(hours: Long): String = Instant.now().minus(hours, ChronoUnit.HOURS).toString()

internal fun seedProfile() = ProfileDto(
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

internal fun community(
    id: String, name: String, desc: String, kind: String,
    members: Int, state: String, photo: String,
    ) = CommunityDto(
    id = id, slug = id, name = name, description = desc, kind = kind,
    memberCount = members, isRestricted = false, membershipState = state,
    imageUrl = "https://images.unsplash.com/$photo?w=1200&q=80&auto=format&fit=crop",
    )

internal fun seedCommunities() = listOf(
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

internal fun post(
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

internal fun seedPosts() = listOf(
    post("p1", "Mika R.", "TEAM_LEADER", "c1", "Should agents be penalised for AHT when the customer genuinely needs more time?", 118, 296, 3, "Workplace Ambassador", "ZA"),
    post("p2", "Thabo N.", "SENIOR_AGENT", "c2", "When does coaching become micromanagement? Where should a Team Leader draw the line?", 76, 184, 8, "Floor Voice", "ZA"),
    post("p3", "Priya S.", "SME", "c3", "Are BPO salaries keeping pace with what companies now expect agents to handle?", 129, 341, 14, "Floor Voice", "IN"),
    post("p4", "Owen K.", "AGENT", "c4", "AI quality scoring is here. Should an algorithm be allowed to affect an agent bonus?", 143, 267, 22, "Recognised Member", "PH"),
    post("p5", "Grace A.", "AGENT", "c3", "What should a real career path from Agent to Team Leader actually look like?", 82, 219, 30, "Contributor", "KE"),
    post("p6", "Carmen V.", "MANAGER", "c5", "Is the industry ready to give frontline employees a stronger voice in how operations are designed?", 105, 198, 46, "Workplace Ambassador", "CO"),
    )

internal fun seedComments(): MutableMap<String, MutableList<CommentDto>> = mutableMapOf(
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

internal fun seedPulses() = listOf(
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

internal fun seedNotifications() = listOf(
    NotificationDto("n1", "TALK", "Mika R. replied to your comment", "\"That is exactly the point I was making.\"", "thefloor://talk/p1", false, ago(4)),
    NotificationDto("n2", "REWARD", "You earned 50 Floor Points", "Verified profile completed.", "thefloor://rewards", false, ago(26)),
    NotificationDto("n3", "FLOOR", "Welcome to the South Africa Floor", "23,461 people are already here.", "thefloor://floor/za", true, ago(50)),
    )

internal fun seedSpotlight() = listOf(
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

internal fun seedSupport() = listOf(
    SupportMessageDto(
        id = "m0",
        sender = "ai",
        body = "Hi — I'm Walker. I can answer from what The Floor actually knows: your points, " +
            "your verification, your tier, how a feature works. If I can't, I'll pass it to a " +
            "person with a reference number.",
        createdAt = ago(1),
    ),
    )

internal fun seedTransactions() = listOf(
    RewardTransactionDto("t1", 150, "Friday Trivia winner", ago(48)),
    RewardTransactionDto("t2", -155, "Reward redemption", ago(72)),
    RewardTransactionDto("t3", 75, "Approved Workplace Spotlight", ago(96)),
    RewardTransactionDto("t4", 10, "Discussion posted in The Job", ago(120)),
    RewardTransactionDto("t5", 25, "Floor-verified learning: Soft Skills", ago(144)),
    RewardTransactionDto("t6", 50, "Verified profile completed", ago(200)),
    RewardTransactionDto("t7", 12_325, "Founding member balance carried over", ago(400)),
    )

internal val CATEGORIES = listOf(
        CategoryDto("c1", "the-job", "The Job"),
        CategoryDto("c2", "leadership", "Leadership"),
        CategoryDto("c3", "pay-progression", "Pay & Progression"),
        CategoryDto("c4", "ai-future", "AI & The Future"),
        CategoryDto("c5", "big-questions", "The Big Questions"),
    )

internal val WAYS_TO_EARN = listOf(
        WayToEarnDto("Complete your verified profile — once", 50, "thefloor://profile/edit"),
        WayToEarnDto("Start a Talk discussion — up to 2 a day", 10, "thefloor://talk"),
        WayToEarnDto("Give an answer marked Helpful — capped daily", 25, "thefloor://talk"),
        WayToEarnDto("Floor-verified Academy learning — per item", 25, "thefloor://academy"),
        WayToEarnDto("Attend a verified Floor event — per event", 50, "thefloor://events"),
        WayToEarnDto("Workplace Spotlight approved — per story", 75, "thefloor://insights"),
        WayToEarnDto("Win an official game or competition", 150, "thefloor://rewards"),
    )

internal val MILESTONES = listOf(
        MilestoneTierDto(5, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "CONNECTOR")), "REACHED"),
        MilestoneTierDto(25, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "AMBASSADOR")), "LOCKED"),
        MilestoneTierDto(100, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "SCOUT")), "LOCKED"),
        MilestoneTierDto(500, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "CAPTAIN")), "LOCKED"),
        MilestoneTierDto(1000, listOf(MilestoneRewardDto(kind = "STATUS", statusGrant = "BUILDER")), "LOCKED"),
    )

internal val REFERRAL_HISTORY = listOf(
        ReferralHistoryItemDto("r1", "Qualified", "2026-09-02T09:00:00Z"),
        ReferralHistoryItemDto("r2", "Qualified", "2026-09-05T09:00:00Z"),
        ReferralHistoryItemDto("r3", "Verified", "2026-09-09T09:00:00Z"),
        ReferralHistoryItemDto("r4", "Joined", "2026-09-14T09:00:00Z"),
        ReferralHistoryItemDto("r5", "Held for review", "2026-09-18T09:00:00Z"),
    )

internal val FAQ = listOf(
        FaqItemDto("Does a referral ever add Floor Points?", "No. Invite & Grow is a separate record on purpose — referrals never add points and never buy professional stature."),
        FaqItemDto("What makes a referral 'qualified'?", "The person you invited was active on three separate days. A signup that never comes back does not count."),
        FaqItemDto("My referral says 'held for review'. Is it rejected?", "No — it is a delay, not a rejection. Referrals from shared workplace networks get a human sense-check, and most clear."),
        FaqItemDto("Is the Community Growth Fund paying out?", "Not yet. It is labelled BETA and NOT FUNDED because there is no commercial revenue behind it yet."),
    )
