package com.thefloor.app.feature.talk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.TalkRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorHero
import com.thefloor.app.core.designsystem.components.FloorLiveRoomCard
import com.thefloor.app.core.designsystem.components.FloorPillButton
import com.thefloor.app.core.designsystem.components.FloorSectionHeader
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.designsystem.components.OfflineBanner
import com.thefloor.app.core.designsystem.components.SkeletonList
import com.thefloor.app.core.model.Post
import com.thefloor.app.core.model.TalkCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TalkFeedUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val categories: List<TalkCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val posts: List<Post> = emptyList(),
    val nextCursor: String? = null,
    val loadingMore: Boolean = false,
    val offline: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class TalkFeedViewModel @Inject constructor(
    private val talkRepository: TalkRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TalkFeedUiState())
    val state: StateFlow<TalkFeedUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            talkRepository.categories().onSuccess { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
        refresh()
    }

    fun selectCategory(categoryId: String?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(loading = it.posts.isEmpty(), refreshing = it.posts.isNotEmpty(), error = null) }
        viewModelScope.launch {
            talkRepository.feed(_state.value.selectedCategoryId, cursor = null)
                .onSuccess { (posts, cursor) ->
                    _state.update { it.copy(loading = false, refreshing = false, posts = posts, nextCursor = cursor, offline = false) }
                }
                .onError { error ->
                    if (error is AppError.Network) {
                        val cached = talkRepository.cachedFeed.firstOrNull().orEmpty()
                        if (cached.isNotEmpty()) {
                            _state.update { it.copy(loading = false, refreshing = false, posts = cached, offline = true) }
                            return@onError
                        }
                    }
                    _state.update { it.copy(loading = false, refreshing = false, error = error.userMessage) }
                }
        }
    }

    fun loadMore() {
        val s = _state.value
        val cursor = s.nextCursor ?: return
        if (s.loadingMore) return
        _state.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            talkRepository.feed(s.selectedCategoryId, cursor)
                .onSuccess { (posts, next) ->
                    _state.update {
                        it.copy(loadingMore = false, posts = it.posts + posts, nextCursor = next)
                    }
                }
                .onError { _state.update { it.copy(loadingMore = false) } }
        }
    }
}

private data class LiveRoom(val topic: String, val desc: String, val online: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val liveRooms = listOf(
    LiveRoom("Love & dating while WFH", "Swipe stories, long-distance shifts, and dating when your schedule is upside down.", 42, Icons.Filled.FavoriteBorder),
    LiveRoom("Music on shift", "What's in your headset right now? Playlists, new finds, and Floor Radio requests.", 67, Icons.Filled.Headphones),
    LiveRoom("Movies & shows", "Whatever you're bingeing between calls or after the night shift.", 38, Icons.Filled.Movie),
    LiveRoom("Gaming lounge", "Mobile, console, PC — squad up or just talk trash.", 54, Icons.Filled.SportsEsports),
    LiveRoom("Mental health check-in", "A calmer room for stress, burnout and supporting each other. Moderated with care.", 29, Icons.Filled.HealthAndSafety),
    LiveRoom("Off-topic hangout", "Nothing to do with work. Just people, talking.", 81, Icons.Filled.ChatBubbleOutline),
)

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun TalkFeedScreen(
    onOpenPost: (String) -> Unit,
    onCompose: () -> Unit,
    viewModel: TalkFeedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Talk") },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCompose,
                containerColor = FloorTheme.colors.amber,
                contentColor = FloorTheme.colors.onAmber,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "New post")
            }
        },
    ) { padding ->
        TalkBody(
            state = state,
            modifier = Modifier.padding(padding),
            onOpenPost = onOpenPost,
            onCompose = onCompose,
            onSelectCategory = viewModel::selectCategory,
            onLoadMore = viewModel::loadMore,
            onRefresh = viewModel::refresh,
        )
    }
}

/** Stateless Talk body — internal so the screenshot suite can render it. */
@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
internal fun TalkBody(
    state: TalkFeedUiState,
    modifier: Modifier = Modifier,
    onOpenPost: (String) -> Unit = {},
    onCompose: () -> Unit = {},
    onSelectCategory: (String?) -> Unit = {},
    onLoadMore: () -> Unit = {},
    onRefresh: () -> Unit = {},
) {
        Column(modifier = modifier) {
            if (state.offline) OfflineBanner()
            androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = onRefresh,
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        FloorHero(
                            eyebrow = "Talk · Discussions that matter",
                            title = "The conversations our industry needs to have.",
                            subtitle = "Talk follows subjects, not status updates. Raise a point, hear different BPO perspectives, and turn frontline experience into useful industry insight.",
                            actions = {
                                FloorPillButton("Start a discussion", onClick = onCompose)
                            },
                        )
                    }
                    // category chips
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FloorChip(text = "All Discussions", selected = state.selectedCategoryId == null, onClick = { onSelectCategory(null) })
                            }
                            items(state.categories, key = { it.id }) { category ->
                                FloorChip(text = category.name, selected = state.selectedCategoryId == category.id, onClick = { onSelectCategory(category.id) })
                            }
                        }
                    }

                    when {
                        state.loading -> item { SkeletonList(rows = 4) }
                        state.error != null -> item { FloorErrorState(message = state.error!!, onRetry = onRefresh) }
                        state.posts.isEmpty() -> item {
                            FloorEmptyState(
                                title = "Start the first conversation",
                                message = "Nobody has posted here yet. Be the one who breaks the silence.",
                                actionText = "Write a post",
                                onAction = onCompose,
                            )
                        }
                        else -> {
                            items(state.posts, key = { it.id }) { post ->
                                PostCard(post = post, onClick = { onOpenPost(post.id) })
                            }
                            if (state.nextCursor != null) {
                                item {
                                    androidx.compose.runtime.LaunchedEffect(state.nextCursor) { onLoadMore() }
                                    Text("Loading more…", style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }

                    // ---- Live Rooms ----
                    item {
                        Spacer(Modifier.height(6.dp))
                        FloorSectionHeader(
                            title = "Live Rooms",
                            subtitle = "Drop into a live topic room — BPO talk or just a hangout.",
                        )
                    }
                    items(liveRooms, key = { it.topic }) { room ->
                        FloorLiveRoomCard(
                            icon = room.icon,
                            topic = room.topic,
                            desc = room.desc,
                            onlineText = "${room.online} online now",
                            onClick = onCompose,
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
}
