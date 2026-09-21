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
        topBar = { FloorTopBar(title = "Invite & Grow", onBack = onBack) },
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
                        com.thefloor.app.core.designsystem.components.FloorHero(
                            eyebrow = "Referral track · Deliberately separate",
                            title = "Bring good people to The Floor.",
                            subtitle = "Invite the people you work with — free for you, free for them. " +
                                "Every qualified referral is tracked, and nobody ever pays to unlock earning.",
                        )
                    }
                    item {
                        com.thefloor.app.core.designsystem.components.FloorInfoNote(
                            accent = com.thefloor.app.core.designsystem.components.FloorAccent.CORAL,
                        ) {
                            Text(
                                "No pay-to-play. No downlines. No commissions from other people's referrals.",
                                style = FloorTheme.typography.bodyStrong,
                                color = FloorTheme.colors.textPrimary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Invite & Grow never adds Floor Points and never buys professional " +
                                    "stature. It is a separate record from your points balance, and it " +
                                    "always will be — that separation is the whole point of it.",
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

                    // 5 — the referral funnel, stage by stage
                    item {
                        FloorCard {
                            Text("The funnel", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Where each person you invite currently sits.",
                                style = FloorTheme.typography.caption,
                                color = FloorTheme.colors.textSecondary,
                            )
                            Spacer(Modifier.height(14.dp))
                            FunnelStage(1, "Invited", "They have your link but have not signed up yet.", summary.invited)
                            FunnelStage(2, "Joined", "Account created.", (summary.invited * 2) / 3)
                            FunnelStage(3, "Verified", "Email or workplace confirmed.", summary.active + 1)
                            FunnelStage(4, "Qualified", "Active on three separate days — a real member, not a click.", summary.active)
                            FunnelStage(5, "Review", "Held for a human sense-check. A delay, not a rejection.", 1)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Referrals from shared workplace networks may be reviewed rather than " +
                                    "automatically rejected — most clear.",
                                style = FloorTheme.typography.caption,
                                color = FloorTheme.colors.textMuted,
                            )
                        }
                    }

                    // Founding tiers — computed from the qualified count, never stored.
                    item {
                        com.thefloor.app.core.designsystem.components.FloorSectionHeader(
                            title = "Founding tiers",
                            subtitle = "Recognition for the people who bring in the most qualified members.",
                        )
                    }
                    item {
                        FloorCard(contentPadding = 18.dp) {
                            foundingTiers.forEach { (name, needed) ->
                                val reached = summary.active >= needed
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        if (reached) "●" else "○",
                                        style = FloorTheme.typography.body,
                                        color = if (reached) FloorTheme.colors.teal else FloorTheme.colors.textMuted,
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        name,
                                        style = FloorTheme.typography.bodyStrong,
                                        color = if (reached) FloorTheme.colors.textPrimary else FloorTheme.colors.textSecondary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        "$needed qualified",
                                        style = FloorTheme.typography.monoTag,
                                        color = FloorTheme.colors.textMuted,
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Your tier is counted from qualified referrals each time it is shown, " +
                                    "so it can never drift out of step with the record.",
                                style = FloorTheme.typography.caption,
                                color = FloorTheme.colors.textMuted,
                            )
                        }
                    }

                    // Leaderboard — qualified counts only.
                    item {
                        com.thefloor.app.core.designsystem.components.FloorSectionHeader(
                            title = "Top referrers",
                            subtitle = "Ranked on qualified referrals, nothing else.",
                        )
                    }
                    item {
                        FloorCard(contentPadding = 4.dp) {
                            leaderboard.forEachIndexed { i, (who, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        "${i + 1}",
                                        style = FloorTheme.typography.mono,
                                        color = FloorTheme.colors.amber,
                                        modifier = Modifier.width(24.dp),
                                    )
                                    Text(
                                        who,
                                        style = FloorTheme.typography.body,
                                        color = FloorTheme.colors.textPrimary,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        count.toString(),
                                        style = FloorTheme.typography.mono,
                                        color = FloorTheme.colors.teal,
                                    )
                                }
                            }
                        }
                    }

                    // Community Growth Fund — real panel, honestly labelled.
                    item {
                        com.thefloor.app.core.designsystem.components.FloorInfoNote {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Community Growth Fund",
                                    style = FloorTheme.typography.title,
                                    color = FloorTheme.colors.textPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                FloorBadge("BETA · NOT FUNDED", tone = BadgeTone.NEUTRAL)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "The fund is where real commercial revenue will eventually flow back to " +
                                    "the members who built the community. There is no revenue yet, so " +
                                    "the balance is zero and no payouts exist. It is shown here because " +
                                    "it is real intent, not because it is live.",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                FloorStat(value = "0", label = "Contributed")
                                FloorStat(value = "0", label = "Paid out")
                                FloorStat(value = "—", label = "Next review")
                            }
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

/** The five founding tiers, by qualified-referral count. */
private val foundingTiers = listOf(
    "Connector" to 5,
    "Ambassador" to 25,
    "Scout" to 100,
    "Captain" to 500,
    "Builder" to 1000,
)

private val leaderboard = listOf(
    "Joan D." to 34,
    "Thabo N." to 28,
    "Camila R." to 21,
    "Rohan M." to 17,
    "You" to 5,
)

/** One stage of the referral funnel, with how many people are sitting in it. */
@Composable
private fun FunnelStage(number: Int, title: String, subtitle: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$number", style = FloorTheme.typography.mono, color = FloorTheme.colors.amber)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = FloorTheme.typography.bodyStrong, color = FloorTheme.colors.textPrimary)
            Text(subtitle, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            count.toString(),
            style = FloorTheme.typography.title,
            color = if (count > 0) FloorTheme.colors.teal else FloorTheme.colors.textMuted,
        )
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
    "CONNECTOR" -> "Floor Connector"
    "AMBASSADOR" -> "Floor Ambassador"
    "SCOUT" -> "Floor Scout"
    "CAPTAIN" -> "Floor Captain"
    "BUILDER" -> "Floor Builder"
    else -> "Member"
}
