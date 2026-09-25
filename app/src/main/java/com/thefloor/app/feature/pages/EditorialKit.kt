package com.thefloor.app.feature.pages

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.SpotlightRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.floorListPadding
import com.thefloor.app.core.network.SpotlightSubmissionDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
internal fun EditorialScaffold(
    title: String,
    onBack: () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = title, onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = floorListPadding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

/** Horizontal filter chip row with local selection. */
@Composable
internal fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(options.size) { i ->
            FloorChip(
                text = options[i],
                selected = options[i] == selected,
                onClick = { onSelect(options[i]) },
            )
        }
    }
}

/** Icon + title + description feature card. */
@Composable
internal fun FeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    accent: FloorAccent = FloorAccent.AMBER,
    trailing: String? = null,
) {
    FloorCard(contentPadding = 18.dp) {
        Row {
            FloorIconChip(icon, accent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = FloorTheme.typography.titleSm,
                        color = FloorTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (trailing != null) {
                        Spacer(Modifier.width(8.dp))
                        FloorBadge(trailing, tone = BadgeTone.TEAL)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
        }
    }
}

/** Card fronted by real artwork — the partner/course imagery carried over from the web prototype so these… */
@Composable
internal fun PhotoCard(
    @DrawableRes image: Int,
    title: String,
    desc: String,
    badge: String? = null,
) {
    FloorCard(contentPadding = 0.dp) {
        Image(
            painter = androidx.compose.ui.res.painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f),
        )
        Column(Modifier.padding(16.dp)) {
            if (badge != null) {
                FloorEyebrow(badge, accent = FloorAccent.AMBER)
                Spacer(Modifier.height(6.dp))
            }
            Text(title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
        }
    }
}

/** Titled prose card. */
@Composable
internal fun ProseCard(title: String, body: String) {
    FloorCard {
        Text(title, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(body, style = FloorTheme.typography.bodyL, color = FloorTheme.colors.textSecondary)
    }
}

/** "Where this can go" panel. */
@Composable
internal fun RoadmapPanel(title: String, lines: List<String>) {
    FloorInfoNote(accent = FloorAccent.CORAL) {
        FloorEyebrow("Where this can go", accent = FloorAccent.CORAL)
        Spacer(Modifier.height(6.dp))
        Text(title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        lines.forEach { line ->
            Row(Modifier.padding(bottom = 6.dp)) {
                Text("—", style = FloorTheme.typography.body, color = FloorTheme.colors.coral)
                Spacer(Modifier.width(8.dp))
                Text(line, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "Not built yet — listed so you know where The Floor is heading.",
            style = FloorTheme.typography.caption,
            color = FloorTheme.colors.textMuted,
        )
    }
}

/** The standing Beta disclosure for pages that show placeholder brands. */
@Composable
internal fun PlaceholderNote(text: String) {
    FloorInfoNote {
        FloorEyebrow("Illustration only", accent = FloorAccent.FAINT)
        Spacer(Modifier.height(6.dp))
        Text(text, style = FloorTheme.typography.body, color = FloorTheme.colors.textMuted)
    }
}

internal data class SpotlightStory(
    val category: String,
    val company: String,
    val country: String,
    val title: String,
    val proof: String,
)

internal val spotlightStories = listOf(
    SpotlightStory("Awards & Recognition", "Meridian Contact Solutions", "South Africa", "Voice team takes national CX award", "Award certificate and published results attached to the submission."),
    SpotlightStory("People & Culture", "Northbank Support Group", "Philippines", "Night shift gets a proper canteen — at 2am", "Photographs, internal announcement and sixteen colleagues co-signed."),
    SpotlightStory("Community Impact", "Arcadia BPO", "Kenya", "Floor raises school fees for forty children", "Receipts and the school's letter of acknowledgement."),
    SpotlightStory("Career Growth", "Lighthouse Customer Care", "Colombia", "Eleven agents promoted to Team Leader this year", "HR confirmation and each member's own verified profile."),
    SpotlightStory("Innovation", "Vantage Voice", "India", "Agents rewrote the QA scorecard themselves", "Before and after scorecards, plus the quality manager's sign-off."),
    SpotlightStory("Workplace Events", "Harbour Line Services", "Poland", "The first all-site handover party", "Event photos and the internal invitation."),
)

@HiltViewModel
class SpotlightViewModel @javax.inject.Inject constructor(
    private val spotlight: SpotlightRepository,
) : ViewModel() {
    val mine = MutableStateFlow<List<SpotlightSubmissionDto>>(emptyList())

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            spotlight.submissions().onSuccess { mine.value = it }
        }
    }
}
