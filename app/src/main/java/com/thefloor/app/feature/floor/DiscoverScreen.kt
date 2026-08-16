package com.thefloor.app.feature.floor

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
        topBar = { FloorTopBar(title = "Find your Floor") },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(horizontal = FloorTheme.spacing.gutter)) {
                FloorTextField(
                    value = query,
                    onValueChange = { viewModel.query.value = it },
                    label = "Search Floors",
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(discoverFilters, key = { it.label }) { filter ->
                        FloorChip(
                            text = filter.label,
                            selected = selectedKind == filter.kind,
                            onClick = { viewModel.onFilter(filter.kind) },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            when (val s = state) {
                DiscoverUiState.Loading -> SkeletonList(rows = 5)
                is DiscoverUiState.Error -> FloorErrorState(message = s.message, onRetry = viewModel::refresh)
                is DiscoverUiState.Ready -> {
                    if (s.offline) OfflineBanner()
                    if (s.communities.isEmpty()) {
                        FloorEmptyState(
                            title = "No Floors match",
                            message = "Try fewer filters or a different search.",
                            actionText = "Clear filters",
                            onAction = { viewModel.query.value = ""; viewModel.onFilter(null) },
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(s.communities, key = { it.id }) { community ->
                                CommunityTile(
                                    community = community,
                                    onOpen = { onOpenCommunity(community.id) },
                                    onToggle = { viewModel.toggleMembership(community) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityTile(
    community: Community,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    FloorCard(onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(community.name, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${community.memberCount} members",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textMuted,
                )
            }
            val (label, tone) = when (community.membershipState) {
                MembershipState.JOINED -> "Joined" to BadgeTone.TEAL
                MembershipState.PENDING -> "Pending" to BadgeTone.AMBER
                MembershipState.MUTED -> "Muted" to BadgeTone.NEUTRAL
                MembershipState.RESTRICTED -> "Restricted" to BadgeTone.CORAL
                MembershipState.NOT_JOINED -> "Join" to BadgeTone.AMBER
            }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                FloorBadge(text = label, tone = tone)
            }
        }
    }
}
