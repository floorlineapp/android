package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme

@Composable
fun FloorChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        label = { Text(text, style = FloorTheme.typography.label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = FloorTheme.colors.surfaceAlt,
            labelColor = FloorTheme.colors.textSecondary,
            selectedContainerColor = FloorTheme.colors.amberSoft,
            selectedLabelColor = FloorTheme.colors.amber,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = FloorTheme.colors.borderSoft,
            selectedBorderColor = FloorTheme.colors.amber,
        ),
    )
}

enum class BadgeTone { NEUTRAL, AMBER, TEAL, CORAL }

/** Pill badge: career levels, statuses, verified marks. */
@Composable
fun FloorBadge(
    text: String,
    tone: BadgeTone = BadgeTone.NEUTRAL,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
) {
    val (bg, fg, borderColor) = when (tone) {
        BadgeTone.NEUTRAL -> Triple(FloorTheme.colors.surfaceAlt, FloorTheme.colors.textSecondary, FloorTheme.colors.borderSoft)
        BadgeTone.AMBER -> Triple(FloorTheme.colors.amberSoft, FloorTheme.colors.amber, Color.Transparent)
        BadgeTone.TEAL -> Triple(FloorTheme.colors.tealSoft, FloorTheme.colors.teal, Color.Transparent)
        BadgeTone.CORAL -> Triple(FloorTheme.colors.coralSoft, FloorTheme.colors.coral, Color.Transparent)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        border = if (borderColor == Color.Transparent) null else BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showDot) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(fg, CircleShape),
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
            }
            Text(text, style = FloorTheme.typography.caption, color = fg)
        }
    }
}

/** Maps user-safe referral statuses to badge tones — one place, used everywhere. */
fun referralStatusTone(status: String): BadgeTone = when (status) {
    "ACTIVE", "REVENUE_GENERATED", "REWARD_PAID" -> BadgeTone.TEAL
    "SIGNED_UP", "PROFILE_COMPLETE", "REWARD_PENDING" -> BadgeTone.AMBER
    "UNDER_REVIEW" -> BadgeTone.CORAL
    else -> BadgeTone.NEUTRAL
}
