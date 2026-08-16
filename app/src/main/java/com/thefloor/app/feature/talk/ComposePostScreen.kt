package com.thefloor.app.feature.talk

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.Events
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.TalkRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.TalkCategory
import com.thefloor.app.domain.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ComposeUiState(
    val categories: List<TalkCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val body: String = "",
    val bodyError: String? = null,
    val submitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class ComposePostViewModel @Inject constructor(
    private val talkRepository: TalkRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(ComposeUiState())
    val state: StateFlow<ComposeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            talkRepository.categories().onSuccess { cats ->
                _state.update { it.copy(categories = cats) }
            }
        }
    }

    fun onBody(value: String) = _state.update { it.copy(body = value, bodyError = null) }
    fun onCategory(id: String) = _state.update { it.copy(selectedCategoryId = id) }

    fun submit() {
        val s = _state.value
        val bodyError = Validators.postBody(s.body)
        val categoryError = if (s.selectedCategoryId == null) "Pick a category" else null
        if (bodyError != null || categoryError != null) {
            _state.update { it.copy(bodyError = bodyError, error = categoryError) }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            talkRepository.createPost(s.selectedCategoryId!!, s.body.trim(), communityId = null)
                .onSuccess {
                    analytics.track(Events.POST_CREATED)
                    _state.update { it.copy(submitting = false, done = true) }
                }
                .onError { error -> _state.update { it.copy(submitting = false, error = error.userMessage) } }
        }
    }
}

@Composable
fun ComposePostScreen(
    onDone: () -> Unit,
    viewModel: ComposePostViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.done) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onDone() }
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "New post", onBack = onDone) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Category", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categories, key = { it.id }) { category ->
                    FloorChip(
                        text = category.name,
                        selected = state.selectedCategoryId == category.id,
                        onClick = { viewModel.onCategory(category.id) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            FloorTextField(
                value = state.body,
                onValueChange = viewModel::onBody,
                label = "What's happening on your floor?",
                error = state.bodyError,
                singleLine = false,
                minLines = 6,
            )
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error!!, style = FloorTheme.typography.body, color = FloorTheme.colors.coral)
            }
            Spacer(Modifier.height(16.dp))
            FloorPrimaryButton(
                text = "Post to The Floor",
                onClick = viewModel::submit,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
