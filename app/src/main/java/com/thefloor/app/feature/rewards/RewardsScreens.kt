package com.thefloor.app.feature.rewards

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
import com.thefloor.app.core.data.RewardsRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.RewardTransaction
import com.thefloor.app.core.model.RewardsSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RewardsUiState(
    val loading: Boolean = true,
    val summary: RewardsSummary? = null,
    val transactions: List<RewardTransaction> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository,
) : ViewModel() {

    val state = MutableStateFlow(RewardsUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            rewardsRepository.summary()
                .onSuccess { summary -> state.update { it.copy(loading = false, summary = summary, error = null) } }
                .onError { e -> state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            rewardsRepository.transactions()
                .onSuccess { txs -> state.update { it.copy(loading = false, transactions = txs, error = null) } }
                .onError { e -> state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }
}

@Composable
fun RewardsScreen(
    onBack: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenInvite: () -> Unit,
    onOpenProfileEdit: () -> Unit = {},
    onOpenFloorTab: () -> Unit = {},
    viewModel: RewardsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Rewards & Games", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> SkeletonList(rows = 4, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.summary != null -> {
                val summary = state.summary!!
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        com.thefloor.app.core.designsystem.components.FloorHero(
                            eyebrow = "Floor Rewards · Transparent by design",
                            title = "Good things should feel good — and make sense.",
                            subtitle = "Earn Floor Points for verified participation across The Floor. Every point has a source, a timestamp and a transaction record, so you always know why your balance changed.",
                        )
                    }
                    item {
                        FloorCard {
                            Text("Your balance", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    summary.creditsBalance.toString(),
                                    style = FloorTheme.typography.monoL,
                                    color = FloorTheme.colors.amber,
                                )
                                Spacer(Modifier.padding(horizontal = 4.dp))
                                Text("Floor Credits", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                            }
                        }
                    }
                    item {
                        com.thefloor.app.core.designsystem.components.FloorSectionHeader(
                            title = "Ways to earn",
                            subtitle = "Participation, not payment.",
                        )
                    }
                    items(summary.waysToEarn, key = { it.title }) { way ->
                        // Every "way to earn" navigates somewhere — dispatch via the
                        // deep-link parser (substring matching misroutes: every brand
                        // URI contains "floor").
                        FloorCard(onClick = {
                            when (com.thefloor.app.domain.DeepLinkParser.parse(way.deepLink)) {
                                com.thefloor.app.domain.DeepLinkParser.Target.ProfileEdit -> onOpenProfileEdit()
                                com.thefloor.app.domain.DeepLinkParser.Target.FloorTab -> onOpenFloorTab()
                                else -> onOpenInvite()
                            }
                        }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(way.title, style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary)
                                Text(
                                    "+${way.credits}",
                                    style = FloorTheme.typography.mono,
                                    color = FloorTheme.colors.teal,
                                )
                            }
                        }
                    }
                    item {
                        FloorCard {
                            Text("Games & competitions", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Friday Trivia, Fastest Case Resolvers, Customer Service Quiz Bowl — coming soon.",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    }
                    item {
                        FloorCard(contentPadding = 0.dp) {
                            FloorListItem(title = "Transaction history", onClick = onOpenTransactions)
                            FloorListItem(title = "Earn more with Invite & Earn", onClick = onOpenInvite)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardTransactionsScreen(
    onBack: () -> Unit,
    viewModel: RewardsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadTransactions() }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Transactions", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> SkeletonList(rows = 6, modifier = Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::loadTransactions,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.transactions.isEmpty() -> FloorEmptyState(
                title = "Nothing here yet",
                message = "Credits you earn will show up here.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.transactions, key = { it.id }) { tx ->
                    FloorCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(reasonLabel(tx.reason), style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary)
                                Text(
                                    TimeAgo.format(tx.createdAt).let { if (it == "now") "just now" else "$it ago" },
                                    style = FloorTheme.typography.caption,
                                    color = FloorTheme.colors.textMuted,
                                )
                            }
                            Text(
                                (if (tx.deltaCredits > 0) "+" else "") + tx.deltaCredits,
                                style = FloorTheme.typography.mono,
                                color = if (tx.deltaCredits >= 0) FloorTheme.colors.teal else FloorTheme.colors.coral,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun reasonLabel(reason: String): String = when (reason) {
    "SIGNUP_BONUS" -> "Welcome bonus"
    "PROFILE_COMPLETE" -> "Profile completed"
    "FIRST_POST" -> "First post"
    "DAILY_CHECKIN" -> "Daily check-in"
    "REFERRAL_SIGNUP" -> "Referral signed up"
    "REFERRAL_ACTIVE" -> "Referral went active"
    "MILESTONE" -> "Milestone reward"
    "REDEMPTION" -> "Reward redeemed"
    "REVERSAL" -> "Adjustment"
    "ADMIN_ADJUST" -> "Adjustment"
    else -> reason
}
