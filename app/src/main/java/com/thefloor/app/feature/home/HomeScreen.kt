package com.thefloor.app.feature.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.thefloor.app.core.common.AppError
import com.thefloor.app.core.common.ShiftGreeting
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.HomeRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorIconChip
import com.thefloor.app.core.designsystem.components.FloorKpiCard
import com.thefloor.app.core.designsystem.components.FloorLiveDot
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTile
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.OfflineBanner
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.HomeContent
import com.thefloor.app.feature.talk.PostCard
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

    val refreshing = MutableStateFlow(false)

    private var lastGood: HomeContent? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
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
            refreshing.value = false
        }
    }

    fun greeting(): String = ShiftGreeting.greetingFor(LocalTime.now())
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun HomeScreen(
    onOpenInvite: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenFloorTab: () -> Unit,
    onOpenTalk: () -> Unit,
    onOpenRadio: () -> Unit,
    onOpenAcademy: () -> Unit,
    onOpenMarketplace: () -> Unit,
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
                title = "Home",
                actions = {
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = FloorTheme.colors.textPrimary)
                    }
                    IconButton(onClick = onOpenNotifications) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = FloorTheme.colors.textPrimary)
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
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            is HomeUiState.Ready -> {
                val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
                androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.padding(padding),
                ) {
                    Column {
                        if (s.offline) OfflineBanner()
                        HomeContentList(
                            content = s.content,
                            greeting = viewModel.greeting(),
                            onOpenInvite = onOpenInvite,
                            onOpenRewards = onOpenRewards,
                            onOpenFloorTab = onOpenFloorTab,
                            onOpenTalk = onOpenTalk,
                            onOpenRadio = onOpenRadio,
                            onOpenAcademy = onOpenAcademy,
                            onOpenMarketplace = onOpenMarketplace,
                            onOpenCommunity = onOpenCommunity,
                            onOpenPost = onOpenPost,
                            onOpenProfileEdit = onOpenProfileEdit,
                        )
                    }
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
    onOpenTalk: () -> Unit,
    onOpenRadio: () -> Unit,
    onOpenAcademy: () -> Unit,
    onOpenMarketplace: () -> Unit,
    onOpenCommunity: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenProfileEdit: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ---- Hero ----
        item {
            FloorHero(
                eyebrow = "The Floor · Global Community",
                title = "$greeting The global home of the people behind every customer conversation.",
                subtitle = "Wherever you work, whatever shift you're on — you're never alone on The Floor.",
                actions = {
                    FloorPillButton("Find your Floor", onClick = onOpenFloorTab, leadingIcon = Icons.Filled.Groups)
                    FloorPillButton("Floor Radio", onClick = onOpenRadio, primary = false, leadingIcon = Icons.Filled.Headphones)
                },
            )
        }

        // ---- KPI row ----
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloorKpiCard(
                    icon = Icons.Filled.Group,
                    value = formatCount(content.presenceCount),
                    label = "people online now",
                    accent = FloorAccent.TEAL,
                    modifier = Modifier.weight(1f),
                )
                FloorKpiCard(
                    icon = Icons.Filled.CardGiftcard,
                    value = formatCount(content.creditsBalance.toInt()),
                    label = "Floor points",
                    accent = FloorAccent.AMBER,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ---- Presence line ----
        item {
            FloorCard(onClick = onOpenFloorTab, contentPadding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorLiveDot()
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${formatCount(content.presenceCount)} people on The Floor right now",
                        style = FloorTheme.typography.titleSm,
                        color = FloorTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    Text("Explore →", style = FloorTheme.typography.label, color = FloorTheme.colors.teal)
                }
            }
        }

        // ---- Profile completion cards ----
        items(content.completionCards, key = { it.title }) { card ->
            FloorCard(onClick = onOpenProfileEdit, contentPadding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorIconChip(Icons.Filled.PersonAddAlt1, FloorAccent.AMBER)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(card.title, style = FloorTheme.typography.titleSm, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(3.dp))
                        Text(card.subtitle, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    }
                }
            }
        }

        // ---- Jump back in ----
        item {
            Spacer(Modifier.height(4.dp))
            FloorSectionHeader(title = "Jump back in", subtitle = "Your shift. Your Floor.")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloorTile(Icons.Filled.Groups, "The Floor", "Your community by country, company and shift.", onOpenFloorTab, Modifier.weight(1f), FloorAccent.AMBER)
                FloorTile(Icons.Filled.Forum, "Talk", "Real people. Real conversations.", onOpenTalk, Modifier.weight(1f), FloorAccent.TEAL)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloorTile(Icons.Filled.Headphones, "Floor Radio", "When words stop, the shift keeps moving.", onOpenRadio, Modifier.weight(1f), FloorAccent.CORAL)
                FloorTile(Icons.Filled.CardGiftcard, "Rewards", "Participate, earn points, unlock perks.", onOpenRewards, Modifier.weight(1f), FloorAccent.AMBER)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloorTile(Icons.Filled.PersonAddAlt1, "Invite & Grow", "Bring good people to The Floor.", onOpenInvite, Modifier.weight(1f), FloorAccent.TEAL)
                FloorTile(Icons.Filled.School, "Academy", "Upskill for the role you want next.", onOpenAcademy, Modifier.weight(1f), FloorAccent.AMBER)
            }
        }

        // ---- Your Floors ----
        if (content.myFloors.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                FloorSectionHeader(title = "Your Floors", linkText = "See all", onLink = onOpenFloorTab)
            }
            items(content.myFloors, key = { "floor-${it.id}" }) { community ->
                FloorCard(onClick = { onOpenCommunity(community.id) }, contentPadding = 16.dp) {
                    FloorEyebrow(community.name, accent = FloorAccent.AMBER)
                    Spacer(Modifier.height(6.dp))
                    Text("${formatCount(community.memberCount)} members", style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
                }
            }
        } else {
            item {
                FloorCard(onClick = onOpenFloorTab, contentPadding = 20.dp) {
                    FloorEyebrow("Find your Floor", accent = FloorAccent.TEAL)
                    Spacer(Modifier.height(8.dp))
                    Text("Communities by country, city, industry, role and shift.", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                }
            }
        }

        // ---- Trending on Talk ----
        if (content.trendingPosts.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                FloorSectionHeader(title = "Trending on Talk", linkText = "See all", onLink = onOpenTalk)
            }
            items(content.trendingPosts, key = { "post-${it.id}" }) { post ->
                PostCard(post = post, onClick = { onOpenPost(post.id) })
            }
        }

        // ---- On Air ----
        item {
            Spacer(Modifier.height(4.dp))
            FloorSectionHeader(title = "On Air now", linkText = "Open Radio", onLink = onOpenRadio)
        }
        item {
            FloorCard(onClick = onOpenRadio, contentPadding = 18.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FloorIconChip(Icons.Filled.Headphones, FloorAccent.CORAL, size = 44.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FloorLiveDot(color = FloorTheme.colors.coral)
                            Spacer(Modifier.width(6.dp))
                            Text("ON AIR · NIGHT SHIFT", style = FloorTheme.typography.monoTag, color = FloorTheme.colors.coral)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Lo-Fi Beats", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                        Text("For the ones awake when everyone else is asleep.", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    FloorPillButton("Play", onClick = onOpenRadio)
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

/** Groups thousands with commas — matches the prototype's "2,347" formatting. */
private fun formatCount(n: Int): String = "%,d".format(n)
