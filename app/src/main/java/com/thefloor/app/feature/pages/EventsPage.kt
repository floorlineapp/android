package com.thefloor.app.feature.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Headphones
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
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader

@Composable
fun EventsScreen(onBack: () -> Unit, onOpenRadioPass: () -> Unit = {}) {
    EditorialScaffold("Events", onBack) {
        item {
            FloorHero(
                eyebrow = "Industry + owned programming",
                title = "Where The Floor meets the industry.",
                subtitle = "External webinars and conferences sit alongside The Floor's own radio " +
                    "sessions and meetups, in one calendar.",
                actions = {
                    FloorPillButton(
                        "Unlock Radio Pass",
                        onClick = onOpenRadioPass,
                        leadingIcon = Icons.Filled.Headphones,
                    )
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
