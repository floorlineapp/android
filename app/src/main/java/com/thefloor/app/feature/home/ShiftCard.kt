package com.thefloor.app.feature.home

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.common.ShiftClock
import com.thefloor.app.core.common.ShiftPhase
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.color
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.model.HomeContent

/**
 * The shift card.
 *
 * Home used to open with the same block whatever time it was. This is the one
 * piece of the app that knows what time it is for the person holding it: what
 * they missed while they were off, what is short enough to read on a break,
 * what their shift added up to, and who else is awake at 3am.
 *
 * Every number in it is real — posts in the last day, unread mentions, points
 * actually earned today — so it changes when the app changes rather than
 * reading like copy.
 */
@Composable
fun ShiftCard(
    content: HomeContent,
    phase: ShiftPhase,
    onOpenTalk: () -> Unit,
    onOpenRadio: () -> Unit,
    onOpenRewards: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = specFor(content, phase)
    val accent = spec.accent

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = FloorTheme.colors.surface,
        border = BorderStroke(1.dp, accent.color().copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(accent.color().copy(alpha = 0.12f), FloorTheme.colors.surface),
                    ),
                )
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                com.thefloor.app.core.designsystem.components.FloorIconChip(spec.icon, accent)
                Spacer(Modifier.width(12.dp))
                Column {
                    FloorEyebrow(spec.eyebrow, accent = accent)
                    Spacer(Modifier.height(3.dp))
                    Text(
                        spec.title,
                        style = FloorTheme.typography.headline,
                        color = FloorTheme.colors.textPrimary,
                    )
                }
            }

            if (spec.lines.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                spec.lines.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // The figure leads. It is the reason to look at the card.
                        val animated by animateIntAsState(targetValue = line.value, label = "shiftFigure")
                        Text(
                            if (line.plus && animated > 0) "+%,d".format(animated) else "%,d".format(animated),
                            style = FloorTheme.typography.title,
                            color = accent.color(),
                            modifier = Modifier.width(72.dp),
                        )
                        Text(
                            line.label,
                            style = FloorTheme.typography.body,
                            color = FloorTheme.colors.textSecondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            if (spec.footnote != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    spec.footnote,
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textMuted,
                )
            }

            Spacer(Modifier.height(16.dp))
            FloorPillButton(
                text = spec.action,
                onClick = when (spec.target) {
                    Target.TALK -> onOpenTalk
                    Target.RADIO -> onOpenRadio
                    Target.REWARDS -> onOpenRewards
                },
            )
        }
    }
}

private enum class Target { TALK, RADIO, REWARDS }

private data class Figure(val value: Int, val label: String, val plus: Boolean = false)

private data class ShiftSpec(
    val eyebrow: String,
    val title: String,
    val lines: List<Figure>,
    val footnote: String?,
    val action: String,
    val target: Target,
    val icon: ImageVector,
    val accent: FloorAccent,
)

@Composable
private fun specFor(content: HomeContent, phase: ShiftPhase): ShiftSpec {
    val firstName = content.displayName.substringBefore(' ').ifBlank { "there" }
    return when (phase) {
        ShiftPhase.BEFORE -> {
            val handover = ShiftClock.nextHandover()
            ShiftSpec(
                eyebrow = "Before your shift",
                title = "While you were off",
                lines = listOfNotNull(
                    Figure(content.postsSinceYesterday, "new posts on your Floors"),
                    Figure(content.mentions, "mentions waiting for you").takeIf { content.mentions > 0 },
                ),
                footnote = handover?.let {
                    "${it.city} hands over in ${it.minutes} minutes."
                } ?: "The Floor is awake somewhere. It always is.",
                action = "Catch up on Talk",
                target = Target.TALK,
                icon = Icons.Filled.WbTwilight,
                accent = FloorAccent.AMBER,
            )
        }

        ShiftPhase.ON_FLOOR -> ShiftSpec(
            eyebrow = "On the floor",
            title = "Got six minutes?",
            lines = listOf(
                Figure(content.trendingPosts.size, "short reads picked for this break"),
                Figure(content.activeConversations, "conversations running right now"),
            ),
            footnote = content.trendingPosts.firstOrNull()?.let { "“${it.body.take(90)}${if (it.body.length > 90) "…" else ""}”" },
            action = "Read them",
            target = Target.TALK,
            icon = Icons.Filled.Coffee,
            accent = FloorAccent.TEAL,
        )

        ShiftPhase.AFTER -> ShiftSpec(
            eyebrow = "After your shift",
            title = "Your shift, wrapped",
            lines = listOfNotNull(
                Figure(content.postsSinceYesterday, "posts on your Floors today"),
                Figure(content.pointsEarnedToday, "Floor Points earned today", plus = true)
                    .takeIf { content.pointsEarnedToday > 0 },
                Figure(content.mentions, "people mentioned you").takeIf { content.mentions > 0 },
            ),
            footnote = if (content.pointsEarnedToday > 0) {
                "Every point has a source and a timestamp. Tap through to see where each one came from."
            } else {
                "No points today. Answering one question is worth twenty-five."
            },
            action = if (content.pointsEarnedToday > 0) "See your ledger" else "Find a question",
            target = if (content.pointsEarnedToday > 0) Target.REWARDS else Target.TALK,
            icon = Icons.Filled.DoneAll,
            accent = FloorAccent.AMBER,
        )

        ShiftPhase.NIGHT -> ShiftSpec(
            eyebrow = "Night shift",
            title = "You're not the only one awake, $firstName",
            lines = listOf(
                Figure(content.presenceCount, "people on The Floor right now"),
            ),
            footnote = "Night Shift is on Floor Radio — low-light energy and company until the handover.",
            action = "Open Floor Radio",
            target = Target.RADIO,
            icon = Icons.Filled.DarkMode,
            accent = FloorAccent.CORAL,
        )
    }
}
