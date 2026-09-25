package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

@Composable
fun AcademyScreen(
    onBack: () -> Unit,
    passport: AcademyViewModel.Passport = AcademyViewModel.Passport(
        role = "Senior Agent", tenure = "4 yr", tier = "Floor Voice",
        learningPoints = 340, completed = 9, verified = 4,
    ),
) {
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

        item {
            FloorCard(contentPadding = 18.dp) {
                FloorEyebrow("My Academy Passport", accent = FloorAccent.TEAL)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PassportStat("Role", passport.role)
                    PassportStat("Tenure", passport.tenure)
                    PassportStat("Tier", passport.tier)
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    PassportStat("Learning Points", passport.learningPoints.toString())
                    PassportStat("Completed", "${passport.completed} items")
                    PassportStat("Verified", "${passport.verified} items")
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
internal fun PassportStat(label: String, value: String) {
    Column {
        Text(label.uppercase(), style = FloorTheme.typography.eyebrow, color = FloorTheme.colors.textMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
    }
}

internal data class EventItem(
    val kicker: String,
    val title: String,
    val desc: String,
    val floorOwned: Boolean,
    val when_: String,
)

internal val events = listOf(
    EventItem("Floor Radio · Night Shift", "Lo-Fi Beats — for the ones awake when everyone else is asleep", "A rolling music session for the graveyard shift across every timezone.", true, "Tonight · 22:00 local"),
    EventItem("Floor Radio · Flagship", "The Global Handover: Johannesburg clocks out, Manila takes The Floor", "One network connecting BPO communities without forcing every country into the same clock.", true, "Thursday · 16:00 SAST"),
    EventItem("Industry webinar", "AI quality scoring — what agents need to know", "External listing. The Floor summarises and credits the organiser; registration happens on their site.", false, "24 Sep · 14:00 BST"),
    EventItem("Industry conference", "Contact centre operations summit", "External listing from the industry calendar.", false, "2 Oct · 09:00 CET"),
    EventItem("Community meetup", "Team Leaders roundtable", "Coaching, micromanagement and where to draw the line — a live discussion room.", true, "Saturday · 11:00 PHT"),
)
