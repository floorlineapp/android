package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefloor.app.R
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.RewardsRepository
import com.thefloor.app.core.data.UserRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorVerifiedBadge
import com.thefloor.app.core.model.RecognitionLevel
import com.thefloor.app.core.network.SpotlightSubmissionDto
import com.thefloor.app.feature.spotlight.SPOTLIGHT_CATEGORIES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    mySubmissions: List<SpotlightSubmissionDto> = emptyList(),
    onOpenRules: () -> Unit = {},
    onOpenSubmit: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenAcademy: () -> Unit = {},
) {
    var category by remember { mutableStateOf(SPOTLIGHT_CATEGORIES.first()) }
    val balance = 12_480
    val threshold = RecognitionLevel.FLOOR_VOICE.threshold
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
                    FloorPillButton("Submit a Spotlight", onClick = onOpenSubmit, enabled = eligible)
                    FloorPillButton("Rules of engagement", onClick = onOpenRules, primary = false)
                    FloorPillButton("See my recognition level", onClick = onOpenProfile, primary = false)
                    FloorPillButton("Open Academy", onClick = onOpenAcademy, primary = false)
                },
            )
        }

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
                Spacer(Modifier.height(8.dp))
                Text(
                    "%,d / %,d Floor Points".format(balance, threshold),
                    style = FloorTheme.typography.mono,
                    color = FloorTheme.colors.textSecondary,
                )
                Spacer(Modifier.height(10.dp))
                if (workplaceVerified) {
                    FloorVerifiedBadge()
                } else {
                    FloorBadge("Workplace not verified", tone = BadgeTone.CORAL)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "An approved story pays +75 Floor Points back into your ledger. " +
                        "Nothing is ever self-approved — every submission is reviewed first.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }

        item {
            FloorSectionHeader(
                title = "Your access",
                subtitle = "The same ladder your profile shows — one system, read from two places.",
            )
        }
        item {
            FloorCard(contentPadding = 16.dp) {
                RecognitionLevel.entries.drop(1).forEach { rung ->
                    val name = rung.label
                    val needed = rung.threshold
                    val reached = balance >= needed
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            if (reached) "●" else "○",
                            style = FloorTheme.typography.body,
                            color = if (reached) FloorTheme.colors.teal else FloorTheme.colors.textMuted,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            name,
                            style = FloorTheme.typography.bodyStrong,
                            color = if (reached) FloorTheme.colors.textPrimary else FloorTheme.colors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        if (name == "Floor Voice") {
                            FloorBadge("Unlocks Spotlight", tone = BadgeTone.AMBER)
                        } else {
                            Text(
                                "%,d".format(needed),
                                style = FloorTheme.typography.monoTag,
                                color = FloorTheme.colors.textMuted,
                            )
                        }
                    }
                }
            }
        }

        if (mySubmissions.isNotEmpty()) {
            item {
                FloorSectionHeader(
                    title = "Your submissions",
                    subtitle = "Every one is read by a person before anything is published.",
                )
            }
            items(mySubmissions.size) { i ->
                val sub = mySubmissions[i]
                FloorCard(contentPadding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FloorEyebrow(sub.category, accent = FloorAccent.FAINT, modifier = Modifier.weight(1f))
                        FloorBadge(
                            when (sub.status) {
                                "approved" -> "Approved · +75"
                                "rejected" -> "Needs changes"
                                else -> "In review"
                            },
                            tone = when (sub.status) {
                                "approved" -> BadgeTone.TEAL
                                "rejected" -> BadgeTone.CORAL
                                else -> BadgeTone.AMBER
                            },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(sub.title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                    if (sub.reviewerNote != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            sub.reviewerNote,
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.coral,
                        )
                    }
                }
            }
        }

        item { FloorSectionHeader(title = "Browse the showcase") }
        item { ChipRow(SPOTLIGHT_CATEGORIES, category) { category = it } }

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

internal val academyTabs = listOf("Skills Passport", "My Learning", "Verification")

internal val competencyAreas = listOf(
    Triple("Customer conversation", "De-escalation, active listening, tone and recovery.", Icons.Filled.RecordVoiceOver),
    Triple("Quality & compliance", "Scorecards, calibration, data handling and regulated scripts.", Icons.Filled.Checklist),
    Triple("Workforce & operations", "Shrinkage, occupancy, forecasting and the numbers behind the rota.", Icons.Filled.Calculate),
    Triple("Leadership", "Coaching, one-to-ones, and the move from Agent to Team Leader.", Icons.Filled.Groups),
    Triple("Technology & AI", "What automation actually changes about the agent's day.", Icons.Filled.Bolt),
    Triple("Wellbeing & resilience", "Shift recovery, burnout signals and sustainable performance.", Icons.Filled.HealthAndSafety),
    Triple("Money & career", "Pay structures, saving on shift pay, and planning the next role.", Icons.Filled.Savings),
)

internal data class Course(val title: String, val desc: String, val area: String, val image: Int, val points: Int)

internal val academyCourses = listOf(
    Course("AI for Customer Service", "How AI is reshaping quality, routing and the agent's day.", "Technology & AI", R.drawable.img_ai_for_customer_service, 25),
    Course("Call Center Excellence", "De-escalation, active listening and handling difficult calls.", "Customer conversation", R.drawable.img_call_center_excellence, 25),
    Course("Communication Skills", "The craft behind every good customer conversation.", "Customer conversation", R.drawable.img_communication_skills, 25),
    Course("Data Analysis for Everyone", "Read your own numbers — AHT, CSAT, shrinkage and beyond.", "Workforce & operations", R.drawable.img_data_analysis_for_everyone, 25),
)

@HiltViewModel
class AcademyViewModel @javax.inject.Inject constructor(
    private val userRepository: UserRepository,
    private val rewardsRepository: RewardsRepository,
) : ViewModel() {
    data class Passport(
        val role: String = "—",
        val tenure: String = "—",
        val tier: String = "Member",
        val learningPoints: Int = 0,
        val completed: Int = 0,
        val verified: Int = 0,
    )

    val passport = MutableStateFlow(Passport())

    init {
        viewModelScope.launch {
            var points = 0
            rewardsRepository.summary().onSuccess { points = it.creditsBalance.toInt() }
            userRepository.me().onSuccess { profile ->
                val workplaceVerified = profile.emailVerified && !profile.employer.isNullOrBlank()
                val level = com.thefloor.app.core.model.recognitionLevel(
                    floorPoints = points,
                    workplaceVerified = workplaceVerified,
                    trustedHistory = workplaceVerified && profile.completeness >= 80,
                )
                passport.value = Passport(
                    role = profile.role ?: profile.careerLevel?.label ?: "—",
                    tenure = profile.experienceYears?.let { "${'$'}it yr" } ?: "—",
                    tier = level.label,
                    learningPoints = 340,
                    completed = 9,
                    verified = 4,
                )
            }
        }
    }
}
