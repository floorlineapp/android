package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

@Composable
fun JobsScreen(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit = {},
    onOpenAcademy: () -> Unit = {},
) {
    EditorialScaffold("Employers & Jobs", onBack) {
        item {
            FloorHero(
                eyebrow = "Illustration now, partnership later",
                title = "See who is hiring. Explore where your BPO career could go next.",
                subtitle = "A future bridge between verified Floor profiles and BPO employers.",
                actions = {
                    FloorPillButton("Use my verified profile", onClick = onOpenProfile)
                    FloorPillButton("Build skills in Academy", onClick = onOpenAcademy, primary = false)
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

internal val brandFamily = listOf(
    "The Floor" to "The community itself — find your people, wherever you work.",
    "The Floor Radio" to "Six regional live feeds and the shows that run across them.",
    "The Floor Rewards" to "The points ledger: earn, see why, redeem.",
    "The Floor Ambassadors" to "The people who bring the most qualified members in.",
    "The Floor Academy" to "Tracked learning and a portable Skills Passport.",
    "The Floor Marketplace" to "Member deals across the BPO lifestyle.",
    "The Floor Index" to "What the work actually pays and looks like, by market.",
    "The Floor Jobs" to "Where a verified profile becomes an opportunity.",
)
