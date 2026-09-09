package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorPillButton
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
            contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Icon + title + description feature card. */
@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    accent: FloorAccent = FloorAccent.AMBER,
) {
    FloorCard(contentPadding = 18.dp) {
        Row {
            FloorIconChip(icon, accent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
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

/* ------------------------------------------------------------------ *
 *  Workplace Spotlight (Insights)
 * ------------------------------------------------------------------ */

@Composable
fun InsightsScreen(onBack: () -> Unit) {
    EditorialScaffold("Workplace Spotlight", onBack) {
        item {
            FloorHero(
                eyebrow = "Workplace Spotlight · Your access",
                title = "Workplace Spotlight",
                subtitle = "Show us why your BPO deserves the spotlight. Real teams, real achievements, real workplace proof — shared by verified people who've earned the right to represent their workplace.",
                actions = {
                    FloorPillButton("Submit a Spotlight", onClick = {})
                    FloorPillButton("Rules of Engagement", onClick = {}, primary = false)
                },
            )
        }
        item {
            FloorInfoNote(accent = FloorAccent.TEAL) {
                FloorEyebrow("Floor Voice · Eligible to submit", accent = FloorAccent.TEAL)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Spotlight submission unlocks through recognised stature built from Participate, Learn, Contribute and Grow — subject to Trust and good standing. Every submission is moderated before publishing.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { FloorSectionHeader(title = "Inside great BPO workplaces") }
        item { FeatureCard(Icons.Filled.Star, "Recognition & awards", "Formal awards, rankings and material achievements — with supporting proof.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.TrendingUp, "Career growth", "Real promotion journeys from Agent to Team Leader and beyond.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.WorkspacePremium, "Culture & community impact", "How teams support each other, learn together and give back.", FloorAccent.CORAL) }
        item {
            ProseCard(
                "What belongs in Spotlight?",
                "Allowed: awards, recognition, career growth, learning, culture, community impact, innovation and workplace events. Personal updates belong on Pulse. If you cannot verify it, do not own it, or would not attach your verified workplace profile to it — do not submit it.",
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Academy
 * ------------------------------------------------------------------ */

@Composable
fun AcademyScreen(onBack: () -> Unit) {
    EditorialScaffold("Academy", onBack) {
        item {
            FloorHero(
                eyebrow = "The Floor · Academy",
                title = "Upskill for the role you want next.",
                subtitle = "Structured learning for contact-centre and BPO careers — from your first week on the phones to leading a floor of your own.",
                actions = { FloorPillButton("Browse the library", onClick = {}) },
            )
        }
        item {
            FloorInfoNote(accent = FloorAccent.TEAL) {
                FloorEyebrow("My Academy Passport", accent = FloorAccent.TEAL)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Learning completed through The Floor builds a portable Skills Passport. It strengthens your Learn pillar and connects directly to your Workplace Spotlight stature.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
        item { FloorSectionHeader(title = "Free learning library") }
        item { FeatureCard(Icons.Filled.School, "Contact-centre foundations", "De-escalation, active listening, and handling difficult calls with confidence.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.Checklist, "Quality & QA", "Understand scorecards, coaching feedback and how to raise your QA numbers.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.TrendingUp, "Path to Team Leader", "The skills, habits and metrics that move you from Agent to leadership.", FloorAccent.CORAL) }
    }
}

/* ------------------------------------------------------------------ *
 *  Events
 * ------------------------------------------------------------------ */

private data class EventItem(val kicker: String, val title: String, val desc: String)

private val events = listOf(
    EventItem("Floor Radio · Night Shift", "Lo-Fi Beats — for the ones awake when everyone else is asleep", "A rolling music session for the graveyard shift across every timezone."),
    EventItem("The Global Handover", "Johannesburg clocks out. Manila takes The Floor.", "One flagship network connecting BPO communities without forcing every country into the same clock."),
    EventItem("Industry Webinar", "AI quality scoring — what agents need to know", "Editorial placeholder sourced from current Call Centre Helper event listings; The Floor summarises and credits sources."),
    EventItem("Community Meetup", "Team Leaders roundtable", "Coaching, micromanagement and where to draw the line — a live discussion room."),
)

@Composable
fun EventsScreen(onBack: () -> Unit) {
    EditorialScaffold("Events", onBack) {
        item {
            FloorHero(
                eyebrow = "Industry Events · Floor Radio Live",
                title = "Where The Floor meets the industry.",
                subtitle = "Industry webinars and conferences sit alongside The Floor's own radio sessions, meetups and community broadcasts.",
                actions = { FloorPillButton("Open Floor Radio", onClick = {}, leadingIcon = Icons.Filled.Headphones) },
            )
        }
        item { FloorSectionHeader(title = "Upcoming events") }
        items(events.size) { i ->
            val e = events[i]
            FloorCard(contentPadding = 18.dp) {
                Row {
                    FloorIconChip(Icons.Filled.CalendarMonth, FloorAccent.AMBER)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        FloorEyebrow(e.kicker, accent = FloorAccent.CORAL)
                        Spacer(Modifier.height(5.dp))
                        Text(e.title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(e.desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Marketplace
 * ------------------------------------------------------------------ */

@Composable
fun MarketplaceScreen(onBack: () -> Unit) {
    EditorialScaffold("Marketplace", onBack) {
        item {
            FloorHero(
                eyebrow = "Section 13 · Commerce",
                title = "Marketplace",
                subtitle = "Member deals around the BPO lifestyle — the gear, tools and services people on the floor actually use.",
            )
        }
        item {
            FloorInfoNote {
                Text(
                    "During Beta these are illustration placeholders only — not yet partners, sponsors or endorsers of The Floor.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textMuted,
                )
            }
        }
        item { FloorSectionHeader(title = "Browse by category") }
        item { FeatureCard(Icons.Filled.Devices, "Electronics & headsets", "Noise-cancelling headsets, webcams and home-office essentials.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.LocalOffer, "Member perks", "Data bundles, transport, food and everyday savings for shift workers.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.HealthAndSafety, "Health & wellbeing", "Wellness, fitness and mental-health services at member rates.", FloorAccent.CORAL) }
    }
}

/* ------------------------------------------------------------------ *
 *  Resources
 * ------------------------------------------------------------------ */

@Composable
fun ResourcesScreen(onBack: () -> Unit) {
    EditorialScaffold("Resources", onBack) {
        item {
            FloorHero(
                eyebrow = "The Floor · Resource Desk",
                title = "The tools people actually need on shift.",
                subtitle = "Calculators, templates, QA tools, WFM references and practical BPO guides — curated in one place so agents, team leaders and operations people don't have to hunt for them.",
            )
        }
        item { FloorSectionHeader(title = "Browse by what you need") }
        item { FeatureCard(Icons.Filled.Calculate, "Calculators", "Shrinkage, occupancy, AHT and SLA calculators for the floor.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.Checklist, "Templates & checklists", "Coaching forms, QA scorecards and shift handover templates.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.AutoStories, "Guides & references", "Plain-language WFM, quality and operations references.", FloorAccent.CORAL) }
        item {
            ProseCard(
                "What we add next",
                "The Resource Desk grows from what the community asks for. Tell Walker what would save you time on shift and we'll prioritise it.",
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  Employers & Jobs
 * ------------------------------------------------------------------ */

@Composable
fun JobsScreen(onBack: () -> Unit) {
    EditorialScaffold("Employers & Jobs", onBack) {
        item {
            FloorHero(
                eyebrow = "The Floor · Employers & Jobs",
                title = "See who is hiring. Explore where your BPO career could go next.",
                subtitle = "A future bridge between verified Floor profiles and BPO employers. During Beta, the companies below are illustration placeholders only — not yet partners, sponsors or endorsers of The Floor.",
                actions = { FloorPillButton("Use my verified profile", onClick = {}) },
            )
        }
        item { FloorSectionHeader(title = "Opportunity board") }
        item { FeatureCard(Icons.Filled.Work, "Senior Agent · Voice", "Meridian Contact Solutions · Johannesburg · Hybrid", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.Work, "Team Leader · Customer Care", "Global BPO placeholder · Manila · On-site", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.Work, "QA Analyst", "Illustration employer · Bogotá · Remote", FloorAccent.CORAL) }
        item {
            ProseCard(
                "Where this can go",
                "As The Floor grows, verified members will be able to apply with a portable profile that already carries their skills, Academy passport and workplace stature — no starting from a blank CV.",
            )
        }
    }
}

/* ------------------------------------------------------------------ *
 *  About
 * ------------------------------------------------------------------ */

@Composable
fun AboutScreen(onBack: () -> Unit) {
    EditorialScaffold("About The Floor", onBack) {
        item {
            FloorHero(
                eyebrow = "Vision & Positioning",
                title = "The global home of the people behind every customer conversation.",
                subtitle = "Wherever you work. Whatever shift you're on. You're never alone.",
            )
        }
        item {
            ProseCard(
                "The problem",
                "Millions of people work in contact centres and BPOs around the world — the voice of every brand — yet they have no shared home, no common voice, and little recognition beyond their own team.",
            )
        }
        item {
            ProseCard(
                "What The Floor is",
                "A global community and ecosystem for BPO and contact-centre workers: a place to find your people, talk about the work honestly, learn and grow, earn recognition, and be seen as the professionals they are.",
            )
        }
        item {
            ProseCard(
                "What The Floor is not",
                "It is not an employer review site, not a complaints board, and not a place to leak confidential information. Spotlight is for positive, attributable workplace showcase — disputes use the appropriate support route.",
            )
        }
        item { FloorSectionHeader(title = "The Floor ecosystem") }
        item { FeatureCard(Icons.Filled.Star, "Participate · Learn · Contribute · Grow", "Four pillars that build recognised stature across the platform.", FloorAccent.AMBER) }
        item { FeatureCard(Icons.Filled.SupportAgent, "Walker, always with you", "Live human help for platform, account, payment or technical issues — from anywhere.", FloorAccent.TEAL) }
    }
}

/* ------------------------------------------------------------------ *
 *  Wellbeing / Walker
 * ------------------------------------------------------------------ */

@Composable
fun WellbeingScreen(onBack: () -> Unit) {
    EditorialScaffold("Support", onBack) {
        item {
            FloorHero(
                eyebrow = "Walker · Live Support",
                title = "Walker now travels with you.",
                subtitle = "There is no separate Walker page. Use the Walker button from anywhere on The Floor for live human help with platform, account, payment or technical issues.",
                actions = { FloorPillButton("Message Walker", onClick = {}, leadingIcon = Icons.Filled.SupportAgent) },
            )
        }
        item { FeatureCard(Icons.Filled.SupportAgent, "Human help, not a bot", "Real people across The Floor, in your timezone, when you need them.", FloorAccent.TEAL) }
        item { FeatureCard(Icons.Filled.HealthAndSafety, "Mental health check-in", "A calmer live room for stress, burnout and supporting each other — moderated with care.", FloorAccent.CORAL) }
        item {
            FloorCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Sensitive topic support", style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                    FloorBadge("24/7", tone = BadgeTone.TEAL)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "If you're struggling, you don't have to carry it alone. Walker can help you find the right support and resources.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }
    }
}
