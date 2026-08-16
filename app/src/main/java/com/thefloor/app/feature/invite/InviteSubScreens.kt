package com.thefloor.app.feature.invite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.TimeAgo
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.ReferralRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.designsystem.components.referralStatusTone
import com.thefloor.app.core.model.FaqItem
import com.thefloor.app.core.model.MilestoneTier
import com.thefloor.app.core.model.ReferralHistoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InviteSubState<T>(
    val loading: Boolean = true,
    val data: T? = null,
    val error: String? = null,
)

@HiltViewModel
class InviteSubViewModel @Inject constructor(
    private val referralRepository: ReferralRepository,
) : ViewModel() {

    val milestones = MutableStateFlow(InviteSubState<List<MilestoneTier>>())
    val history = MutableStateFlow(InviteSubState<List<ReferralHistoryItem>>())
    val faq = MutableStateFlow(InviteSubState<List<FaqItem>>())

    fun loadMilestones() {
        viewModelScope.launch {
            referralRepository.milestones()
                .onSuccess { data -> milestones.update { InviteSubState(loading = false, data = data) } }
                .onError { e -> milestones.update { InviteSubState(loading = false, error = e.userMessage) } }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            referralRepository.history()
                .onSuccess { data -> history.update { InviteSubState(loading = false, data = data) } }
                .onError { e -> history.update { InviteSubState(loading = false, error = e.userMessage) } }
        }
    }

    fun loadFaq() {
        viewModelScope.launch {
            referralRepository.faq()
                .onSuccess { data -> faq.update { InviteSubState(loading = false, data = data) } }
                .onError { e -> faq.update { InviteSubState(loading = false, error = e.userMessage) } }
        }
    }
}

@Composable
fun InviteMilestonesScreen(
    onBack: () -> Unit,
    viewModel: InviteSubViewModel = hiltViewModel(),
) {
    val state by viewModel.milestones.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadMilestones() }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Milestones", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> SkeletonList(rows = 5, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::loadMilestones,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.data.orEmpty(), key = { it.thresholdActive }) { tier ->
                    FloorCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${tier.thresholdActive} active referrals",
                                style = FloorTheme.typography.title,
                                color = FloorTheme.colors.textPrimary,
                            )
                            FloorBadge(
                                text = when (tier.state) {
                                    "PAID" -> "Paid"
                                    "UNLOCKED" -> "Unlocked"
                                    else -> "Locked"
                                },
                                tone = when (tier.state) {
                                    "PAID" -> BadgeTone.TEAL
                                    "UNLOCKED" -> BadgeTone.AMBER
                                    else -> BadgeTone.NEUTRAL
                                },
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        tier.rewards.forEach { reward ->
                            val text = when (reward.kind) {
                                "CREDITS" -> "${reward.credits} Floor Credits"
                                "CASH" -> "${reward.cash?.format()} cash bonus"
                                else -> reward.perkText ?: reward.statusGrant?.let { statusLabel(it) } ?: ""
                            }
                            Text("→ $text", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                        }
                    }
                }
                item {
                    Text(
                        "Milestone values are illustrative and subject to final program terms. " +
                            "Cash bonuses are funded by The Floor's own revenue and require verification.",
                        style = FloorTheme.typography.caption,
                        color = FloorTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
fun InviteHistoryScreen(
    onBack: () -> Unit,
    viewModel: InviteSubViewModel = hiltViewModel(),
) {
    val state by viewModel.history.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadHistory() }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Referral history", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> SkeletonList(rows = 5, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::loadHistory,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.data.isNullOrEmpty() -> FloorEmptyState(
                title = "No referrals yet",
                message = "Share your link to see referrals appear here.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.data.orEmpty(), key = { it.id }) { item ->
                    FloorCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                // Privacy: referred members are shown as anonymous entries.
                                Text("Referral", style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
                                Text(
                                    TimeAgo.format(item.createdAt) + " ago",
                                    style = FloorTheme.typography.caption,
                                    color = FloorTheme.colors.textMuted,
                                )
                            }
                            FloorBadge(
                                text = historyStatusLabel(item.displayStatus),
                                tone = referralStatusTone(item.displayStatus),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InviteFaqScreen(
    onBack: () -> Unit,
    viewModel: InviteSubViewModel = hiltViewModel(),
) {
    val state by viewModel.faq.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadFaq() }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Invite & Earn FAQ", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> SkeletonList(rows = 5, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::loadFaq,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.data.orEmpty(), key = { it.question }) { item ->
                    FloorCard {
                        Text(item.question, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(item.answer, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    }
                }
            }
        }
    }
}

private fun historyStatusLabel(status: String): String = when (status) {
    "INVITED" -> "Invited"
    "SIGNED_UP" -> "Signed up"
    "PROFILE_COMPLETE" -> "Profile complete"
    "ACTIVE" -> "Active"
    "REVENUE_GENERATED" -> "Revenue generated"
    "REWARD_PENDING" -> "Reward pending"
    "REWARD_PAID" -> "Reward paid"
    "UNDER_REVIEW" -> "Under review"
    else -> status
}
