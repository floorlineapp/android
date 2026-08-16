package com.thefloor.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.CommunityRepository
import com.thefloor.app.core.data.UserRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.designsystem.components.FloorTextButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.CareerLevel
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.network.UpdateProfileRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Progressive onboarding — three light steps, all skippable.
 * Skipped steps resurface as Home completion cards, never as blockers.
 */
data class OnboardingUiState(
    val step: Int = 0, // 0 country, 1 role & level, 2 floors
    val country: String = "",
    val role: String = "",
    val careerLevel: CareerLevel? = null,
    val suggested: List<Community> = emptyList(),
    val joined: Set<String> = emptySet(),
    val saving: Boolean = false,
    val finished: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onCountry(value: String) = _state.update { it.copy(country = value.uppercase().take(2)) }
    fun onRole(value: String) = _state.update { it.copy(role = value) }
    fun onLevel(level: CareerLevel) = _state.update { it.copy(careerLevel = level) }

    fun nextFromCountry(skip: Boolean) {
        if (!skip && _state.value.country.length == 2) {
            saveProfile(UpdateProfileRequestDto(country = _state.value.country))
        }
        _state.update { it.copy(step = 1) }
    }

    fun nextFromRole(skip: Boolean) {
        val s = _state.value
        if (!skip && (s.role.isNotBlank() || s.careerLevel != null)) {
            saveProfile(
                UpdateProfileRequestDto(
                    role = s.role.ifBlank { null },
                    careerLevel = s.careerLevel?.name,
                )
            )
        }
        _state.update { it.copy(step = 2) }
        loadSuggestions()
    }

    fun toggleJoin(community: Community) {
        val id = community.id
        viewModelScope.launch {
            if (id in _state.value.joined) {
                communityRepository.leave(id)
                _state.update { it.copy(joined = it.joined - id) }
            } else {
                communityRepository.join(id).onSuccess {
                    _state.update { it.copy(joined = it.joined + id) }
                }
            }
        }
    }

    fun finish() = _state.update { it.copy(finished = true) }

    private fun loadSuggestions() {
        viewModelScope.launch {
            communityRepository.suggested().onSuccess { list ->
                _state.update { it.copy(suggested = list) }
            }
        }
    }

    private fun saveProfile(update: UpdateProfileRequestDto) {
        viewModelScope.launch { userRepository.updateProfile(update) }
    }
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.finished) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onFinished() }
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Set up your Floor") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            Spacer(Modifier.height(8.dp))
            FloorProgressBar(progress = (state.step + 1) / 3f, label = "Step ${state.step + 1} of 3")
            Spacer(Modifier.height(24.dp))

            when (state.step) {
                0 -> {
                    Text("Where do you work from?", style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Unlocks local Floors and relevant jobs later.", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    Spacer(Modifier.height(16.dp))
                    FloorTextField(
                        value = state.country,
                        onValueChange = viewModel::onCountry,
                        label = "Country code",
                        supporting = "Two letters — PH, ZA, RO…",
                    )
                    Spacer(Modifier.height(24.dp))
                    FloorPrimaryButton(
                        text = "Continue",
                        onClick = { viewModel.nextFromCountry(skip = false) },
                        enabled = state.country.length == 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FloorTextButton(text = "Skip for now", onClick = { viewModel.nextFromCountry(skip = true) })
                }
                1 -> {
                    Text("What do you do on the floor?", style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                    Spacer(Modifier.height(16.dp))
                    FloorTextField(value = state.role, onValueChange = viewModel::onRole, label = "Role (e.g. Customer Support)")
                    Spacer(Modifier.height(16.dp))
                    Text("Career level", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(CareerLevel.AGENT, CareerLevel.SENIOR_AGENT, CareerLevel.TEAM_LEADER).forEach { level ->
                            FloorChip(
                                text = level.label,
                                selected = state.careerLevel == level,
                                onClick = { viewModel.onLevel(level) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(CareerLevel.SME, CareerLevel.SUPERVISOR, CareerLevel.MANAGER).forEach { level ->
                            FloorChip(
                                text = level.label,
                                selected = state.careerLevel == level,
                                onClick = { viewModel.onLevel(level) },
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    FloorPrimaryButton(
                        text = "Continue",
                        onClick = { viewModel.nextFromRole(skip = false) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FloorTextButton(text = "Skip for now", onClick = { viewModel.nextFromRole(skip = true) })
                }
                else -> {
                    Text("Find your Floor", style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("Join at least one community to get the full experience.", style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.suggested, key = { it.id }) { community ->
                            FloorCard(onClick = { viewModel.toggleJoin(community) }) {
                                Text(community.name, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (community.id in state.joined) "Joined ✓" else "${community.memberCount} members — tap to join",
                                    style = FloorTheme.typography.caption,
                                    color = if (community.id in state.joined) FloorTheme.colors.teal else FloorTheme.colors.textSecondary,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    FloorPrimaryButton(
                        text = "Step onto The Floor",
                        onClick = viewModel::finish,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FloorTextButton(text = "Skip for now", onClick = viewModel::finish)
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
