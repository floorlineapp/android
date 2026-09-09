package com.thefloor.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.thefloor.app.core.datastore.ThemeMode
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorAvatar
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorKpiCard
import com.thefloor.app.core.designsystem.components.FloorLiveRoomCard
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTile
import com.thefloor.app.core.designsystem.components.FloorVerifiedBadge
import com.thefloor.app.core.designsystem.components.FloorWordmark
import com.thefloor.app.core.model.CareerLevel
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.model.CompletionCard
import com.thefloor.app.core.model.HomeContent
import com.thefloor.app.core.model.MembershipState
import com.thefloor.app.core.model.Post
import com.thefloor.app.core.model.Pulse
import com.thefloor.app.core.model.RewardTransaction
import com.thefloor.app.core.model.RewardsSummary
import com.thefloor.app.core.model.TalkCategory
import com.thefloor.app.core.model.UserProfile
import com.thefloor.app.core.model.WayToEarn
import com.thefloor.app.core.model.WorkMode
import com.thefloor.app.feature.auth.WelcomeScreen
import com.thefloor.app.feature.floor.DiscoverBody
import com.thefloor.app.feature.floor.DiscoverUiState
import com.thefloor.app.feature.home.HomeContentList
import com.thefloor.app.feature.profile.ProfileBody
import com.thefloor.app.feature.pulse.PulseBody
import com.thefloor.app.feature.pulse.PulseUiState
import com.thefloor.app.feature.radio.RadioBody
import com.thefloor.app.feature.radio.RadioPlaybackState
import com.thefloor.app.feature.rewards.RewardsBody
import com.thefloor.app.feature.talk.TalkBody
import com.thefloor.app.feature.talk.TalkFeedUiState
import com.thefloor.app.feature.pages.AboutScreen
import com.thefloor.app.feature.pages.AcademyScreen
import com.thefloor.app.feature.pages.EventsScreen
import com.thefloor.app.feature.pages.InsightsScreen
import com.thefloor.app.feature.pages.JobsScreen
import com.thefloor.app.feature.pages.MarketplaceScreen
import com.thefloor.app.feature.pages.ResourcesScreen
import com.thefloor.app.feature.pages.WellbeingScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders each screen to a PNG on the JVM so the UI can actually be reviewed.
 * The viewport is deliberately tall so most of each scrolling page lands in one
 * frame. Snapshots are written to /screenshots and published by CI.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h1900dp-xhdpi")
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun snap(name: String, dark: Boolean = true, content: @Composable () -> Unit) {
        compose.setContent {
            FloorTheme(mode = if (dark) ThemeMode.DARK else ThemeMode.LIGHT) {
                Surface(color = FloorTheme.colors.ink, modifier = Modifier.fillMaxSize()) { content() }
            }
        }
        compose.onRoot().captureRoboImage("$name.png")
    }

    // ---------- editorial pages ----------
    @Test fun insights() = snap("page_insights") { InsightsScreen {} }
    @Test fun academy() = snap("page_academy") { AcademyScreen {} }
    @Test fun events() = snap("page_events") { EventsScreen {} }
    @Test fun marketplace() = snap("page_marketplace") { MarketplaceScreen {} }
    @Test fun resources() = snap("page_resources") { ResourcesScreen {} }
    @Test fun jobs() = snap("page_jobs") { JobsScreen {} }
    @Test fun about() = snap("page_about") { AboutScreen {} }
    @Test fun wellbeing() = snap("page_wellbeing") { WellbeingScreen {} }

    // ---------- brand / auth ----------
    @Test fun welcome() = snap("page_welcome") { WelcomeScreen({}, {}) }
    @Test fun welcomeLight() = snap("page_welcome_light", dark = false) { WelcomeScreen({}, {}) }

    // ---------- design-system gallery, both themes ----------
    @Test fun galleryDark() = snap("gallery_dark") { Gallery() }
    @Test fun galleryLight() = snap("gallery_light", dark = false) { Gallery() }

    @Test fun academyLight() = snap("page_academy_light", dark = false) { AcademyScreen {} }
    @Test fun aboutLight() = snap("page_about_light", dark = false) { AboutScreen {} }

    // ---------- Home, rendered from the real screen body ----------
    @Test fun home() = snap("page_home") { HomeBody() }
    @Test fun homeLight() = snap("page_home_light", dark = false) { HomeBody() }

    // ---------- the rest of the real screens ----------
    @Test fun talk() = snap("page_talk") { TalkBody(state = fakeTalk) }
    @Test fun talkLight() = snap("page_talk_light", dark = false) { TalkBody(state = fakeTalk) }
    @Test fun floor() = snap("page_floor") { DiscoverBody(state = fakeDiscover, query = "", selectedKind = null) }
    @Test fun profile() = snap("page_profile") { ProfileBody(profile = fakeProfile) }
    @Test fun profileLight() = snap("page_profile_light", dark = false) { ProfileBody(profile = fakeProfile) }
    @Test fun pulse() = snap("page_pulse") { PulseBody(state = fakePulse) }
    @Test fun rewards() = snap("page_rewards") { RewardsBody(summary = fakeRewards, transactions = fakeTx) }
    @Test fun rewardsLight() = snap("page_rewards_light", dark = false) { RewardsBody(summary = fakeRewards, transactions = fakeTx) }
    @Test fun radio() = snap("page_radio") { RadioBody(playback = RadioPlaybackState.OnAir(playing = true, programName = "Night Shift")) }
}

/** Real Home content, fed representative data. */
@Composable
private fun HomeBody() {
    HomeContentList(
        content = fakeHome,
        greeting = "Good afternoon shift, Naledi.",
        onOpenInvite = {}, onOpenRewards = {}, onOpenFloorTab = {}, onOpenTalk = {},
        onOpenRadio = {}, onOpenAcademy = {}, onOpenMarketplace = {},
        onOpenCommunity = {}, onOpenPost = {}, onOpenProfileEdit = {},
    )
}

private val fakeHome = HomeContent(
    displayName = "Naledi M.",
    presenceCount = 2347,
    completionCards = listOf(
        CompletionCard(
            title = "Complete your profile",
            subtitle = "Add your workplace to see who else works there.",
            deepLink = "floor://profile/edit",
        ),
    ),
    myFloors = listOf(
        Community(
            id = "za", slug = "south-africa", name = "South Africa Floor",
            description = "From Cape Town to Joburg, Durban to everywhere in between.",
            kind = "COUNTRY", memberCount = 23461, isRestricted = false,
            membershipState = MembershipState.JOINED,
        ),
    ),
    trendingPosts = listOf(
        Post(
            id = "p1", authorId = "u1", authorName = "Mika R.", authorLevel = CareerLevel.TEAM_LEADER,
            categoryId = "c1", categoryName = "The Job", communityId = "za",
            body = "Should agents be penalised for AHT when the customer genuinely needs more time?",
            commentCount = 118, reactionCount = 296, myReaction = null, saved = false,
            createdAt = "2026-09-08T10:00:00Z",
        ),
        Post(
            id = "p2", authorId = "u2", authorName = "Thabo N.", authorLevel = CareerLevel.SENIOR_AGENT,
            categoryId = "c2", categoryName = "Leadership", communityId = "za",
            body = "When does coaching become micromanagement? Where should a Team Leader draw the line?",
            commentCount = 76, reactionCount = 184, myReaction = null, saved = false,
            createdAt = "2026-09-07T10:00:00Z",
        ),
    ),
    invitedCount = 12,
    activeReferrals = 5,
    creditsBalance = 12480,
    flags = emptyMap(),
)

@Composable
private fun Gallery() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FloorWordmark()
        FloorHero(
            eyebrow = "The Floor · Global Community",
            title = "Good afternoon shift, Naledi.",
            subtitle = "Wherever you work, whatever shift you're on — you're never alone on The Floor.",
            actions = {
                FloorPillButton("Find your Floor", onClick = {}, leadingIcon = Icons.Filled.Groups)
                FloorPillButton("Floor Radio", onClick = {}, primary = false, leadingIcon = Icons.Filled.Headphones)
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloorKpiCard(Icons.Filled.Groups, "2,347", "people online now", Modifier.weight(1f), FloorAccent.TEAL)
            FloorKpiCard(Icons.Filled.Headphones, "12,480", "Floor points", Modifier.weight(1f), FloorAccent.AMBER)
        }
        FloorSectionHeader(title = "Jump back in", subtitle = "Your shift. Your Floor.", linkText = "See all", onLink = {})
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FloorTile(Icons.Filled.Groups, "The Floor", "Your community by country and shift.", {}, Modifier.weight(1f), FloorAccent.AMBER)
            FloorTile(Icons.Filled.Headphones, "Floor Radio", "When words stop, the shift keeps moving.", {}, Modifier.weight(1f), FloorAccent.CORAL)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FloorAvatar("Naledi M.", ring = FloorAccent.TEAL)
            FloorAvatar("Thabo N.", ring = FloorAccent.AMBER)
            FloorVerifiedBadge()
        }
        FloorLiveRoomCard(
            icon = Icons.Filled.Headphones,
            topic = "Music on shift",
            desc = "What's in your headset right now? Playlists, new finds, and Floor Radio requests.",
            onlineText = "67 online now",
            onClick = {},
        )
    }
}

// ---------------------------------------------------------------- fixtures

private val fakeTalk = TalkFeedUiState(
    loading = false,
    categories = listOf(
        TalkCategory("c1", "the-job", "The Job"),
        TalkCategory("c2", "leadership", "Leadership"),
        TalkCategory("c3", "pay", "Pay & Progression"),
        TalkCategory("c4", "ai", "AI & The Future"),
    ),
    posts = listOf(
        Post("p1", "u1", "Mika R.", CareerLevel.TEAM_LEADER, "c1", "The Job", "za",
            "Should agents be penalised for AHT when the customer genuinely needs more time?",
            118, 296, null, false, "2026-09-08T10:00:00Z"),
        Post("p2", "u2", "Priya S.", CareerLevel.SME, "c3", "Pay & Progression", "ph",
            "Are BPO salaries keeping pace with what companies now expect agents to handle?",
            129, 341, null, false, "2026-09-07T10:00:00Z"),
        Post("p3", "u3", "Owen K.", CareerLevel.AGENT, "c4", "AI & The Future", "za",
            "AI quality scoring is here. Should an algorithm be allowed to affect an agent bonus?",
            143, 267, null, false, "2026-09-06T10:00:00Z"),
    ),
)

private val fakeDiscover = DiscoverUiState.Ready(
    listOf(
        Community("za", "south-africa", "South Africa Floor",
            "From Cape Town to Joburg, Durban to everywhere in between.",
            "COUNTRY", 23461, false, MembershipState.JOINED),
        Community("ph", "philippines", "Philippines Floor",
            "The heart of global outsourcing. Always on, always awake.",
            "COUNTRY", 41208, false, MembershipState.NOT_JOINED),
        Community("co", "colombia", "Colombia Floor",
            "Passion. Resilience. World-class customer experience.",
            "COUNTRY", 17894, false, MembershipState.NOT_JOINED),
    ),
)

private val fakeProfile = UserProfile(
    userId = "u1",
    displayName = "Naledi M.",
    photoUrl = null,
    country = "South Africa",
    city = "Johannesburg",
    languages = listOf("English", "Zulu"),
    employer = "Meridian Contact Solutions",
    site = "Rosebank Contact Center",
    industry = "Telecom",
    role = "Senior Agent",
    careerLevel = CareerLevel.SENIOR_AGENT,
    experienceYears = 4,
    workMode = WorkMode.HYBRID,
    skills = listOf("De-escalation", "Billing systems", "CRM", "Coaching new hires"),
    completeness = 50,
    emailVerified = true,
    visibility = emptyMap(),
)

private val fakePulse = PulseUiState(
    loading = false,
    myUserId = "u1",
    pulses = listOf(
        Pulse("x1", "u2", "Thabo N.", "Third escalation before 9am and the coffee machine is broken. Send help.", 24, false, "2026-09-09T07:00:00Z"),
        Pulse("x1b", "u1", "Naledi M.", "Just closed the longest call of my life. 74 minutes. We got there.", 61, true, "2026-09-09T06:10:00Z"),
        Pulse("x2", "u3", "Grace A.", "Night shift crew — what are we listening to tonight?", 12, false, "2026-09-09T05:30:00Z"),
    ),
)

private val fakeRewards = RewardsSummary(
    creditsBalance = 12480,
    waysToEarn = listOf(
        WayToEarn("Complete your verified profile", 50, "floor://profile/edit"),
        WayToEarn("Start a meaningful Talk discussion", 10, "floor://talk"),
        WayToEarn("Attend a verified Floor event", 50, "floor://events"),
    ),
    recentTransactions = emptyList(),
)

private val fakeTx = listOf(
    RewardTransaction("t1", 150, "Friday Trivia winner", "2026-09-05T10:00:00Z"),
    RewardTransaction("t2", -155, "Historic reward redemption", "2026-09-04T10:00:00Z"),
    RewardTransaction("t3", 75, "Approved Workplace Spotlight", "2026-09-03T10:00:00Z"),
    RewardTransaction("t4", 10, "Meaningful discussion posted", "2026-09-02T10:00:00Z"),
    RewardTransaction("t5", 25, "Floor-verified learning: Soft Skills", "2026-09-01T10:00:00Z"),
)
