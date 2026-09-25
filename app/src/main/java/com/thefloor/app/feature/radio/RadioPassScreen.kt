package com.thefloor.app.feature.radio

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTopBar

/**
 * The Radio Pass upgrade screen.
 *
 * "Unlock Radio Pass" used to go nowhere. It now lands here.
 *
 * Deliberately not a checkout: there is no payment provider connected, no price
 * agreed and no terms written, so the screen says that in plain words and
 * offers to tell the member when it opens rather than collecting anything. A
 * fake payment step would be the worst possible thing to put in front of a
 * workforce that gets targeted by subscription scams.
 */
private data class PassPerk(val title: String, val detail: String, val included: Boolean)

private val perks = listOf(
    PassPerk("Every regional feed, on demand", "Replay any show from any of the six regions, not just the live stream.", true),
    PassPerk("Download for the commute", "Keep shows on the device for journeys with no signal.", true),
    PassPerk("Ad-free listening", "No sponsor breaks in any region.", true),
    PassPerk("Priority in region chat", "Your requests surface to the host first.", true),
    PassPerk("Early access to Floor Sessions", "Interviews and live sets before they go public.", true),
    PassPerk("Submit your own music", "Free for everyone — the Pass is never a condition of being heard.", false),
)

@Composable
fun RadioPassScreen(onBack: () -> Unit) {
    var notify by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Radio Pass", onBack = onBack) },
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
                    eyebrow = "Floor Radio · Pass",
                    eyebrowAccent = FloorAccent.CORAL,
                    title = "The whole station, on your schedule.",
                    subtitle = "Floor Radio stays free and live for everyone. The Pass is for people " +
                        "who want it on demand — every region, every show, offline.",
                )
            }

            item {
                FloorInfoNote(accent = FloorAccent.AMBER) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Not on sale yet",
                            style = FloorTheme.typography.title,
                            color = FloorTheme.colors.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        FloorBadge("COMING", tone = BadgeTone.AMBER)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "There is no price set, no payment provider connected and no subscription " +
                            "terms published. Rather than show you a checkout that cannot complete, " +
                            "this page tells you what the Pass will be and lets you ask to be told " +
                            "when it opens.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                }
            }

            item { FloorSectionHeader(title = "What the Pass adds", subtitle = "Everything else on Floor Radio stays free.") }
            items(perks.size) { i ->
                val perk = perks[i]
                FloorCard(contentPadding = 16.dp) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            if (perk.included) "★" else "○",
                            style = FloorTheme.typography.bodyStrong,
                            color = if (perk.included) FloorTheme.colors.amber else FloorTheme.colors.teal,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                perk.title,
                                style = FloorTheme.typography.bodyStrong,
                                color = FloorTheme.colors.textPrimary,
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                perk.detail,
                                style = FloorTheme.typography.caption,
                                color = FloorTheme.colors.textMuted,
                            )
                        }
                        if (!perk.included) {
                            Spacer(Modifier.width(8.dp))
                            FloorBadge("FREE", tone = BadgeTone.TEAL)
                        }
                    }
                }
            }

            item {
                if (notify) {
                    FloorInfoNote(accent = FloorAccent.TEAL) {
                        FloorEyebrow("You're on the list", accent = FloorAccent.TEAL)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "We'll let you know here when the Pass opens. Nothing has been charged " +
                                "and no payment details were asked for.",
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.textSecondary,
                        )
                    }
                } else {
                    FloorPrimaryButton(
                        text = "Tell me when it opens",
                        onClick = { notify = true },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item {
                Text(
                    "Floor Points are not involved in the Pass, in either direction: points cannot " +
                        "buy it, and holding it does not earn them.",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textMuted,
                )
            }
        }
    }
}
