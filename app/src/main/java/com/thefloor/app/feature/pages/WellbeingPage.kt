package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.R
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

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
