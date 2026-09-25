package com.thefloor.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.model.HomeContent

/** First steps. */
@Composable
fun FirstStepsCard(
    content: HomeContent,
    profileComplete: Boolean,
    onOpenFloorTab: () -> Unit,
    onOpenProfileEdit: () -> Unit,
    onOpenTalk: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val joinedEnough = content.myFloors.size >= 2
    val steps = listOf(
        Step("Join two Floors", "Your country, and one for how you work.", 0, joinedEnough, onOpenFloorTab),
        Step("Complete your profile", "Your workplace is what shows you who else is there.", 50, profileComplete, onOpenProfileEdit),
        Step("Start or answer one discussion", "The fastest way to be recognised on The Floor.", 10, content.startedDiscussion, onOpenTalk),
    )
    val done = steps.count { it.done }
    if (done == steps.size) return

    FloorCard(modifier = modifier, contentPadding = 20.dp) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                FloorEyebrow("First steps", accent = FloorAccent.TEAL)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Three things worth doing first",
                    style = FloorTheme.typography.title,
                    color = FloorTheme.colors.textPrimary,
                )
            }
            FloorBadge("$done of ${steps.size}", tone = BadgeTone.TEAL)
        }

        Spacer(Modifier.height(14.dp))
        FloorProgressBar(progress = done / steps.size.toFloat())
        Spacer(Modifier.height(16.dp))

        steps.forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (step.done) Modifier else Modifier.clickableRow(step.onOpen))
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    if (step.done) "✓" else "○",
                    style = FloorTheme.typography.bodyStrong,
                    color = if (step.done) FloorTheme.colors.teal else FloorTheme.colors.textMuted,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        step.title,
                        style = FloorTheme.typography.bodyStrong,
                        color = if (step.done) FloorTheme.colors.textMuted else FloorTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(step.detail, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
                }
                if (step.points > 0 && !step.done) {
                    Spacer(Modifier.width(10.dp))
                    FloorBadge("+${step.points}", tone = BadgeTone.AMBER)
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "Points are awarded once the action is verified — never for tapping a button.",
            style = FloorTheme.typography.caption,
            color = FloorTheme.colors.textMuted,
        )
    }
}

private data class Step(
    val title: String,
    val detail: String,
    val points: Int,
    val done: Boolean,
    val onOpen: () -> Unit,
)

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier = clickable(onClick = onClick)
