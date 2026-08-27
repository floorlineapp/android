package com.thefloor.app.feature.pulse

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.TimeAgo
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.PulseRepository
import com.thefloor.app.core.datastore.SessionStore
import com.thefloor.app.core.designsystem.FloorMotion
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorLoading
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.Pulse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PulseUiState(
    val loading: Boolean = true,
    val pulses: List<Pulse> = emptyList(),
    val input: String = "",
    val posting: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
    val myUserId: String? = null,
)

@HiltViewModel
class PulseViewModel @Inject constructor(
    private val pulseRepository: PulseRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _state = MutableStateFlow(PulseUiState())
    val state: StateFlow<PulseUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch { _state.update { it.copy(myUserId = sessionStore.current()?.userId) } }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            pulseRepository.feed()
                .onSuccess { (pulses, _) -> _state.update { it.copy(loading = false, pulses = pulses, error = null) } }
                .onError { e -> _state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun onInput(value: String) = _state.update { it.copy(input = value) }

    fun post() {
        val body = _state.value.input.trim()
        if (body.isEmpty() || _state.value.posting) return
        _state.update { it.copy(posting = true) }
        viewModelScope.launch {
            pulseRepository.create(body)
                .onSuccess { pulse ->
                    _state.update { it.copy(posting = false, input = "", pulses = listOf(pulse) + it.pulses) }
                }
                .onError { e -> _state.update { it.copy(posting = false, actionMessage = e.userMessage) } }
        }
    }

    fun toggleLike(pulse: Pulse) {
        // Optimistic — the server call reconciles on failure.
        val nowLiked = !pulse.liked
        _state.update { s ->
            s.copy(pulses = s.pulses.map {
                if (it.id == pulse.id) it.copy(liked = nowLiked, likeCount = (it.likeCount + if (nowLiked) 1 else -1).coerceAtLeast(0)) else it
            })
        }
        viewModelScope.launch {
            val result = if (nowLiked) pulseRepository.like(pulse.id) else pulseRepository.unlike(pulse.id)
            result.onError { e ->
                _state.update { s ->
                    s.copy(
                        actionMessage = e.userMessage,
                        pulses = s.pulses.map { if (it.id == pulse.id) pulse else it },
                    )
                }
            }
        }
    }

    fun delete(pulse: Pulse) {
        viewModelScope.launch {
            pulseRepository.delete(pulse.id)
                .onSuccess { _state.update { s -> s.copy(pulses = s.pulses.filterNot { it.id == pulse.id }, actionMessage = "Pulse deleted") } }
                .onError { e -> _state.update { it.copy(actionMessage = e.userMessage) } }
        }
    }

    fun clearActionMessage() = _state.update { it.copy(actionMessage = null) }
}

@Composable
fun PulseScreen(
    onBack: () -> Unit,
    viewModel: PulseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Pulse", onBack = onBack) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().imePadding()) {
            when {
                state.loading -> FloorLoading(Modifier.weight(1f))
                state.error != null -> FloorErrorState(
                    message = state.error!!,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.weight(1f).fillMaxSize(),
                )
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(FloorTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.pulses.isEmpty()) {
                        item {
                            Text(
                                "No pulses yet — be the first to say what's happening on your floor.",
                                style = FloorTheme.typography.body,
                                color = FloorTheme.colors.textSecondary,
                            )
                        }
                    }
                    items(state.pulses, key = { it.id }) { pulse ->
                        PulseCard(
                            pulse = pulse,
                            isMine = pulse.authorId == state.myUserId,
                            onLike = { viewModel.toggleLike(pulse) },
                            onDelete = { viewModel.delete(pulse) },
                        )
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
                FloorTextField(
                    value = state.input,
                    onValueChange = viewModel::onInput,
                    label = "What's happening?",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                FloorPrimaryButton(
                    text = "Post",
                    onClick = viewModel::post,
                    loading = state.posting,
                    enabled = state.input.isNotBlank(),
                )
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

@Composable
private fun PulseCard(
    pulse: Pulse,
    isMine: Boolean,
    onLike: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FloorTheme.colors.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulseAvatar(pulse.authorName)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(pulse.authorName, style = FloorTheme.typography.label, color = FloorTheme.colors.textPrimary)
                Text(TimeAgo.format(pulse.createdAt), style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted)
            }
            Spacer(Modifier.weight(1f))
            if (isMine) {
                Text(
                    "Delete",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.coral,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onDelete)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(pulse.body, style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary)
        Spacer(Modifier.height(10.dp))
        val view = LocalView.current
        val heartScale by animateFloatAsState(
            targetValue = if (pulse.liked) 1.18f else 1f,
            animationSpec = FloorMotion.bouncy,
            label = "heartScale",
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    onLike()
                },
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    if (pulse.liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (pulse.liked) "Unlike" else "Like",
                    tint = if (pulse.liked) FloorTheme.colors.coral else FloorTheme.colors.textMuted,
                    modifier = Modifier.size(20.dp).scale(heartScale),
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                pulse.likeCount.toString(),
                style = FloorTheme.typography.caption,
                color = FloorTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun PulseAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(FloorTheme.colors.amber),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            name.trim().firstOrNull()?.uppercase() ?: "?",
            style = FloorTheme.typography.label,
            color = FloorTheme.colors.onAmber,
            textAlign = TextAlign.Center,
        )
    }
}
