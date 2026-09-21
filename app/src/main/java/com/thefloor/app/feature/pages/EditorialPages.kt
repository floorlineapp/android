package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thefloor.app.R
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTopBar

/* ------------------------------------------------------------------ *
 *  Shared building blocks for the editorial (content) pages.
 * ------------------------------------------------------------------ */

@Composable
private fun EditorialScaffold(
    title: String,
    onBack: () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = title, onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = FloorTheme.spacing.gutter,
                end = FloorTheme.spacing.gutter,
                top = 12.dp,
                // Clearance for the floating Walker button.
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Horizontal filter chip row with local selection. */
@Composable
private fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options.size) { i ->
            FloorChip(
                text = options[i],
                selected = options[i] == selected,
                onClick = { onSelect(options[i]) },
            )
        }
    }
}

/** Icon + title + description feature card. */
@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    accent: FloorAccent = FloorAccent.AMBER,
    trailing: String? = null,
) {
    FloorCard(contentPadding = 18.dp) {
        Row {
            FloorIconChip(icon, accent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = FloorTheme.typography.titleSm,
                        color = FloorTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (trailing != null) {
                        Spacer(Modifier.width(8.dp))
                        FloorBadge(trailing, tone = BadgeTone.TEAL)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
        }
    }
}

/**
 * Card fronted by real artwork — the partner/course imagery carried over from
 * the web prototype so these pages read the same as the HTML.
 */
@Composable
private fun PhotoCard(
    @androidx.annotation.DrawableRes image: Int,
    title: String,
    desc: String,
    badge: String? = null,
) {
    FloorCard(contentPadding = 0.dp) {
        // Fit, not Crop: these cards carry brand marks along the top edge that a
        // centre-crop slices off. 3:2 matches the source artwork.
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(image),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f),
        )
        Column(Modifier.padding(16.dp)) {
            if (badge != null) {
                FloorEyebrow(badge, accent = FloorAccent.AMBER)
                Spacer(Modifier.height(6.dp))
            }
            Text(title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
        }
    }
}

/** Titled prose card. */
@Composable
private fun ProseCard(title: String, body: String) {
    FloorCard {
        Text(title, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(body, style = FloorTheme.typography.bodyL, color = FloorTheme.colors.textSecondary)
    }
}

/**
 * "Where this can go" panel. Everything inside is explicitly future-tense —
 * these are the roadmap slots the product is built to accept, not features
 * that exist today.
 */
@Composable
private fun RoadmapPanel(title: String, lines: List<String>) {
    FloorInfoNote(accent = FloorAccent.CORAL) {
        FloorEyebrow("Where this can go", accent = FloorAccent.CORAL)
        Spacer(Modifier.height(6.dp))
        Text(title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        lines.forEach { line ->
            Row(Modifier.padding(bottom = 6.dp)) {
                Text("—", style = FloorTheme.typography.body, color = FloorTheme.colors.coral)
                Spacer(Modifier.width(8.dp))
                Text(line, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "Not built yet — listed so you know where The Floor is heading.",
            style = FloorTheme.typography.caption,
            color = FloorTheme.colors.textMuted,
        )
    }
}

/** The standing Beta disclosure for pages that show placeholder brands. */
@Composable
private fun PlaceholderNote(text: String) {
    FloorInfoNote {
        FloorEyebrow("Illustration only", accent = FloorAccent.FAINT)
        Spacer(Modifier.height(6.dp))
        Text(text, style = FloorTheme.typography.body, color = FloorTheme.colors.textMuted)
    }
}

/* ------------------------------------------------------------------ *
 *  Workplace Spotlight — verified showcase, gated at Floor Voice
 * ------------------------------------------------------------------ */

private val spotlightCategories = listOf(
    "Awards & Recognition",
    "People & Culture",
    "Community Impact",
    "Career Growth",
    "Innovation",
    "Workplace Events",
)

private data class SpotlightStory(
    val category: String,
    val company: String,
    val country: String,
    val title: String,
    val proof: String,
)

private val spotlightStories = listOf(
    SpotlightStory("Awards & Recognition", "Meridian Contact Solutions", "South Africa", "Voice team takes national CX award", "Award certificate and published results attached to the submission."),
    SpotlightStory("People & Culture", "Northbank Support Group", "Philippines", "Night shift gets a proper canteen — at 2am", "Photographs, internal announcement and sixteen colleagues co-signed."),
    SpotlightStory("Community Impact", "Arcadia BPO", "Kenya", "Floor raises school fees for forty children", "Receipts and the school's letter of acknowledgement."),
    SpotlightStory("Career Growth", "Lighthouse Customer Care", "Colombia", "Eleven agents promoted to Team Leader this year", "HR confirmation and each member's own verified profile."),
    SpotlightStory("Innovation", "Vantage Voice", "India", "Agents rewrote the QA scorecard themselves", "Before and after scorecards, plus the quality manager's sign-off."),
    SpotlightStory("Workplace Events", "Harbour Line Services", "Poland", "The first all-site handover party", "Event photos and the internal invitation."),
)

@Composable
fun InsightsScreen(onBack: () -> Unit) {
    var category by remember { mutableStateOf(spotlightCategories.first()) }
    // Demo member: 12,480 points and a verified workplace — past the Floor Voice gate.
    val balance = 12_480
    val threshold = 10_000
    val workplaceVerified = true
    val eligible = balance >= threshold && workplaceVerified

    EditorialScaffold("Workplace Spotlight", onBack) {
        item {
            FloorHero(
                eyebrow = "Verified showcase · Gated feature",
                title = "Real workplace wins, from people trusted to tell them.",
                subtitle = "A moderated showcase of awards, milestones, culture and team moments — " +
                    "submitted only by members who have earned enough stature to represent " +
                    "their workplace, and reviewed before anything is published.",
                actions = {
                    FloorPillButton("Submit a Spotlight", onClick = {}, enabled = eligible)
                    FloorPillButton("Rules of engagement", onClick = {}, primary = false)
                },
            )
        }

        // The recognition gate, stated plainly rather than hidden behind a disabled button.
        item {
            FloorInfoNote(accent = if (eligible) FloorAccent.TEAL else FloorAccent.AMBER) {
                FloorEyebrow(
                    if (eligible) "Floor Voice · Eligible to submit" else "Locked · Floor Voice required",
                    accent = if (eligible) FloorAccent.TEAL else FloorAccent.AMBER,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Submission unlocks at Floor Voice: 10,000 Floor Points and a verified workplace.",
                    style = FloorTheme.typography.bodyStrong,
                    color = FloorTheme.colors.textPrimary,
                )
                Spacer(Modifier.height(10.dp))
                FloorProgressBar(progress = (balance.toFloat() / threshold).coerceAtMost(1f))
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "%,d / %,d Floor Points".format(balance, threshold),
                        style = FloorTheme.typography.mono,
                        color = FloorTheme.colors.textSecondary,
                    )
                    Text(
                        if (workplaceVerified) "Workplace verified" else "Workplace not verified",
                        style = FloorTheme.typography.monoTag,
                        color = if (workplaceVerified) FloorTheme.colors.teal else FloorTheme.colors.coral,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "An approved story pays +75 Floor Points back into your ledger. " +
                        "Nothing is ever self-approved — every submission is reviewed first.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }

        item { FloorSectionHeader(title = "Browse the showcase") }
        item { ChipRow(spotlightCategories, category) { category = it } }

        val shown = spotlightStories.filter { it.category == category }
        items(shown.size) { i ->
            val s = shown[i]
            FloorCard(contentPadding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorEyebrow(s.category, accent = FloorAccent.AMBER)
                    Spacer(Modifier.weight(1f))
                    FloorBadge("Approved", tone = BadgeTone.TEAL)
                }
                Spacer(Modifier.height(8.dp))
                Text(s.title, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(5.dp))
                Text(
                    "${s.company} · ${s.country}",
                    style = FloorTheme.typography.monoTag,
                    color = FloorTheme.colors.textMuted,
                )
                Spacer(Modifier.height(10.dp))
                Text("Proof supplied", style = FloorTheme.typography.label, color = FloorTheme.colors.teal)
                Spacer(Modifier.height(3.dp))
                Text(s.proof, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
        }

        item {
            ProseCard(
                "What belongs in Spotlight",
                "Awards, recognition, career growth, learning, culture, community impact, " +
                    "innovation and workplace events. Personal updates belong on Pulse. " +
                    "If you cannot verify it, do not own it, or would not attach your " +
                    "verified workplace profile to it — do not submit it.",
            )
        }
        item {
            ProseCard(
                "What does not",
                "Spotlight is not a review site and not a complaints board. Disputes, " +
                    "grievances and anything confidential go to Walker, not here.",
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Academy — Skills Passport, tracked learning, verification
 * ------------------------------------------------------------------ */

private val academyTabs = listOf("Skills Passport", "My Learning", "Verification")

private val competencyAreas = listOf(
    Triple("Customer conversation", "De-escalation, active listening, tone and recovery.", Icons.Filled.RecordVoiceOver),
    Triple("Quality & compliance", "Scorecards, calibration, data handling and regulated scripts.", Icons.Filled.Checklist),
    Triple("Workforce & operations", "Shrinkage, occupancy, forecasting and the numbers behind the rota.", Icons.Filled.Calculate),
    Triple("Leadership", "Coaching, one-to-ones, and the move from Agent to Team Leader.", Icons.Filled.Groups),
    Triple("Technology & AI", "What automation actually changes about the agent's day.", Icons.Filled.Bolt),
    Triple("Wellbeing & resilience", "Shift recovery, burnout signals and sustainable performance.", Icons.Filled.HealthAndSafety),
    Triple("Money & career", "Pay structures, saving on shift pay, and planning the next role.", Icons.Filled.Savings),
)

private data class Course(val title: String, val desc: String, val area: String, val image: Int, val points: Int)

private val academyCourses = listOf(
    Course("AI for Customer Service", "How AI is reshaping quality, routing and the agent's day.", "Technology & AI", R.drawable.img_ai_for_customer_service, 25),
    Course("Call Center Excellence", "De-escalation, active listening and handling difficult calls.", "Customer conversation", R.drawable.img_call_center_excellence, 25),
    Course("Communication Skills", "The craft behind every good customer conversation.", "Customer conversation", R.drawable.img_communication_skills, 25),
    Course("Data Analysis for Everyone", "Read your own numbers — AHT, CSAT, shrinkage and beyond.", "Workforce & operations", R.drawable.img_data_analysis_for_everyone, 25),
)

@Composable
fun AcademyScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(academyTabs.first()) }

    EditorialScaffold("Academy", onBack) {
        item {
            FloorHero(
                eyebrow = "Structured, tracked learning",
                title = "Upskill for the role you want next.",
                subtitle = "Resources is your working toolbox. Academy is structured development " +
                    "and tracked learning that contributes to your learning record.",
            )
        }

        // Passport header — role, tenure, tier, learning points, items completed.
        item {
            FloorCard(contentPadding = 18.dp) {
                FloorEyebrow("My Academy Passport", accent = FloorAccent.TEAL)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PassportStat("Role", "Senior Agent")
                    PassportStat("Tenure", "3y 2m")
                    PassportStat("Tier", "Floor Voice")
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PassportStat("Learning Points", "340")
                    PassportStat("Completed", "9 items")
                    PassportStat("Verified", "4 items")
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    "Learning Points are a separate ledger from Floor Points — Academy progress " +
                        "is shown here, and only Floor-verified learning pays into your Floor " +
                        "Points balance.",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textMuted,
                )
            }
        }

        item { ChipRow(academyTabs, tab) { tab = it } }

        when (tab) {
            "Skills Passport" -> {
                item {
                    FloorSectionHeader(
                        title = "Seven competency areas",
                        subtitle = "Your passport builds across all of them and travels with you.",
                    )
                }
                items(competencyAreas.size) { i ->
                    val (title, desc, icon) = competencyAreas[i]
                    FeatureCard(
                        icon,
                        title,
                        desc,
                        listOf(FloorAccent.AMBER, FloorAccent.TEAL, FloorAccent.CORAL)[i % 3],
                    )
                }
                item {
                    ProseCard(
                        "Why the passport matters",
                        "Verified learning strengthens your Learn pillar, which feeds your " +
                            "Workplace Spotlight stature — and it is the credential Employers " +
                            "& Jobs is designed to eventually read from.",
                    )
                }
            }
            "My Learning" -> {
                item {
                    FloorSectionHeader(
                        title = "Free learning library",
                        subtitle = "Curated external courses. The Floor credits sources rather than reproducing them.",
                    )
                }
                items(academyCourses.size) { i ->
                    val c = academyCourses[i]
                    PhotoCard(c.image, c.title, c.desc, "${c.area} · +${c.points} pts when verified")
                }
                item {
                    FeatureCard(
                        Icons.Filled.TrendingUp,
                        "Path to Team Leader",
                        "The skills, habits and metrics that move you from Agent to leadership.",
                        FloorAccent.CORAL,
                        trailing = "In progress",
                    )
                }
            }
            else -> {
                item {
                    FloorSectionHeader(
                        title = "Three levels of verification",
                        subtitle = "How much weight a completed item carries depends on who confirmed it.",
                    )
                }
                item {
                    FeatureCard(
                        Icons.Filled.School,
                        "Self-completed",
                        "You mark the item done. It adds Learning Points and shows on your passport, " +
                            "but pays no Floor Points.",
                        FloorAccent.FAINT,
                        trailing = "Level 1",
                    )
                }
                item {
                    FeatureCard(
                        Icons.Filled.VerifiedUser,
                        "Floor Verified",
                        "You write a short reflection on what you actually took from it. Once accepted " +
                            "it pays +25 Floor Points into your ledger, once per item.",
                        FloorAccent.TEAL,
                        trailing = "+25 pts",
                    )
                }
                item {
                    FeatureCard(
                        Icons.Filled.WorkspacePremium,
                        "Partner Verified",
                        "Confirmed by a real training provider. No institutions are integrated yet — " +
                            "this level exists so that adding one later changes the data, not the app.",
                        FloorAccent.CORAL,
                        trailing = "Future",
                    )
                }
                item {
                    RoadmapPanel(
                        "Partner-verified learning",
                        listOf(
                            "Named training providers confirming completion directly.",
                            "A passport an employer can check rather than take on trust.",
                            "Course credit redeemable with Floor Points.",
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun PassportStat(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = FloorTheme.typography.eyebrow, color = FloorTheme.colors.textMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
    }
}

/* ------------------------------------------------------------------ *
 *  Events
 * ------------------------------------------------------------------ */

private data class EventItem(
    val kicker: String,
    val title: String,
    val desc: String,
    val floorOwned: Boolean,
    val when_: String,
)

private val events = listOf(
    EventItem("Floor Radio · Night Shift", "Lo-Fi Beats — for the ones awake when everyone else is asleep", "A rolling music session for the graveyard shift across every timezone.", true, "Tonight · 22:00 local"),
    EventItem("Floor Radio · Flagship", "The Global Handover: Johannesburg clocks out, Manila takes The Floor", "One network connecting BPO communities without forcing every country into the same clock.", true, "Thursday · 16:00 SAST"),
    EventItem("Industry webinar", "AI quality scoring — what agents need to know", "External listing. The Floor summarises and credits the organiser; registration happens on their site.", false, "24 Sep · 14:00 BST"),
    EventItem("Industry conference", "Contact centre operations summit", "External listing from the industry calendar.", false, "2 Oct · 09:00 CET"),
    EventItem("Community meetup", "Team Leaders roundtable", "Coaching, micromanagement and where to draw the line — a live discussion room.", true, "Saturday · 11:00 PHT"),
)

@Composable
fun EventsScreen(onBack: () -> Unit) {
    EditorialScaffold("Events", onBack) {
        item {
            FloorHero(
                eyebrow = "Industry + owned programming",
                title = "Where The Floor meets the industry.",
                subtitle = "External webinars and conferences sit alongside The Floor's own radio " +
                    "sessions and meetups, in one calendar.",
                actions = {
                    FloorPillButton("Unlock Radio Pass", onClick = {}, leadingIcon = Icons.Filled.Headphones)
                },
            )
        }
        item {
            FloorInfoNote(accent = FloorAccent.TEAL) {
                Text(
                    "Verified attendance at a Floor event pays +50 Floor Points — once per event, " +
                        "checked in at the session rather than claimed afterwards.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { FloorSectionHeader(title = "Upcoming events") }
        items(events.size) { i ->
            val e = events[i]
            FloorCard(contentPadding = 18.dp) {
                Row {
                    FloorIconChip(
                        if (e.floorOwned) Icons.Filled.Headphones else Icons.Filled.CalendarMonth,
                        if (e.floorOwned) FloorAccent.AMBER else FloorAccent.TEAL,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FloorEyebrow(
                                e.kicker,
                                accent = if (e.floorOwned) FloorAccent.AMBER else FloorAccent.TEAL,
                                modifier = Modifier.weight(1f),
                            )
                            FloorBadge(
                                if (e.floorOwned) "+50 PTS" else "EXTERNAL",
                                tone = if (e.floorOwned) BadgeTone.TEAL else BadgeTone.NEUTRAL,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(e.title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(e.desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text(e.when_, style = FloorTheme.typography.monoTag, color = FloorTheme.colors.textMuted)
                    }
                }
            }
        }
        item {
            PlaceholderNote(
                "External listings link out to the organiser's own registration page. " +
                    "The Floor does not sell tickets and is not the organiser.",
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Marketplace — 10 categories, Lifestyle splits into 9 more
 * ------------------------------------------------------------------ */

private val marketplaceCategories = listOf(
    "Electronics", "Food", "Travel", "Clothing", "Education",
    "Wellness", "Insurance", "Financial", "Entertainment", "Lifestyle",
)

private val lifestyleSubcategories = listOf(
    "Groceries", "Transportation", "Travel", "Fitness", "Beauty & Personal Care",
    "Home & Living", "Entertainment", "Dining", "USA & UK Specials",
)

private data class Deal(val brand: String, val title: String, val desc: String, val image: Int?)

private val dealsByCategory: Map<String, List<Deal>> = mapOf(
    "Electronics" to listOf(
        Deal("Illustration partner", "Noise-cancelling headsets", "The headset is the tool. Member pricing on the ones that survive a full shift.", null),
        Deal("Illustration partner", "Home-office bundles", "Webcam, ring light and a chair that does not end your back.", null),
    ),
    "Food" to listOf(
        Deal("Illustration partner", "Shift-hour delivery", "Discounts that apply at 3am, not only at lunchtime.", null),
        Deal("Illustration partner", "Canteen top-ups", "Prepaid meal credit for on-site teams.", null),
    ),
    "Travel" to listOf(
        Deal("Illustration partner", "Flight Deals", "Get home for the holidays for less — member fares across regions.", R.drawable.img_flight_deals),
        Deal("Illustration partner", "Hotel Stays", "Rest days done properly, at member rates.", R.drawable.img_hotel_stays),
        Deal("Illustration partner", "Fly Anywhere", "Compare and book the whole trip in one place.", R.drawable.img_fly_anywhere),
    ),
    "Clothing" to listOf(
        Deal("Illustration partner", "Workwear that lasts", "Smart-casual basics for on-site floors.", null),
    ),
    "Education" to listOf(
        Deal("Illustration partner", "Paid course credit", "Discounted seats on paid professional courses.", null),
    ),
    "Wellness" to listOf(
        Deal("Illustration partner", "Calm Premium", "Sleep, focus and wind-down tools built for rotating shifts.", R.drawable.img_calm_premium),
        Deal("Illustration partner", "BetterUp Coaching", "One-to-one coaching for stress, confidence and career direction.", R.drawable.img_betterup_coaching),
    ),
    "Insurance" to listOf(
        Deal("Illustration partner", "Health Insurance", "Cover that works around shift patterns.", R.drawable.img_health_insurance),
        Deal("Illustration partner", "Life Cover", "Straightforward protection for the people who depend on you.", R.drawable.img_life_cover),
        Deal("Illustration partner", "Funeral Cover", "Cover that families in several Floor regions ask for first.", R.drawable.img_funeral_cover),
    ),
    "Financial" to listOf(
        Deal("Illustration partner", "Invest & Save", "Put shift pay to work without gambling it.", R.drawable.img_invest_save),
        Deal("Illustration partner", "Budget & Save Like a Pro", "Make shift pay stretch — plan, save, invest, grow.", R.drawable.img_budget_save_like_a_pro),
    ),
    "Entertainment" to listOf(
        Deal("Illustration partner", "Experiences", "Things worth doing on your days off.", R.drawable.img_experiences),
    ),
)

@Composable
fun MarketplaceScreen(onBack: () -> Unit) {
    var category by remember { mutableStateOf(marketplaceCategories.first()) }
    var sub by remember { mutableStateOf(lifestyleSubcategories.first()) }

    EditorialScaffold("Marketplace", onBack) {
        item {
            FloorHero(
                eyebrow = "Member commerce · 10 categories",
                title = "Marketplace",
                subtitle = "Member deals around the BPO lifestyle — the gear, cover, travel and " +
                    "services people on the floor actually use.",
            )
        }
        item {
            PlaceholderNote(
                "Every brand shown here is a prospective affiliate partner The Floor intends to " +
                    "onboard — not a confirmed live partnership, sponsor or endorser.",
            )
        }
        item { ChipRow(marketplaceCategories, category) { category = it } }

        if (category == "Lifestyle") {
            item {
                FloorSectionHeader(
                    title = "Lifestyle",
                    subtitle = "The broadest category — it splits into nine of its own.",
                )
            }
            item { PhotoCard(R.drawable.img_lifestyle_hero, "Lifestyle deals", "Everything that makes the days between shifts better.", "Featured") }
            item { ChipRow(lifestyleSubcategories, sub) { sub = it } }
            item {
                FeatureCard(
                    Icons.Filled.Star,
                    sub,
                    "Member offers across $sub. Prospective partners only during Beta — " +
                        "nothing here is a confirmed commercial relationship yet.",
                    FloorAccent.AMBER,
                )
            }
        } else {
            val deals = dealsByCategory[category].orEmpty()
            item { FloorSectionHeader(title = category, subtitle = "${deals.size} offers in this category") }
            items(deals.size) { i ->
                val d = deals[i]
                if (d.image != null) {
                    PhotoCard(d.image, d.title, d.desc, d.brand)
                } else {
                    FeatureCard(Icons.Filled.Star, d.title, d.desc, FloorAccent.AMBER, trailing = "Deal")
                }
            }
        }

        item {
            RoadmapPanel(
                "From placeholder card to real deal",
                listOf(
                    "Confirmed affiliate partners replacing prospective ones, marked as such.",
                    "Commission tracked back to the member who introduced the partner.",
                    "Revenue routed into the Community Growth Fund.",
                ),
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Resources — free instant toolbox, deliberately untracked
 * ------------------------------------------------------------------ */

@Composable
fun ResourcesScreen(onBack: () -> Unit) {
    EditorialScaffold("Resources", onBack) {
        item {
            FloorHero(
                eyebrow = "Free instant toolbox",
                title = "The tools people actually need on shift.",
                subtitle = "Calculators, templates, QA tools, WFM references and curated BPO guides — " +
                    "instant and ungated.",
            )
        }
        item {
            FloorInfoNote(accent = FloorAccent.TEAL) {
                Text(
                    "Resources and Academy do different jobs.",
                    style = FloorTheme.typography.titleSm,
                    color = FloorTheme.colors.textPrimary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Nothing here is tracked, pointed or added to your learning record. Grab it, " +
                        "use it, close it. Academy is where structured, tracked development lives.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { FloorSectionHeader(title = "Browse by what you need") }
        item { FeatureCard(Icons.Filled.Calculate, "Calculators", "Shrinkage, occupancy, AHT and SLA calculators for the floor.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.Checklist, "Templates & checklists", "Coaching forms, QA scorecards and shift handover templates.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.AutoStories, "Guides & references", "Plain-language WFM, quality and operations references.", FloorAccent.CORAL) }
        item { FeatureCard(Icons.Filled.Psychology, "Scripts & phrasing", "De-escalation lines, hold phrasing and handover wording that works.", FloorAccent.AMBER) }

        item {
            FloorSectionHeader(
                title = "Featured toolkit directory",
                subtitle = "Curated from an external source library — credited, never reproduced.",
            )
        }
        item { PhotoCard(R.drawable.img_open_your_first_bank_account, "Open Your First Bank Account", "A starter guide for new agents getting paid properly.", "New agent starter") }
        item { PhotoCard(R.drawable.img_budget_save_like_a_pro, "Budget & Save Like a Pro", "Make shift pay stretch — plan, save, invest, grow.", "Save smarter") }
        item { PhotoCard(R.drawable.img_money_tips_that_matter, "Money Tips That Matter", "Small habits that change what payday feels like.", "Financial tips") }
        item { PhotoCard(R.drawable.img_start_trading_the_right_way, "Start Trading the Right Way", "Understand the risk before you put money in.", "Learn to trade") }

        item {
            RoadmapPanel(
                "Three desks, not one shelf",
                listOf(
                    "An agent desk — everything needed on the phones today.",
                    "A team leader desk — coaching, one-to-ones and calibration.",
                    "An operations desk — forecasting, WFM and reporting.",
                ),
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Employers & Jobs — illustration now, partnership later
 * ------------------------------------------------------------------ */

private data class Employer(val name: String, val market: String, val focus: String)

/**
 * Eleven illustration profiles. These are deliberately invented names, not real
 * companies: the process guide flags using real, named employers as UI
 * placeholders as a legal question to settle before public launch, so the app
 * ships with names that cannot be mistaken for a partnership claim.
 */
private val employers = listOf(
    Employer("Meridian Contact Solutions", "South Africa · Johannesburg", "Voice · financial services"),
    Employer("Northbank Support Group", "Philippines · Manila", "Omnichannel · retail"),
    Employer("Lighthouse Customer Care", "Colombia · Bogotá", "Bilingual voice · travel"),
    Employer("Vantage Voice", "India · Bengaluru", "Technical support"),
    Employer("Arcadia BPO", "Kenya · Nairobi", "Back office · fintech"),
    Employer("Harbour Line Services", "Poland · Kraków", "Multilingual · logistics"),
    Employer("Summit Care Partners", "Mexico · Guadalajara", "Nearshore voice"),
    Employer("Delta Bridge Outsourcing", "Egypt · Cairo", "Multilingual · telecom"),
    Employer("Copperfield Contact", "United Kingdom · Manchester", "Regulated · insurance"),
    Employer("Riverstone Global", "United States · remote", "Remote-first · SaaS support"),
    Employer("Atlas Shift Group", "Brazil · São Paulo", "Voice · e-commerce"),
)

private data class Job(val title: String, val company: String, val location: String, val mode: String)

private val jobs = listOf(
    Job("Senior Agent · Voice", "Meridian Contact Solutions", "Johannesburg", "Hybrid"),
    Job("Team Leader · Customer Care", "Northbank Support Group", "Manila", "On-site"),
    Job("QA Analyst", "Lighthouse Customer Care", "Bogotá", "Remote"),
    Job("WFM Analyst", "Vantage Voice", "Bengaluru", "Hybrid"),
    Job("Customer Support Specialist", "Riverstone Global", "Remote", "Remote"),
)

@Composable
fun JobsScreen(onBack: () -> Unit) {
    EditorialScaffold("Employers & Jobs", onBack) {
        item {
            FloorHero(
                eyebrow = "Illustration now, partnership later",
                title = "See who is hiring. Explore where your BPO career could go next.",
                subtitle = "A future bridge between verified Floor profiles and BPO employers.",
                actions = {
                    FloorPillButton("Use my verified profile", onClick = {})
                    FloorPillButton("Build skills in Academy", onClick = {}, primary = false)
                },
            )
        }
        item {
            PlaceholderNote(
                "The companies below are illustration placeholders only — they are not yet " +
                    "partners, sponsors or endorsers of The Floor, and no listing here implies " +
                    "any relationship with a real employer.",
            )
        }

        item { FloorSectionHeader(title = "Employer directory", subtitle = "${employers.size} illustration profiles") }
        items(employers.size) { i ->
            val e = employers[i]
            FloorCard(contentPadding = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorIconChip(Icons.Filled.Diversity3, FloorAccent.TEAL)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(e.name, style = FloorTheme.typography.bodyStrong, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text(e.market, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
                        Spacer(Modifier.height(2.dp))
                        Text(e.focus, style = FloorTheme.typography.monoTag, color = FloorTheme.colors.textMuted)
                    }
                    Spacer(Modifier.width(8.dp))
                    FloorBadge("PLACEHOLDER", tone = BadgeTone.NEUTRAL)
                }
            }
        }

        item {
            FloorSectionHeader(
                title = "Opportunity board",
                subtitle = "Every role links out to the employer's own careers portal.",
            )
        }
        items(jobs.size) { i ->
            val j = jobs[i]
            FeatureCard(
                Icons.Filled.Work,
                j.title,
                "${j.company} · ${j.location} · ${j.mode}",
                listOf(FloorAccent.AMBER, FloorAccent.TEAL, FloorAccent.CORAL)[i % 3],
                trailing = "External",
            )
        }

        item {
            RoadmapPanel(
                "A profile worth applying with",
                listOf(
                    "Roles matched against your verified profile instead of a blank CV.",
                    "Verified recruiter badges, so you know who you are talking to.",
                    "Sharing your Floor profile and Academy passport with an employer in one tap.",
                ),
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  About The Floor
 * ------------------------------------------------------------------ */

private val brandFamily = listOf(
    "The Floor" to "The community itself — find your people, wherever you work.",
    "The Floor Radio" to "Six regional live feeds and the shows that run across them.",
    "The Floor Rewards" to "The points ledger: earn, see why, redeem.",
    "The Floor Ambassadors" to "The people who bring the most qualified members in.",
    "The Floor Academy" to "Tracked learning and a portable Skills Passport.",
    "The Floor Marketplace" to "Member deals across the BPO lifestyle.",
    "The Floor Index" to "What the work actually pays and looks like, by market.",
    "The Floor Jobs" to "Where a verified profile becomes an opportunity.",
)

@Composable
fun AboutScreen(onBack: () -> Unit, onOpenWellbeing: () -> Unit = {}) {
    EditorialScaffold("About The Floor", onBack) {
        item {
            FloorHero(
                eyebrow = "Vision & positioning",
                title = "The global home of the people behind every customer conversation.",
                subtitle = "Wherever you work. Whatever shift you're on. You're never alone.",
            )
        }
        item {
            ProseCard(
                "The problem",
                "The global contact-centre workforce is enormous, but fragmented. Millions of " +
                    "people are the voice of every brand, yet there is no independent, " +
                    "worker-centred ecosystem spanning companies, countries, industries and " +
                    "career levels. The Floor is that ecosystem.",
            )
        }
        item {
            ProseCard(
                "What The Floor is",
                "A global community and ecosystem for BPO and contact-centre workers: a place to " +
                    "find your people, talk about the work honestly, learn and grow, earn " +
                    "recognition, and be seen as the professionals they are.",
            )
        }
        item {
            ProseCard(
                "What The Floor is not",
                "It is not an employer review site, not a complaints board, and not a place to " +
                    "leak confidential information. Spotlight is for positive, attributable " +
                    "workplace showcase — disputes use the appropriate support route.",
            )
        }
        item {
            FloorSectionHeader(
                title = "The Floor ecosystem",
                subtitle = "One connected family, not fifteen unrelated screens.",
            )
        }
        items(brandFamily.size) { i ->
            val (name, desc) = brandFamily[i]
            FeatureCard(
                Icons.Filled.Star,
                name,
                desc,
                listOf(FloorAccent.AMBER, FloorAccent.TEAL, FloorAccent.CORAL)[i % 3],
            )
        }
        item {
            FloorCard(onClick = onOpenWellbeing, contentPadding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorIconChip(Icons.Filled.SupportAgent, FloorAccent.TEAL)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Walker", style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Support across The Floor — there is no Walker page, only the button that follows you.",
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Wellbeing — the one hidden page, and it exists to explain Walker
 * ------------------------------------------------------------------ */

@Composable
fun WellbeingScreen(onBack: () -> Unit) {
    EditorialScaffold("Walker & wellbeing", onBack) {
        item {
            FloorHero(
                eyebrow = "Global support layer",
                title = "There is no separate Walker page.",
                subtitle = "Use the floating Walker button from anywhere on The Floor. It is on " +
                    "every screen, and it carries the conversation with you between them.",
            )
        }
        item {
            FloorInfoNote(accent = FloorAccent.AMBER) {
                FloorEyebrow("How Walker works today", accent = FloorAccent.AMBER)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Walker answers automatically first. If it can't resolve something — account, " +
                        "payment, verification, or anything broken — asking for a person hands the " +
                        "whole conversation to the Walker team, and the reply arrives in the same " +
                        "thread. Support runs across timezones, so an answer may not be instant.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { FloorSectionHeader(title = "What Walker is for") }
        item { FeatureCard(Icons.Filled.SupportAgent, "Anything not working", "Login, verification, a missing point, a Floor that won't load.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.Lightbulb, "Suggest a Floor", "Ask for a community that doesn't exist yet — it comes straight to us.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.HealthAndSafety, "A harder conversation", "Stress, burnout and the weight of the work. Walker can point you to the right support.", FloorAccent.CORAL) }

        item { FloorSectionHeader(title = "Looking after yourself", subtitle = "Partner wellbeing benefits, available through Marketplace.") }
        item { PhotoCard(R.drawable.img_calm_premium, "Calm Premium", "Sleep, focus and wind-down tools built for people on rotating shifts.", "Wellbeing") }
        item { PhotoCard(R.drawable.img_betterup_coaching, "BetterUp Coaching", "One-to-one coaching for stress, confidence and career direction.", "Wellbeing") }
        item {
            FloorCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "If you are really struggling",
                        style = FloorTheme.typography.titleSm,
                        color = FloorTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    FloorBadge("Sensitive", tone = BadgeTone.CORAL)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "The Floor is a community, not a crisis service. If things feel heavier than " +
                        "a bad shift, please reach out to someone you trust or a local support " +
                        "line. Walker can help you find the right resource for your country.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { Box(Modifier.height(4.dp)) }
    }
}
