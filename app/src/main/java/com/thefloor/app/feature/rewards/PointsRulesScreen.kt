package com.thefloor.app.feature.rewards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTopBar

/** How points work. */
private data class EarnRule(val title: String, val points: Int, val cap: String, val source: String)

private val earnRules = listOf(
    EarnRule("Complete your verified profile", 50, "Once, ever", "Profile"),
    EarnRule("Start a Talk discussion", 10, "Max 2 qualifying posts per day", "Talk"),
    EarnRule("An answer another member marks Helpful", 25, "Capped daily", "Talk"),
    EarnRule("Floor-verified Academy learning", 25, "Once per item", "Academy"),
    EarnRule("Verified attendance at a Floor event", 50, "Once per event", "Events"),
    EarnRule("An approved Workplace Spotlight story", 75, "Once per submission", "Spotlight"),
    EarnRule("Winning an official game or competition", 150, "Official results only", "Games"),
)

@Composable
fun PointsRulesScreen(onBack: () -> Unit, onOpenTransactions: () -> Unit = {}) {
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "How points work", onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(
                start = FloorTheme.spacing.gutter,
                end = FloorTheme.spacing.gutter,
                top = 12.dp,
                bottom = 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FloorHero(
                    eyebrow = "Floor Points · Rules",
                    title = "An engagement record, not a currency.",
                    subtitle = "Floor Points are a reward for verified participation. They are not " +
                        "cash, they cannot be bought, and they cannot be transferred between members.",
                )
            }

            item {
                FloorInfoNote(accent = FloorAccent.TEAL) {
                    Text(
                        "Every award is written to your ledger with an event ID, a source and a timestamp.",
                        style = FloorTheme.typography.bodyStrong,
                        color = FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "The event ID is what stops the same action paying twice — a duplicate ID is " +
                            "rejected by the ledger itself, not by the app. That is also why points " +
                            "arrive after an action is verified rather than the moment you tap.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                }
            }

            item {
                FloorSectionHeader(
                    title = "Every way to earn",
                    subtitle = "Seven actions. Nothing else pays.",
                    linkText = "Your ledger",
                    onLink = onOpenTransactions,
                )
            }
            items(earnRules.size) { i ->
                val rule = earnRules[i]
                FloorCard(contentPadding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            rule.title,
                            style = FloorTheme.typography.bodyStrong,
                            color = FloorTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(10.dp))
                        FloorBadge("+${rule.points}", tone = BadgeTone.TEAL)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text(
                            rule.cap,
                            style = FloorTheme.typography.caption,
                            color = FloorTheme.colors.textMuted,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Source: ${rule.source}",
                            style = FloorTheme.typography.monoTag,
                            color = FloorTheme.colors.textMuted,
                        )
                    }
                }
            }

            item { FloorSectionHeader(title = "What points do not do") }
            item {
                FloorCard(contentPadding = 18.dp) {
                    listOf(
                        "They are not cash and cannot be withdrawn.",
                        "They cannot be sent to another member, or pooled.",
                        "They cannot be bought, and no purchase affects them.",
                        "Invite & Grow never adds them — referrals are a separate record entirely.",
                        "Pulse never adds them — that boundary keeps Pulse low-stakes.",
                    ).forEach { line ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text("—", style = FloorTheme.typography.body, color = FloorTheme.colors.coral)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                line,
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    }
                }
            }

            item {
                FloorCard(contentPadding = 18.dp) {
                    FloorEyebrow("If a point is missing", accent = FloorAccent.AMBER)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Tell Walker which action it was and roughly when. Because every award " +
                            "carries a source and a timestamp, a missing one can be traced rather " +
                            "than guessed at.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                }
            }
        }
    }
}
