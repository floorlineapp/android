package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme

/** Standard surface card: surface fill, 1dp soft border, 12dp radius, 16dp padding. */
@Composable
fun FloorCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = FloorTheme.colors.surface,
        border = BorderStroke(1.dp, FloorTheme.colors.borderSoft),
    ) {
        val inner = if (onClick != null) {
            Modifier
                .clip(shape)
                .clickable(onClick = onClick)
                .padding(contentPadding)
        } else {
            Modifier.padding(contentPadding)
        }
        Column(modifier = inner, content = content)
    }
}

/** Highlighted card used for the Invite & Earn entry points. */
@Composable
fun FloorAccentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier,
        shape = shape,
        color = FloorTheme.colors.surface,
        border = BorderStroke(1.dp, FloorTheme.colors.amber.copy(alpha = 0.45f)),
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(FloorTheme.colors.amberSoft)
                .let { if (onClick != null) it.clickable(onClick = onClick) else it }
                .padding(16.dp),
        ) {
            Column(content = content)
        }
    }
}
