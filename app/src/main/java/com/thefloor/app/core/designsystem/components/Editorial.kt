package com.thefloor.app.core.designsystem.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.pressScale

/** Accent role for eyebrows / accents across the editorial kit. */
enum class FloorAccent { AMBER, TEAL, CORAL, FAINT }

@Composable
private fun FloorAccent.color(): Color = when (this) {
    FloorAccent.AMBER -> FloorTheme.colors.amber
    FloorAccent.TEAL -> FloorTheme.colors.teal
    FloorAccent.CORAL -> FloorTheme.colors.coral
    FloorAccent.FAINT -> FloorTheme.colors.textMuted
}

@Composable
private fun FloorAccent.soft(): Color = when (this) {
    FloorAccent.AMBER -> FloorTheme.colors.amberSoft
    FloorAccent.TEAL -> FloorTheme.colors.tealSoft
    FloorAccent.CORAL -> FloorTheme.colors.coralSoft
    FloorAccent.FAINT -> FloorTheme.colors.surfaceAlt
}

/** Mono, uppercase, letter-spaced eyebrow — the signature label of the brand. */
@Composable
fun FloorEyebrow(
    text: String,
    modifier: Modifier = Modifier,
    accent: FloorAccent = FloorAccent.AMBER,
) {
    Text(
        text.uppercase(),
        style = FloorTheme.typography.eyebrow,
        color = accent.color(),
        modifier = modifier,
    )
}

/** Fully-rounded pill button used inside heroes and call-to-actions. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FloorPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val view = LocalView.current
    val shape = RoundedCornerShape(999.dp)
    val container = if (primary) FloorTheme.colors.amber else Color.Transparent
    val content = if (primary) FloorTheme.colors.onAmber else FloorTheme.colors.textPrimary
    Surface(
        onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            onClick()
        },
        modifier = modifier.pressScale(interaction),
        enabled = enabled,
        shape = shape,
        color = container,
        contentColor = content,
        border = if (primary) null else BorderStroke(1.dp, FloorTheme.colors.border),
        interactionSource = interaction,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 44.dp)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(17.dp), tint = content)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = FloorTheme.typography.label)
        }
    }
}

/**
 * Editorial hero: amber + teal radial wash over the surface, soft border, 18dp
 * radius. Eyebrow / title / subtitle then any [actions] (usually pill buttons,
 * laid out left-to-right with 10dp gaps).
 */
@Composable
fun FloorHero(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    eyebrowAccent: FloorAccent = FloorAccent.AMBER,
    subtitle: String? = null,
    actions: (@Composable () -> Unit)? = null,
) {
    val amber = FloorTheme.colors.amber
    val teal = FloorTheme.colors.teal
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = FloorTheme.colors.surface,
        border = BorderStroke(1.dp, FloorTheme.colors.border),
    ) {
        Column(
            modifier = Modifier
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(amber.copy(alpha = 0.12f), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.20f),
                            radius = size.maxDimension * 0.55f,
                        ),
                    )
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(teal.copy(alpha = 0.09f), Color.Transparent),
                            center = Offset(size.width * 0.9f, size.height * 0.85f),
                            radius = size.maxDimension * 0.5f,
                        ),
                    )
                }
                .padding(horizontal = 22.dp, vertical = 26.dp),
        ) {
            if (eyebrow != null) {
                FloorEyebrow(eyebrow, accent = eyebrowAccent)
                Spacer(Modifier.height(12.dp))
            }
            Text(title, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(10.dp))
                Text(subtitle, style = FloorTheme.typography.bodyL, color = FloorTheme.colors.textSecondary)
            }
            if (actions != null) {
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { actions() }
            }
        }
    }
}

/** Section header: display title left, optional teal "see all →" link right. */
@Composable
fun FloorSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    linkText: String? = null,
    onLink: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(subtitle, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
            }
        }
        if (linkText != null && onLink != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onLink)
                    .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(linkText, style = FloorTheme.typography.label, color = FloorTheme.colors.teal)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = FloorTheme.colors.teal,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/** Square "jump back in" tile: accent icon chip, display title, muted desc. */
@Composable
fun FloorTile(
    icon: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: FloorAccent = FloorAccent.AMBER,
) {
    FloorCard(modifier = modifier, onClick = onClick, contentPadding = 20.dp) {
        FloorIconChip(icon, accent)
        Spacer(Modifier.height(14.dp))
        Text(title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.height(5.dp))
        Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
    }
}

/** 38dp rounded icon chip with a soft accent fill. */
@Composable
fun FloorIconChip(
    icon: ImageVector,
    accent: FloorAccent = FloorAccent.AMBER,
    size: Dp = 38.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.soft()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = accent.color(), modifier = Modifier.size(size * 0.5f))
    }
}

/** Home KPI card: icon chip + display number + faint label. */
@Composable
fun FloorKpiCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: FloorAccent = FloorAccent.TEAL,
) {
    FloorCard(modifier = modifier, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloorIconChip(icon, accent)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                Text(label, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
            }
        }
    }
}

/** Circular avatar with a colored ring and initials. */
@Composable
fun FloorAvatar(
    name: String,
    modifier: Modifier = Modifier,
    ring: FloorAccent = FloorAccent.TEAL,
    size: Dp = 36.dp,
) {
    val initials = name.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(FloorTheme.colors.surfaceAlt)
            .border(2.dp, ring.color(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
    }
}

/** Small teal "live" dot. */
@Composable
fun FloorLiveDot(modifier: Modifier = Modifier, color: Color = FloorTheme.colors.teal) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}

/** Teal verified pill: check + label. */
@Composable
fun FloorVerifiedBadge(text: String = "Workplace verified", modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = FloorTheme.colors.tealSoft,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = FloorTheme.colors.teal,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(text.uppercase(), style = FloorTheme.typography.monoTag, color = FloorTheme.colors.teal)
        }
    }
}

/** Live-room row: teal icon chip, topic, desc, "N online now" in mono teal. */
@Composable
fun FloorLiveRoomCard(
    icon: ImageVector,
    topic: String,
    desc: String,
    onlineText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloorCard(modifier = modifier, onClick = onClick, contentPadding = 16.dp) {
        Row {
            FloorIconChip(icon, FloorAccent.TEAL)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(topic, style = FloorTheme.typography.bodyStrong, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorLiveDot()
                    Spacer(Modifier.width(6.dp))
                    Text(onlineText, style = FloorTheme.typography.monoTag, color = FloorTheme.colors.teal)
                }
            }
        }
    }
}

/** Bordered informational note; FAINT keeps the plain surface, accents tint it. */
@Composable
fun FloorInfoNote(
    modifier: Modifier = Modifier,
    accent: FloorAccent = FloorAccent.FAINT,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (accent == FloorAccent.FAINT) FloorTheme.colors.surface else accent.soft(),
        border = BorderStroke(
            1.dp,
            if (accent == FloorAccent.FAINT) FloorTheme.colors.border else accent.color().copy(alpha = 0.35f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
