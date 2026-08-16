package com.thefloor.app.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme

@Composable
fun FloorLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = FloorTheme.colors.amber)
    }
}

/** Shimmer block for skeleton layouts. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .height(height)
            .alpha(alpha)
            .clip(RoundedCornerShape(cornerRadius))
            .background(FloorTheme.colors.surfaceAlt),
    )
}

/** Standard list skeleton: N card-shaped shimmer rows. */
@Composable
fun SkeletonList(rows: Int = 4, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FloorTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(rows) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth(), height = 88.dp, cornerRadius = 12.dp)
        }
    }
}

@Composable
fun FloorEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = FloorTheme.colors.textMuted,
                modifier = Modifier.height(40.dp))
            Spacer(Modifier.height(16.dp))
        }
        Text(title, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary,
            textAlign = TextAlign.Center)
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(20.dp))
            FloorPrimaryButton(text = actionText, onClick = onAction)
        }
    }
}

@Composable
fun FloorErrorState(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null,
            tint = FloorTheme.colors.coral, modifier = Modifier.height(40.dp))
        Spacer(Modifier.height(16.dp))
        Text("Something went wrong", style = FloorTheme.typography.title,
            color = FloorTheme.colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = FloorTheme.typography.body,
            color = FloorTheme.colors.textSecondary, textAlign = TextAlign.Center)
        if (onRetry != null) {
            Spacer(Modifier.height(20.dp))
            FloorSecondaryButton(text = "Retry", onClick = onRetry)
        }
    }
}

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FloorTheme.colors.surfaceAlt)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null,
            tint = FloorTheme.colors.textMuted, modifier = Modifier.height(16.dp))
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(
            "You're offline — showing what we saved",
            style = FloorTheme.typography.caption,
            color = FloorTheme.colors.textSecondary,
        )
    }
}
