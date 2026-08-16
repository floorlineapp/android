package com.thefloor.app.feature.invite

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.R
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.Events
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.ReferralRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton
import com.thefloor.app.core.designsystem.components.FloorStat
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.ReferralSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InviteUiState {
    data object Loading : InviteUiState
    data class Ready(val summary: ReferralSummary) : InviteUiState
    data class Error(val message: String) : InviteUiState
}

@HiltViewModel
class InviteEarnViewModel @Inject constructor(
    private val referralRepository: ReferralRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<InviteUiState>(InviteUiState.Loading)
    val state: StateFlow<InviteUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            referralRepository.summary()
                .onSuccess { summary -> _state.update { InviteUiState.Ready(summary) } }
                .onError { error -> _state.update { InviteUiState.Error(error.userMessage) } }
        }
    }

    fun recordShare(channel: String) {
        analytics.track(Events.INVITE_LINK_SHARED, mapOf("channel" to channel))
        viewModelScope.launch { referralRepository.recordShare(channel) }
    }
}

@Composable
fun InviteEarnScreen(
    onBack: () -> Unit,
    onOpenMilestones: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFaq: () -> Unit,
    viewModel: InviteEarnViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Invite & Earn", onBack = onBack) },
    ) { padding ->
        when (val s = state) {
            InviteUiState.Loading -> SkeletonList(rows = 5, modifier = Modifier.padding(padding))
            is InviteUiState.Error -> FloorErrorState(
                message = s.message,
                onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            is InviteUiState.Ready -> {
                val summary = s.summary
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 1 — header
                    item {
                        Column {
                            Text("Bring your floor with you.", style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Invite the people you work with. Free for you, free for them.",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    }

                    // 2 — earnings summary (all server values)
                    item {
                        FloorCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                FloorStat(value = summary.invited.toString(), label = "Invited")
                                FloorStat(value = summary.active.toString(), label = "Active", emphasized = true)
                                FloorStat(value = summary.credits.toString(), label = "Credits")
                                FloorStat(value = summary.cashEarned.format(), label = "Cash earned")
                            }
                        }
                    }

                    // 3+4 — link + share
                    item {
                        FloorCard {
                            Text("Your personal link", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                            Spacer(Modifier.height(6.dp))
                            Text(summary.link, style = FloorTheme.typography.mono, color = FloorTheme.colors.amber)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FloorSecondaryButton(
                                    text = "Copy",
                                    onClick = {
                                        clipboard.setText(AnnotatedString(summary.link))
                                        viewModel.recordShare("copy")
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                FloorPrimaryButton(
                                    text = "Share",
                                    onClick = {
                                        // Native Android share sheet — no hard-coded app dependencies.
                                        val message = context.getString(R.string.invite_share_message, summary.link)
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Invite to The Floor"))
                                        viewModel.recordShare("share_sheet")
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    // 5 — how it works
                    item {
                        FloorCard {
                            Text("How it works", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                            Spacer(Modifier.height(12.dp))
                            HowItWorksStep(1, "Share your link", "Send it to the people on your floor.")
                            HowItWorksStep(2, "They join free", "Signing up costs nothing. Ever.")
                            HowItWorksStep(3, "They go active", "Profile complete + one Floor joined.")
                            HowItWorksStep(4, "You earn from real Floor revenue", "Bonuses are funded by The Floor's own business revenue.")
                        }
                    }

                    // 6 — next milestone
                    summary.nextMilestone?.let { next ->
                        item {
                            FloorCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Next milestone", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                                    FloorBadge(
                                        text = "${next.remaining} to go",
                                        tone = BadgeTone.AMBER,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                FloorProgressBar(
                                    progress = next.progressPct / 100f,
                                    label = "${next.currentActive} of ${next.thresholdActive} active referrals",
                                )
                                Spacer(Modifier.height(12.dp))
                                next.rewards.forEach { reward ->
                                    val text = when (reward.kind) {
                                        "CREDITS" -> "${reward.credits} Floor Credits"
                                        "CASH" -> "${reward.cash?.format()} cash bonus"
                                        else -> reward.perkText ?: reward.statusGrant?.let { statusLabel(it) } ?: ""
                                    }
                                    Text("→ $text", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Reward figures are subject to final program terms.",
                                    style = FloorTheme.typography.caption,
                                    color = FloorTheme.colors.textMuted,
                                )
                            }
                        }
                    }

                    // 8 — status
                    item {
                        FloorCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Your status", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        statusLabel(summary.ambassadorStatus),
                                        style = FloorTheme.typography.title,
                                        color = FloorTheme.colors.textPrimary,
                                    )
                                }
                                FloorBadge(
                                    text = statusLabel(summary.ambassadorStatus),
                                    tone = if (summary.ambassadorStatus == "NONE") BadgeTone.NEUTRAL else BadgeTone.TEAL,
                                )
                            }
                        }
                    }

                    // 7, 9, 10 — deeper sections
                    item {
                        FloorCard(contentPadding = 0.dp) {
                            FloorListItem(title = "All milestones", onClick = onOpenMilestones)
                            FloorListItem(title = "Referral history", onClick = onOpenHistory)
                            FloorListItem(title = "FAQ", onClick = onOpenFaq)
                        }
                    }

                    // Legal strip — server-driven disclosures.
                    item {
                        Column {
                            summary.disclosures.forEach { line ->
                                Text(
                                    line,
                                    style = FloorTheme.typography.caption,
                                    color = FloorTheme.colors.textMuted,
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HowItWorksStep(number: Int, title: String, subtitle: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            "$number",
            style = FloorTheme.typography.mono,
            color = FloorTheme.colors.amber,
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
            Text(subtitle, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
        }
    }
}

fun statusLabel(status: String): String = when (status) {
    "AMBASSADOR" -> "Floor Ambassador"
    "SCOUT" -> "Floor Scout"
    "CAPTAIN" -> "Floor Captain"
    else -> "Member"
}
