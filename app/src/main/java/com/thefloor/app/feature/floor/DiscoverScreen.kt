package com.thefloor.app.feature.floor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import com.thefloor.app.core.designsystem.components.FloorHero
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.Events
import com.thefloor.app.core.common.AppError
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.CommunityRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.OfflineBanner
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.model.MembershipState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverFilter(val label: String, val kind: String?)

val discoverFilters = listOf(
    DiscoverFilter("All", null),
    DiscoverFilter("Global", "GLOBAL"),
    DiscoverFilter("Country", "COUNTRY"),
    DiscoverFilter("City", "CITY"),
    DiscoverFilter("Industry", "INDUSTRY"),
    DiscoverFilter("Role", "ROLE"),
    DiscoverFilter("Shift", "SHIFT"),
    DiscoverFilter("Level", "CAREER_LEVEL"),
    DiscoverFilter("Work mode", "WORK_MODE"),
)

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Ready(val communities: List<Community>, val offline: Boolean = false) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

@OptIn(FlowPreview::class)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    val query = MutableStateFlow("")
    val selectedKind = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            query.debounce(300).distinctUntilChanged().collect { refresh() }
        }
    }

    fun onFilter(kind: String?) {
        selectedKind.value = kind
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            communityRepository.discover(query.value, selectedKind.value)
                .onSuccess { list -> _state.update { DiscoverUiState.Ready(list) } }
                .onError { error ->
                    if (error is AppError.Network) {
                        val cached = communityRepository.cached.firstOrNull().orEmpty()
                        if (cached.isNotEmpty()) {
                            _state.update { DiscoverUiState.Ready(cached, offline = true) }
                            return@onError
                        }
                    }
                    _state.update { DiscoverUiState.Error(error.userMessage) }
                }
        }
    }

    fun toggleMembership(community: Community) {
        viewModelScope.launch {
            when (community.membershipState) {
                MembershipState.JOINED -> communityRepository.leave(community.id)
                else -> communityRepository.join(community.id).onSuccess {
                    analytics.track(Events.COMMUNITY_JOINED, mapOf("kind" to community.kind))
                }
            }
            refresh()
        }
    }
}

@Composable
fun DiscoverScreen(
    onOpenCommunity: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedKind by viewModel.selectedKind.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "The Floor") },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (val s = state) {
                DiscoverUiState.Loading -> Column {
                    DiscoverHeader(query, selectedKind, viewModel)
                    SkeletonList(rows = 5)
                }
                is DiscoverUiState.Error -> Column {
                    DiscoverHeader(query, selectedKind, viewModel)
                    FloorErrorState(message = s.message, onRetry = viewModel::refresh)
                }
                is DiscoverUiState.Ready -> {
                    if (s.offline) OfflineBanner()
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            FloorHero(
                                eyebrow = "Section · The Community",
                                title = "The Floor",
                                subtitle = "Find your community. Connect. Learn. Grow. Join any Floor to become a member — no approval needed.",
                            )
                        }
                        item {
                            FloorTextField(
                                value = query,
                                onValueChange = { viewModel.query.value = it },
                                label = "Search Floors",
                            )
                        }
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(discoverFilters, key = { it.label }) { filter ->
                                    FloorChip(
                                        text = filter.label,
                                        selected = selectedKind == filter.kind,
                                        onClick = { viewModel.onFilter(filter.kind) },
                                    )
                                }
                            }
                        }
                        if (s.communities.isEmpty()) {
                            item {
                                FloorEmptyState(
                                    title = "No Floors match",
                                    message = "Try fewer filters or a different search.",
                                    actionText = "Clear filters",
                                    onAction = { viewModel.query.value = ""; viewModel.onFilter(null) },
                                )
                            }
                        } else {
                            items(s.communities, key = { it.id }) { community ->
                                CommunityTile(
                                    community = community,
                                    onOpen = { onOpenCommunity(community.id) },
                                    onToggle = { viewModel.toggleMembership(community) },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

/** Header used for the non-Ready states (Ready renders header inside the list). */
@Composable
private fun DiscoverHeader(query: String, selectedKind: String?, viewModel: DiscoverViewModel) {
    Column(modifier = Modifier.padding(FloorTheme.spacing.gutter), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FloorHero(
            eyebrow = "Section · The Community",
            title = "The Floor",
            subtitle = "Find your community. Connect. Learn. Grow.",
        )
        FloorTextField(value = query, onValueChange = { viewModel.query.value = it }, label = "Search Floors")
    }
}

/** Derives a flag emoji from a community name; falls back to a globe. */
private fun flagFor(name: String): String {
    val n = name.lowercase()
    val map = listOf(
        "south africa" to "🇿🇦", "philippin" to "🇵🇭", "colombia" to "🇨🇴", "india" to "🇮🇳",
        "mexico" to "🇲🇽", "poland" to "🇵🇱", "jamaica" to "🇯🇲", "albania" to "🇦🇱",
        "egypt" to "🇪🇬", "brazil" to "🇧🇷", "united states" to "🇺🇸", "usa" to "🇺🇸",
        "kenya" to "🇰🇪", "nigeria" to "🇳🇬", "united kingdom" to "🇬🇧", "morocco" to "🇲🇦",
        "remote" to "🌐", "global" to "🌍",
    )
    return map.firstOrNull { n.contains(it.first) }?.second ?: "🌍"
}

@Composable
fun CommunityTile(
    community: Community,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val amber = FloorTheme.colors.amber
    val teal = FloorTheme.colors.teal
    androidx.compose.material3.Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        color = FloorTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, FloorTheme.colors.border),
    ) {
        Column {
            // Banner strip (gradient placeholder in lieu of a photo).
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(amber.copy(alpha = 0.28f), teal.copy(alpha = 0.22f)),
                        ),
                    ),
                contentAlignment = Alignment.BottomStart,
            ) {
                Text(
                    "${community.name} community",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(12.dp),
                )
            }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(flagFor(community.name), style = FloorTheme.typography.title)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        community.name.uppercase(),
                        style = FloorTheme.typography.label,
                        color = FloorTheme.colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    "%,d members".format(community.memberCount),
                    style = FloorTheme.typography.monoTag,
                    color = FloorTheme.colors.textMuted,
                )
                if (community.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(community.description, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val (label, tone) = when (community.membershipState) {
                        MembershipState.JOINED -> "Joined" to BadgeTone.TEAL
                        MembershipState.PENDING -> "Pending" to BadgeTone.AMBER
                        MembershipState.MUTED -> "Muted" to BadgeTone.NEUTRAL
                        MembershipState.RESTRICTED -> "Restricted" to BadgeTone.CORAL
                        MembershipState.NOT_JOINED -> "Join" to BadgeTone.AMBER
                    }
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(onClick = onToggle),
                        contentAlignment = Alignment.Center,
                    ) {
                        FloorBadge(text = label, tone = tone)
                    }
                }
            }
        }
    }
}
