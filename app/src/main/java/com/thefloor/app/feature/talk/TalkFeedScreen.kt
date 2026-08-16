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
import androidx.compose.material.icons.filled.Edit
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
        Column(modifier = Modifier.padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FloorChip(
                        text = "All",
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                    )
                }
                items(state.categories, key = { it.id }) { category ->
                    FloorChip(
                        text = category.name,
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.selectCategory(category.id) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (state.offline) OfflineBanner()

            when {
                state.loading -> SkeletonList(rows = 5)
                state.error != null -> FloorErrorState(message = state.error!!, onRetry = viewModel::refresh)
                state.posts.isEmpty() -> FloorEmptyState(
                    title = "Start the first conversation",
                    message = "Nobody has posted here yet. Be the one who breaks the silence.",
                    actionText = "Write a post",
                    onAction = onCompose,
                )
                else -> androidx.compose.material3.pulltorefresh.PullToRefreshBox(
                    isRefreshing = state.refreshing,
                    onRefresh = viewModel::refresh,
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = FloorTheme.spacing.gutter, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.posts, key = { it.id }) { post ->
                            PostCard(post = post, onClick = { onOpenPost(post.id) })
                        }
                        if (state.nextCursor != null) {
                            item {
                                androidx.compose.runtime.LaunchedEffect(state.nextCursor) { viewModel.loadMore() }
                                Text(
                                    "Loading more…",
                                    style = FloorTheme.typography.caption,
                                    color = FloorTheme.colors.textMuted,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
