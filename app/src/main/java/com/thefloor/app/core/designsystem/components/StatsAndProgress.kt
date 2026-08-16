package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme

/** Compact data primitive: mono value over caption label (Invite & Earn, Rewards). */
@Composable
fun FloorStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    Column(
        modifier = modifier.semantics { contentDescription = "$label: $value" },
    ) {
        Text(
            value,
            style = FloorTheme.typography.monoL,
            color = if (emphasized) FloorTheme.colors.amber else FloorTheme.colors.textPrimary,
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
    }
}

/** Brand progress bar: soft track, amber fill. [progress] in 0f..1f. */
@Composable
fun FloorProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Column(modifier = modifier) {
        if (label != null) {
            Text(label, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
            Spacer(Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(FloorTheme.colors.borderSoft)
                .semantics { contentDescription = "Progress ${(clamped * 100).toInt()} percent" },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(FloorTheme.colors.amber),
            )
        }
    }
}
