package com.thefloor.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.AppError
import com.thefloor.app.core.common.ShiftGreeting
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.HomeRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccentCard
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorStat
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.OfflineBanner
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.HomeContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Ready(val content: HomeContent, val offline: Boolean = false) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var lastGood: HomeContent? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            homeRepository.load()
                .onSuccess { content ->
                    lastGood = content
                    _state.update { HomeUiState.Ready(content) }
                }
                .onError { error ->
                    val cached = lastGood
                    _state.update {
                        when {
                            cached != null && error is AppError.Network -> HomeUiState.Ready(cached, offline = true)
                            else -> HomeUiState.Error(error.userMessage)
                        }
                    }
                }
        }
    }

    fun greeting(): String = ShiftGreeting.greetingFor(LocalTime.now())
}

@Composable
fun HomeScreen(
    onOpenInvite: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenFloorTab: () -> Unit,
    onOpenCommunity: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfileEdit: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = {
            FloorTopBar(
                title = "The Floor",
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = FloorTheme.colors.textPrimary,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (val s = state) {
            HomeUiState.Loading -> SkeletonList(rows = 5, modifier = Modifier.padding(padding))
            is HomeUiState.Error -> FloorErrorState(
                message = s.message,
                onRetry = viewModel::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            is HomeUiState.Ready -> {
                Column(modifier = Modifier.padding(padding)) {
                    if (s.offline) OfflineBanner()
                    HomeContentList(
                        content = s.content,
                        greeting = viewModel.greeting(),
                        onOpenInvite = onOpenInvite,
                        onOpenRewards = onOpenRewards,
                        onOpenFloorTab = onOpenFloorTab,
                        onOpenCommunity = onOpenCommunity,
                        onOpenPost = onOpenPost,
                        onOpenProfileEdit = onOpenProfileEdit,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContentList(
    content: HomeContent,
    greeting: String,
    onOpenInvite: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenFloorTab: () -> Unit,
    onOpenCommunity: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfileEdit: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = FloorTheme.spacing.gutter, vertical = 8.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(greeting, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${content.presenceCount} people on The Floor right now",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.teal,
                )
            }
        }

        // Progressive profile completion — server-driven cards.
        items(content.completionCards, key = { it.title }) { card ->
            FloorCard(onClick = onOpenProfileEdit) {
                Text(card.title, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(card.subtitle, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
            }
        }

        // Invite & Earn — always one tap from Home.
        item {
            FloorAccentCard(onClick = onOpenInvite) {
                Text("Invite & Earn", style = FloorTheme.typography.title, color = FloorTheme.colors.amber)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Bring your floor with you. Free to refer.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    FloorStat(value = content.invitedCount.toString(), label = "Invited")
                    FloorStat(value = content.activeReferrals.toString(), label = "Active", emphasized = true)
                }
            }
        }

        item {
            FloorCard(onClick = onOpenRewards) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Floor Rewards", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                        Text("Earn as you participate", style = FloorTheme.typography.caption, color = FloorTheme.colors.textSecondary)
                    }
                    FloorStat(value = content.creditsBalance.toString(), label = "Credits")
                }
            }
        }

        if (content.myFloors.isNotEmpty()) {
            item {
                Text("Your Floors", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(content.myFloors, key = { it.id }) { community ->
                        FloorCard(
                            onClick = { onOpenCommunity(community.id) },
                            modifier = Modifier.width(220.dp),
                        ) {
                            Text(community.name, style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("${community.memberCount} members", style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
                        }
                    }
                }
            }
        } else {
            item {
                FloorCard(onClick = onOpenFloorTab) {
                    Text("Find your Floor", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Communities by country, city, industry, role and shift.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                }
            }
        }

        if (content.trendingPosts.isNotEmpty()) {
            item {
                Text("Trending on Talk", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
            }
            items(content.trendingPosts, key = { it.id }) { post ->
                FloorCard(onClick = { onOpenPost(post.id) }) {
                    Text(post.authorName, style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        post.body,
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textPrimary,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
