package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

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
