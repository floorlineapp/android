package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.common.countryFlag
import com.thefloor.app.core.designsystem.FloorTheme

/**
 * Who wrote this, and what they have earned.
 *
 * The Recognition ladder used to be visible only on your own profile, which
 * makes it a private score — and a private score motivates nobody. Every
 * authored thing in the app now carries the same identity line: avatar, flag,
 * name, tier, and the job title underneath. That is what turns Floor Points
 * from a number you check into something other people can see you have.
 *
 * The tier tone is deliberate: amber for the top rung so Workplace Ambassadors
 * read differently at a glance, teal for the earned middle, muted for everyone
 * still on their way up. Nothing here shames a new member — an unranked person
 * simply shows no badge.
 */
@Composable
fun FloorAuthorLine(
    name: String,
    modifier: Modifier = Modifier,
    tier: String? = null,
    countryCode: String? = null,
    subtitle: String? = null,
    avatarSize: Dp = 34.dp,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FloorAvatar(name = name, ring = tierAccent(tier), size = avatarSize)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val flag = countryFlag(countryCode)
                if (flag != null) {
                    Text(flag, style = FloorTheme.typography.body)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    name,
                    style = FloorTheme.typography.bodyStrong,
                    color = FloorTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
            // Tier sits on its own line rather than competing with the name for
            // width. Putting it beside the name meant either a wrapped badge or
            // a truncated person — and a truncated name is the worse of the two.
            val showTier = !tier.isNullOrBlank() && tier != "Member"
            if (showTier || !subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showTier) {
                        Text(
                            tier!!,
                            style = FloorTheme.typography.monoTag,
                            color = tierColor(tier),
                            maxLines = 1,
                        )
                    }
                    if (showTier && !subtitle.isNullOrBlank()) {
                        Text(
                            "  ·  ",
                            style = FloorTheme.typography.caption,
                            color = FloorTheme.colors.textMuted,
                        )
                    }
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = FloorTheme.typography.caption,
                            color = FloorTheme.colors.textMuted,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun tierColor(tier: String?): androidx.compose.ui.graphics.Color = when (tier) {
    "Workplace Ambassador" -> FloorTheme.colors.amber
    "Floor Voice", "Recognised Member" -> FloorTheme.colors.teal
    else -> FloorTheme.colors.textMuted
}

private fun tierAccent(tier: String?): FloorAccent = when (tier) {
    "Workplace Ambassador" -> FloorAccent.AMBER
    "Floor Voice", "Recognised Member" -> FloorAccent.TEAL
    else -> FloorAccent.FAINT
}
