package com.thefloor.app.feature.talk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.TalkRepository
import com.thefloor.app.core.data.UserRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorLoading
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.Comment
import com.thefloor.app.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Participant(val userId: String, val name: String)

data class PostDetailUiState(
    val loading: Boolean = true,
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentInput: String = "",
    val sendingComment: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    /** This device's user id — drives which Delete actions are shown. */
    val myUserId: String? = null,
    /** User ids the author has tagged for the next comment. */
    val pendingMentions: List<Participant> = emptyList(),
    /** Set when the post was deleted so the screen can pop. */
    val postDeleted: Boolean = false,
) {
    /** Distinct people in the thread (post author + commenters), minus me — the tag list. */
    val participants: List<Participant>
        get() {
            val all = buildList {
                post?.let { add(Participant(it.authorId, it.authorName)) }
                comments.forEach { add(Participant(it.authorId, it.authorName)) }
            }
            return all.distinctBy { it.userId }.filter { it.userId != myUserId }
        }
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val talkRepository: TalkRepository,
    private val userRepository: UserRepository,
    private val sessionStore: com.thefloor.app.core.datastore.SessionStore,
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId").orEmpty()

    private val _state = MutableStateFlow(PostDetailUiState())
    val state: StateFlow<PostDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(myUserId = sessionStore.current()?.userId) }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            talkRepository.post(postId)
                .onSuccess { post ->
                    _state.update { it.copy(loading = false, post = post, error = null) }
                    talkRepository.comments(postId).onSuccess { comments ->
                        _state.update { it.copy(comments = comments) }
                    }
                }
                .onError { error -> _state.update { it.copy(loading = false, error = error.userMessage) } }
        }
    }

    fun onCommentInput(value: String) = _state.update { it.copy(commentInput = value) }

    /** Tag a thread participant: records the mention and inserts "@Name " into the draft. */
    fun addMention(p: Participant) = _state.update {
        if (it.pendingMentions.any { m -> m.userId == p.userId }) it
        else it.copy(
            pendingMentions = it.pendingMentions + p,
            commentInput = (it.commentInput.trimEnd() + " @${p.name} ").trimStart(),
        )
    }

    fun clearMentions() = _state.update { it.copy(pendingMentions = emptyList()) }

    fun sendComment() {
        val body = _state.value.commentInput.trim()
        if (body.isEmpty() || _state.value.sendingComment) return
        val mentions = _state.value.pendingMentions.map { it.userId }
        _state.update { it.copy(sendingComment = true) }
        viewModelScope.launch {
            talkRepository.addComment(postId, body, parentId = null, mentionUserIds = mentions)
                .onSuccess { comment ->
                    _state.update {
                        it.copy(
                            sendingComment = false,
                            commentInput = "",
                            pendingMentions = emptyList(),
                            comments = it.comments + comment,
                            post = it.post?.copy(commentCount = it.post!!.commentCount + 1),
                        )
                    }
                }
                .onError { error ->
                    _state.update { it.copy(sendingComment = false, actionMessage = error.userMessage) }
                }
        }
    }

    fun deletePost() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            talkRepository.deletePost(post.id)
                .onSuccess { _state.update { it.copy(postDeleted = true) } }
                .onError { error -> _state.update { it.copy(actionMessage = error.userMessage) } }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            talkRepository.deleteComment(commentId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            comments = it.comments.filterNot { c -> c.id == commentId },
                            post = it.post?.copy(commentCount = (it.post!!.commentCount - 1).coerceAtLeast(0)),
                            actionMessage = "Comment deleted",
                        )
                    }
                }
                .onError { error -> _state.update { it.copy(actionMessage = error.userMessage) } }
        }
    }

    fun toggleReaction() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            if (post.myReaction == null) {
                talkRepository.setReaction(post.id, "RESPECT").onSuccess {
                    _state.update {
                        it.copy(post = post.copy(myReaction = "RESPECT", reactionCount = post.reactionCount + 1))
                    }
                }
            } else {
                talkRepository.clearReaction(post.id).onSuccess {
                    _state.update {
                        it.copy(post = post.copy(myReaction = null, reactionCount = (post.reactionCount - 1).coerceAtLeast(0)))
                    }
                }
            }
        }
    }

    fun toggleSave() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            talkRepository.toggleSave(post.id, !post.saved).onSuccess {
                _state.update { it.copy(post = post.copy(saved = !post.saved)) }
            }
        }
    }

    fun report(reason: String) {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            talkRepository.report("POST", post.id, reason, detail = null)
                .onSuccess { _state.update { it.copy(actionMessage = "Report submitted — thank you.") } }
                .onError { error -> _state.update { it.copy(actionMessage = error.userMessage) } }
        }
    }

    fun blockAuthor() {
        val post = _state.value.post ?: return
        viewModelScope.launch {
            userRepository.blockUser(post.authorId)
                .onSuccess { _state.update { it.copy(actionMessage = "Member blocked. You won't see each other's content.") } }
                .onError { error -> _state.update { it.copy(actionMessage = error.userMessage) } }
        }
    }

    fun clearActionMessage() = _state.update { it.copy(actionMessage = null) }
}

private val reportReasons = listOf(
    "SPAM" to "Spam or scam",
    "HARASSMENT" to "Harassment or bullying",
    "DOXXING" to "Sharing private information",
    "IMPERSONATION" to "Impersonation",
    "HATE" to "Hate or discrimination",
    "OTHER" to "Something else",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var menuOpen by remember { mutableStateOf(false) }
    var reportSheetOpen by remember { mutableStateOf(false) }
    var tagMenuOpen by remember { mutableStateOf(false) }

    // Pop the screen once the post is deleted.
    androidx.compose.runtime.LaunchedEffect(state.postDeleted) {
        if (state.postDeleted) onBack()
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = {
            FloorTopBar(
                title = "Post",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options", tint = FloorTheme.colors.textPrimary)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(if (state.post?.saved == true) "Unsave" else "Save") },
                            onClick = { menuOpen = false; viewModel.toggleSave() },
                        )
                        // Delete only for your own post (server also enforces this).
                        if (state.post != null && state.post!!.authorId == state.myUserId) {
                            DropdownMenuItem(
                                text = { Text("Delete post", color = FloorTheme.colors.coral) },
                                onClick = { menuOpen = false; viewModel.deletePost() },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = { menuOpen = false; reportSheetOpen = true },
                            )
                            DropdownMenuItem(
                                text = { Text("Block member") },
                                onClick = { menuOpen = false; viewModel.blockAuthor() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("View profile") },
                            onClick = {
                                menuOpen = false
                                state.post?.authorId?.let(onOpenProfile)
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading -> FloorLoading(Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!,
                onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.post != null -> {
                val post = state.post!!
                Column(modifier = Modifier.padding(padding).fillMaxSize().imePadding()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item { PostCard(post = post, onClick = {}) }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FloorSecondaryButton(
                                    text = if (post.myReaction != null) "Respected ✓" else "Respect",
                                    onClick = viewModel::toggleReaction,
                                )
                            }
                        }
                        item {
                            Text(
                                "Comments (${state.comments.size})",
                                style = FloorTheme.typography.title,
                                color = FloorTheme.colors.textPrimary,
                            )
                        }
                        if (state.comments.isEmpty()) {
                            item {
                                Text(
                                    "No comments yet — say something helpful.",
                                    style = FloorTheme.typography.body,
                                    color = FloorTheme.colors.textSecondary,
                                )
                            }
                        } else {
                            items(state.comments, key = { it.id }) { comment ->
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(comment.authorName, style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            com.thefloor.app.core.common.TimeAgo.format(comment.createdAt),
                                            style = FloorTheme.typography.caption,
                                            color = FloorTheme.colors.textMuted,
                                        )
                                        Spacer(Modifier.weight(1f))
                                        // Delete your own comment (server also enforces ownership).
                                        if (comment.authorId == state.myUserId) {
                                            Text(
                                                "Delete",
                                                style = FloorTheme.typography.caption,
                                                color = FloorTheme.colors.coral,
                                                modifier = Modifier
                                                    .defaultMinSize(minWidth = 48.dp, minHeight = 32.dp)
                                                    .clickable { viewModel.deleteComment(comment.id) }
                                                    .padding(horizontal = 4.dp, vertical = 6.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(comment.body, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                                }
                            }
                        }
                    }
                    // Composer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = FloorTheme.spacing.gutter, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Tag a thread participant (@mention).
                        androidx.compose.foundation.layout.Box {
                            IconButton(
                                onClick = { tagMenuOpen = true },
                                enabled = state.participants.isNotEmpty(),
                            ) {
                                Icon(
                                    Icons.Filled.AlternateEmail,
                                    contentDescription = "Tag someone",
                                    tint = if (state.participants.isNotEmpty()) FloorTheme.colors.amber else FloorTheme.colors.textMuted,
                                )
                            }
                            DropdownMenu(expanded = tagMenuOpen, onDismissRequest = { tagMenuOpen = false }) {
                                Text(
                                    "Tag someone in the thread",
                                    style = FloorTheme.typography.caption,
                                    color = FloorTheme.colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                                state.participants.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("@${p.name}") },
                                        onClick = { tagMenuOpen = false; viewModel.addMention(p) },
                                    )
                                }
                            }
                        }
                        FloorTextField(
                            value = state.commentInput,
                            onValueChange = viewModel::onCommentInput,
                            label = "Add a comment",
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        FloorPrimaryButton(
                            text = "Send",
                            onClick = viewModel::sendComment,
                            loading = state.sendingComment,
                            enabled = state.commentInput.isNotBlank(),
                        )
                    }
                }
            }
        }

        if (reportSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { reportSheetOpen = false },
                containerColor = FloorTheme.colors.surfaceAlt,
            ) {
                Column(modifier = Modifier.padding(FloorTheme.spacing.gutter)) {
                    Text("Why are you reporting this?", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    reportReasons.forEach { (code, label) ->
                        com.thefloor.app.core.designsystem.components.FloorListItem(
                            title = label,
                            onClick = {
                                reportSheetOpen = false
                                viewModel.report(code)
                            },
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        state.actionMessage?.let { message ->
            androidx.compose.runtime.LaunchedEffect(message) {
                kotlinx.coroutines.delay(2500)
                viewModel.clearActionMessage()
            }
            androidx.compose.material3.Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = FloorTheme.colors.surfaceAlt,
                contentColor = FloorTheme.colors.textPrimary,
            ) { Text(message) }
        }
    }
}
