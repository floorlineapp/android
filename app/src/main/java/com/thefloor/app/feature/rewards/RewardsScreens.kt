package com.thefloor.app.feature.rewards

import androidx.compose.foundation.background
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
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorInfoNote
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
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

    // The ledger section needs the transaction list, not just the summary.
    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.loadTransactions() }

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
            state.summary != null -> RewardsBody(
                summary = state.summary!!,
                transactions = state.transactions,
                modifier = Modifier.padding(padding),
                onOpenTransactions = onOpenTransactions,
                onOpenInvite = onOpenInvite,
                onOpenProfileEdit = onOpenProfileEdit,
                onOpenFloorTab = onOpenFloorTab,
            )
        }
    }
}

private data class GameCard(val name: String, val desc: String, val from: Long, val to: Long)

/** Competitions carried over from the prototype, gradients included. */
private val games = listOf(
    GameCard("Friday Trivia: Telecom Night", "Live every Friday · 200 pts to the winning floor.", 0xFF5B2A86, 0xFFA23E9C),
    GameCard("Fastest Case Resolvers", "This month's leaderboard for first-contact resolution.", 0xFFB8860B, 0xFFE0A527),
    GameCard("Customer Service Quiz Bowl", "A monthly knockout tournament between BPOs.", 0xFF1F5C6B, 0xFF2F9E8F),
    GameCard("Photo Challenge: Culture in Action", "Show us your floor — ends in 3 days.", 0xFF9A3B2F, 0xFFD97B3F),
)

private data class Redeemable(val name: String, val cost: Int, val desc: String)

private val redeemables = listOf(
    Redeemable("Airtime / data voucher", 800, "Partner-funded mobile airtime or data reward."),
    Redeemable("Wellness app — 1 month pass", 1200, "A selected wellbeing benefit from an approved partner."),
    Redeemable("Academy course credit", 2000, "Credit toward selected future paid partner learning."),
    Redeemable("Noise-cancelling headset raffle entry", 500, "One entry into an official Floor reward draw."),
)

/** Stateless Rewards body — internal so the screenshot suite can render it. */
@Composable
internal fun RewardsBody(
    summary: RewardsSummary,
    transactions: List<RewardTransaction>,
    modifier: Modifier = Modifier,
    onOpenTransactions: () -> Unit = {},
    onOpenInvite: () -> Unit = {},
    onOpenProfileEdit: () -> Unit = {},
    onOpenFloorTab: () -> Unit = {},
) {
    val earned = transactions.filter { it.deltaCredits > 0 }.sumOf { it.deltaCredits }
    val redeemed = -transactions.filter { it.deltaCredits < 0 }.sumOf { it.deltaCredits }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(FloorTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            FloorHero(
                eyebrow = "Floor Rewards · Transparent by design",
                title = "Good things should feel good — and make sense.",
                subtitle = "Earn Floor Points for verified participation across The Floor. Every point has a source, a timestamp and a transaction record, so you always know why your balance changed.",
                actions = {
                    FloorPillButton("How points work", onClick = onOpenTransactions)
                    FloorPillButton("Full history", onClick = onOpenTransactions, primary = false)
                },
            )
        }

        // "Three different things — kept separate."
        item {
            FloorInfoNote(accent = FloorAccent.TEAL) {
                Text(
                    "Three different things — kept separate.",
                    style = FloorTheme.typography.titleSm,
                    color = FloorTheme.colors.textPrimary,
                )
                Spacer(Modifier.height(10.dp))
                SeparationRow("Floor Points", "rewards & engagement")
                SeparationRow("Learning Points", "Academy progress")
                SeparationRow("Invite & Grow", "referral record · future earning")
                Spacer(Modifier.height(8.dp))
                Text(
                    "Invite & Grow never adds Floor Points and never buys professional stature.",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textSecondary,
                )
            }
        }

        // Balance + metric grid
        item {
            FloorCard {
                FloorEyebrow("Available balance", accent = FloorAccent.FAINT)
                Spacer(Modifier.height(6.dp))
                Text(
                    "%,d".format(summary.creditsBalance),
                    style = FloorTheme.typography.displayL,
                    color = FloorTheme.colors.amber,
                )
                Text("Floor Points", style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Total earned", "%,d".format(earned), "Lifetime ledger credits", Modifier.weight(1f))
                MetricCard("Total redeemed", "%,d".format(redeemed), "Lifetime reward spend", Modifier.weight(1f))
            }
        }

        // Ways to earn
        item {
            FloorSectionHeader(
                title = "Ways to earn Floor Points",
                subtitle = "The system awards points only after the qualifying action is verified.",
            )
        }
        items(summary.waysToEarn, key = { it.title }) { way ->
            // Dispatch via the deep-link parser — substring matching misroutes,
            // since every brand URI contains "floor".
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
                    Text(way.title, style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    FloorBadge("+${way.credits} PTS", tone = BadgeTone.TEAL)
                }
            }
        }

        // Ledger
        if (transactions.isNotEmpty()) {
            item {
                FloorSectionHeader(
                    title = "Your recent points history",
                    subtitle = "Every entry has a source and a timestamp.",
                    linkText = "Full ledger",
                    onLink = onOpenTransactions,
                )
            }
            item {
                FloorCard(contentPadding = 4.dp) {
                    transactions.take(6).forEach { tx ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(tx.reason, style = FloorTheme.typography.bodyStrong, color = FloorTheme.colors.textPrimary)
                                Text(TimeAgo.format(tx.createdAt), style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
                            }
                            Text(
                                if (tx.deltaCredits >= 0) "+${tx.deltaCredits}" else "${tx.deltaCredits}",
                                style = FloorTheme.typography.mono,
                                color = if (tx.deltaCredits >= 0) FloorTheme.colors.teal else FloorTheme.colors.coral,
                            )
                        }
                    }
                }
            }
        }

        // Games & competitions
        item {
            FloorSectionHeader(
                title = "Games & competitions",
                subtitle = "Winner points are posted from official results — never self-claimed.",
            )
        }
        items(games, key = { it.name }) { g ->
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                color = androidx.compose.ui.graphics.Color.Transparent,
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    androidx.compose.ui.graphics.Color(g.from),
                                    androidx.compose.ui.graphics.Color(g.to),
                                ),
                            ),
                        )
                        .padding(20.dp),
                ) {
                    Text(g.name, style = FloorTheme.typography.title, color = androidx.compose.ui.graphics.Color.White)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        g.desc,
                        style = FloorTheme.typography.body,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.88f),
                    )
                }
            }
        }

        // Redeem
        item {
            FloorSectionHeader(
                title = "Redeem Floor Points",
                subtitle = "Points unlock rewards and benefits. They are not cash and cannot be transferred into Invite & Grow.",
            )
        }
        items(redeemables, key = { it.name }) { r ->
            FloorCard {
                Text(r.name, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(r.desc, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("%,d Floor pts".format(r.cost), style = FloorTheme.typography.mono, color = FloorTheme.colors.amber)
                    FloorPillButton(
                        "Redeem",
                        onClick = onOpenTransactions,
                        enabled = summary.creditsBalance >= r.cost,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun SeparationRow(name: String, detail: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(name, style = FloorTheme.typography.bodyStrong, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(detail, style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
    }
}

@Composable
private fun MetricCard(label: String, value: String, help: String, modifier: Modifier = Modifier) {
    FloorCard(modifier = modifier, contentPadding = 16.dp) {
        FloorEyebrow(label, accent = FloorAccent.FAINT)
        Spacer(Modifier.height(6.dp))
        Text(value, style = FloorTheme.typography.monoL, color = FloorTheme.colors.teal)
        Spacer(Modifier.height(2.dp))
        Text(help, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
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
