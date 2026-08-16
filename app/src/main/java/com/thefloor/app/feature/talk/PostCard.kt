package com.thefloor.app.feature.talk

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.common.TimeAgo
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.model.Post

/** Shared post card for feed, home trending, and community discussions. */
@Composable
fun PostCard(post: Post, onClick: () -> Unit) {
    FloorCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(post.authorName, style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
            post.authorLevel?.let { level ->
                Spacer(Modifier.width(8.dp))
                FloorBadge(text = level.label, tone = BadgeTone.NEUTRAL)
            }
            Spacer(Modifier.width(8.dp))
            Text(TimeAgo.format(post.createdAt), style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            post.body,
            style = FloorTheme.typography.body,
            color = FloorTheme.colors.textPrimary,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(10.dp))
        Row {
            if (post.categoryName.isNotBlank()) {
                FloorBadge(text = post.categoryName, tone = BadgeTone.AMBER)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "${post.reactionCount} reactions · ${post.commentCount} comments",
                style = FloorTheme.typography.caption,
                color = FloorTheme.colors.textMuted,
            )
        }
    }
}
