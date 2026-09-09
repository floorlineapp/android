package com.thefloor.app.feature.talk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorAvatar
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.model.Post

/** Maps a Talk category to a stable badge tone so the feed reads with rhythm. */
private fun categoryTone(name: String): BadgeTone = when {
    name.contains("job", true) || name.contains("pay", true) || name.contains("progress", true) -> BadgeTone.AMBER
    name.contains("leader", true) || name.contains("coach", true) -> BadgeTone.TEAL
    name.contains("ai", true) || name.contains("future", true) -> BadgeTone.CORAL
    else -> BadgeTone.NEUTRAL
}

/** Shared discussion card for feed, home trending, and community discussions. */
@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    FloorCard(onClick = onClick, contentPadding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FloorAvatar(name = post.authorName, ring = FloorAccent.TEAL, size = 34.dp)
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(post.authorName, style = FloorTheme.typography.bodyStrong, color = FloorTheme.colors.textPrimary)
                post.authorLevel?.let { level ->
                    Text(level.label, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
                }
            }
            if (post.categoryName.isNotBlank()) {
                FloorBadge(text = post.categoryName.uppercase(), tone = categoryTone(post.categoryName))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            post.body,
            style = FloorTheme.typography.titleSm,
            color = FloorTheme.colors.textPrimary,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.StarBorder,
                    contentDescription = null,
                    tint = FloorTheme.colors.textMuted,
                    modifier = Modifier.width(15.dp).height(15.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text("${post.reactionCount} upvotes", style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = FloorTheme.colors.textMuted,
                    modifier = Modifier.width(15.dp).height(15.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text("${post.commentCount} replies", style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
            }
        }
    }
}
