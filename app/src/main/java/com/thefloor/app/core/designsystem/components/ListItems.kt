package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme

/** Icon / label / chevron row used across More and Settings. */
@Composable
fun FloorListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = FloorTheme.spacing.touchTarget)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = FloorTheme.spacing.gutter, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon, contentDescription = null,
                tint = if (enabled) FloorTheme.colors.amber else FloorTheme.colors.textMuted,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = FloorTheme.typography.bodyL,
                color = if (enabled) FloorTheme.colors.textPrimary else FloorTheme.colors.textMuted,
            )
            if (subtitle != null) {
                Text(subtitle, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = FloorTheme.colors.textMuted,
            )
        }
    }
}
