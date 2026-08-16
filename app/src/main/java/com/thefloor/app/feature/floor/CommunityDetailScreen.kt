package com.thefloor.app.feature.floor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.CommunityRepository
import com.thefloor.app.core.data.TalkRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorEmptyState
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorLoading
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.model.MembershipState
import com.thefloor.app.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CommunityDetailUiState {
    data object Loading : CommunityDetailUiState
    data class Ready(val community: Community, val posts: List<Post>) : CommunityDetailUiState
    data class Error(val message: String) : CommunityDetailUiState
}

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository,
    private val talkRepository: TalkRepository,
) : ViewModel() {

    private val communityId: String = savedStateHandle.get<String>("communityId").orEmpty()

    private val _state = MutableStateFlow<CommunityDetailUiState>(CommunityDetailUiState.Loading)
    val state: StateFlow<CommunityDetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            communityRepository.get(communityId)
                .onSuccess { community ->
                    val posts = when (val feed = talkRepository.feed(categoryId = null, cursor = null, communityId = communityId)) {
                        is com.thefloor.app.core.common.AppResult.Success -> feed.data.first
                        else -> emptyList()
                    }
                    _state.update { CommunityDetailUiState.Ready(community, posts) }
                }
                .onError { error -> _state.update { CommunityDetailUiState.Error(error.userMessage) } }
        }
    }

    fun toggleMembership(community: Community) {
        viewModelScope.launch {
            when (community.membershipState) {
                MembershipState.JOINED -> communityRepository.leave(community.id)
                else -> communityRepository.join(community.id)
            }
            refresh()
        }
    }
}

@Composable
fun CommunityDetailScreen(
    communityId: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    viewModel: CommunityDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Floor", onBack = onBack) },
    ) { padding ->
        when (val s = state) {
            CommunityDetailUiState.Loading -> FloorLoading(Modifier.padding(padding))
            is CommunityDetailUiState.Error -> FloorErrorState(
                message = s.message, onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            is CommunityDetailUiState.Ready -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        Text(s.community.name, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${s.community.memberCount} members",
                            style = FloorTheme.typography.caption,
                            color = FloorTheme.colors.teal,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(s.community.description, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            when (s.community.membershipState) {
                                MembershipState.JOINED -> FloorSecondaryButton(
                                    text = "Leave this Floor",
                                    onClick = { viewModel.toggleMembership(s.community) },
                                )
                                MembershipState.PENDING -> FloorSecondaryButton(
                                    text = "Request pending",
                                    onClick = {},
                                    enabled = false,
                                )
                                else -> FloorPrimaryButton(
                                    text = if (s.community.isRestricted) "Request to join" else "Join this Floor",
                                    onClick = { viewModel.toggleMembership(s.community) },
                                )
                            }
                        }
                    }
                }
                item {
                    Text("Discussions", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                }
                if (s.posts.isEmpty()) {
                    item {
                        FloorCard {
                            Text(
                                "No conversations here yet — start the first one from Talk.",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    }
                } else {
                    items(s.posts, key = { it.id }) { post ->
                        FloorCard(onClick = { onOpenPost(post.id) }) {
                            Text(post.authorName, style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                post.body,
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textPrimary,
                                maxLines = 4,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "${post.reactionCount} reactions · ${post.commentCount} comments",
                                style = FloorTheme.typography.caption,
                                color = FloorTheme.colors.textMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}
