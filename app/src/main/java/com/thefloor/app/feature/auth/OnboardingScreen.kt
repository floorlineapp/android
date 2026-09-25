package com.thefloor.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.CommunityRepository
import com.thefloor.app.core.data.UserRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorAccent
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorEyebrow
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.designsystem.components.FloorTextButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.CareerLevel
import com.thefloor.app.core.model.Community
import com.thefloor.app.core.model.WorkMode
import com.thefloor.app.core.network.UpdateProfileRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Steps 3–6 of the six-step sign-up: About You, BPO Profile, Experience and Your Floor. */
private const val TOTAL_STEPS = 6
private const val FIRST_STEP_HERE = 3

val AGE_RANGES = listOf("18–24", "25–34", "35–44", "45–54", "55+")
val CHANNELS = listOf("Voice", "Chat", "Email", "Social", "Back office", "Technical")
val INDUSTRIES = listOf("Telecom", "Retail", "Banking", "Travel", "Healthcare", "Utilities", "Tech", "Insurance")

data class OnboardingUiState(
    /** 3..6, matching the step numbers the member sees. */
    val step: Int = FIRST_STEP_HERE,
    val country: String = "",
    val city: String = "",
    val ageRange: String? = null,
    val languages: String = "",
    val employer: String = "",
    val site: String = "",
    val industry: String? = null,
    val role: String = "",
    val careerLevel: CareerLevel? = null,
    val experienceYears: String = "",
    val channels: Set<String> = emptySet(),
    val workMode: WorkMode? = null,
    val skills: String = "",
    val suggested: List<Community> = emptyList(),
    val joined: Set<String> = emptySet(),
    val finished: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onCountry(v: String) = _state.update { it.copy(country = v.uppercase().take(2)) }
    fun onCity(v: String) = _state.update { it.copy(city = v) }
    fun onAgeRange(v: String) = _state.update { it.copy(ageRange = v) }
    fun onLanguages(v: String) = _state.update { it.copy(languages = v) }
    fun onEmployer(v: String) = _state.update { it.copy(employer = v) }
    fun onSite(v: String) = _state.update { it.copy(site = v) }
    fun onIndustry(v: String) = _state.update { it.copy(industry = v) }
    fun onRole(v: String) = _state.update { it.copy(role = v) }
    fun onLevel(v: CareerLevel) = _state.update { it.copy(careerLevel = v) }
    fun onYears(v: String) = _state.update { it.copy(experienceYears = v.filter(Char::isDigit).take(2)) }
    fun onWorkMode(v: WorkMode) = _state.update { it.copy(workMode = v) }
    fun onSkills(v: String) = _state.update { it.copy(skills = v) }

    fun toggleChannel(c: String) = _state.update {
        it.copy(channels = if (c in it.channels) it.channels - c else it.channels + c)
    }

    /** Writes this step's fields, then advances. */
    fun next(skip: Boolean) {
        val s = _state.value
        if (!skip) {
            when (s.step) {
                3 -> saveProfile(
                    UpdateProfileRequestDto(
                        country = s.country.ifBlank { null },
                        city = s.city.ifBlank { null },
                        ageRange = s.ageRange,
                        languages = s.languages.splitList().ifEmpty { null },
                    ),
                )
                4 -> saveProfile(
                    UpdateProfileRequestDto(
                        employer = s.employer.ifBlank { null },
                        site = s.site.ifBlank { null },
                        industry = s.industry,
                        role = s.role.ifBlank { null },
                    ),
                )
                5 -> saveProfile(
                    UpdateProfileRequestDto(
                        careerLevel = s.careerLevel?.name,
                        experienceYears = s.experienceYears.toIntOrNull(),
                        channels = s.channels.toList().ifEmpty { null },
                        workMode = s.workMode?.name,
                        skills = s.skills.splitList().ifEmpty { null },
                    ),
                )
                else -> Unit
            }
        }
        _state.update { it.copy(step = (it.step + 1).coerceAtMost(TOTAL_STEPS)) }
        if (_state.value.step == TOTAL_STEPS) loadSuggestions()
    }

    fun back() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(FIRST_STEP_HERE)) }

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

    private fun String.splitList(): List<String> =
        split(",").map(String::trim).filter(String::isNotBlank)
}

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.finished) {
        LaunchedEffect(Unit) { onFinished() }
    }

    val title = when (state.step) {
        3 -> "About you"
        4 -> "BPO profile"
        5 -> "Experience"
        else -> "Your Floor"
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = {
            FloorTopBar(
                title = title,
                onBack = if (state.step > FIRST_STEP_HERE) viewModel::back else null,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            Spacer(Modifier.height(8.dp))
            FloorProgressBar(
                progress = state.step / TOTAL_STEPS.toFloat(),
                label = "Step ${state.step} of $TOTAL_STEPS",
            )
            Spacer(Modifier.height(20.dp))

            when (state.step) {
                3 -> StepAboutYou(state, viewModel)
                4 -> StepBpoProfile(state, viewModel)
                5 -> StepExperience(state, viewModel)
                else -> StepYourFloor(state, viewModel)
            }
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Text(title, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
    Spacer(Modifier.height(8.dp))
    Text(subtitle, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
    Spacer(Modifier.height(18.dp))
}

@Composable
private fun StepFooter(onContinue: () -> Unit, onSkip: () -> Unit, enabled: Boolean = true) {
    Spacer(Modifier.height(22.dp))
    FloorPrimaryButton(
        text = "Continue",
        onClick = onContinue,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    )
    FloorTextButton(text = "Skip for now", onClick = onSkip)
    Spacer(Modifier.height(16.dp))
}

/** Multi-select chip row that wraps. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipWrap(
    label: String,
    options: List<String>,
    isSelected: (String) -> Boolean,
    onSelect: (String) -> Unit,
) {
    FloorEyebrow(label, accent = FloorAccent.FAINT)
    Spacer(Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { o ->
            FloorChip(text = o, selected = isSelected(o), onClick = { onSelect(o) })
        }
    }
}

@Composable
private fun StepAboutYou(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        StepHeader(
            "Where are you in the world?",
            "This unlocks your country Floor and the local rooms that go with it.",
        )
        FloorTextField(
            value = state.country,
            onValueChange = vm::onCountry,
            label = "Country code",
            supporting = "Two letters — PH, ZA, CO…",
        )
        Spacer(Modifier.height(14.dp))
        FloorTextField(value = state.city, onValueChange = vm::onCity, label = "City")
        Spacer(Modifier.height(18.dp))
        ChipWrap("Age range", AGE_RANGES, { it == state.ageRange }, vm::onAgeRange)
        Spacer(Modifier.height(18.dp))
        FloorTextField(
            value = state.languages,
            onValueChange = vm::onLanguages,
            label = "Languages",
            supporting = "Comma separated — English, Tagalog, Spanish",
        )
        StepFooter({ vm.next(skip = false) }, { vm.next(skip = true) })
    }
}

@Composable
private fun StepBpoProfile(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        StepHeader(
            "Where do you work?",
            "Your employer is what lets The Floor show you the people you already work alongside — " +
                "and it is what a verified workplace is checked against later.",
        )
        FloorTextField(value = state.employer, onValueChange = vm::onEmployer, label = "Employer / BPO")
        Spacer(Modifier.height(14.dp))
        FloorTextField(
            value = state.site,
            onValueChange = vm::onSite,
            label = "Site",
            supporting = "The building or campus, if you're on site",
        )
        Spacer(Modifier.height(18.dp))
        ChipWrap("Industry you support", INDUSTRIES, { it == state.industry }, vm::onIndustry)
        Spacer(Modifier.height(18.dp))
        FloorTextField(
            value = state.role,
            onValueChange = vm::onRole,
            label = "Role",
            supporting = "e.g. Customer Support Specialist",
        )
        StepFooter({ vm.next(skip = false) }, { vm.next(skip = true) })
    }
}

@Composable
private fun StepExperience(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        StepHeader(
            "How far along are you?",
            "This shapes your Academy passport and the roles Employers & Jobs will eventually match.",
        )
        ChipWrap(
            "Career level",
            CareerLevel.entries.map { it.label },
            { it == state.careerLevel?.label },
            { label -> CareerLevel.entries.firstOrNull { it.label == label }?.let(vm::onLevel) },
        )
        Spacer(Modifier.height(18.dp))
        FloorTextField(
            value = state.experienceYears,
            onValueChange = vm::onYears,
            label = "Years in the industry",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
        )
        Spacer(Modifier.height(18.dp))
        ChipWrap("Channels you handle", CHANNELS, { it in state.channels }, vm::toggleChannel)
        Spacer(Modifier.height(18.dp))
        ChipWrap(
            "Work mode",
            WorkMode.entries.map { it.label },
            { it == state.workMode?.label },
            { label -> WorkMode.entries.firstOrNull { it.label == label }?.let(vm::onWorkMode) },
        )
        Spacer(Modifier.height(18.dp))
        FloorTextField(
            value = state.skills,
            onValueChange = vm::onSkills,
            label = "Skills",
            supporting = "Comma separated — De-escalation, CRM, Coaching",
        )
        StepFooter({ vm.next(skip = false) }, { vm.next(skip = true) })
    }
}

@Composable
private fun ColumnScope.StepYourFloor(state: OnboardingUiState, vm: OnboardingViewModel) {
    StepHeader(
        "Find your people",
        "Join any Floor in one tap. There is no approval step, and you can leave whenever you like.",
    )
    LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.suggested, key = { it.id }) { community ->
            FloorCard(onClick = { vm.toggleJoin(community) }) {
                Text(community.name, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (community.id in state.joined) {
                        "Joined ✓"
                    } else {
                        "%,d members — tap to join".format(community.memberCount)
                    },
                    style = FloorTheme.typography.caption,
                    color = if (community.id in state.joined) FloorTheme.colors.teal else FloorTheme.colors.textSecondary,
                )
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    FloorPrimaryButton(
        text = "Step onto The Floor",
        onClick = vm::finish,
        modifier = Modifier.fillMaxWidth(),
    )
    FloorTextButton(text = "Skip for now", onClick = vm::finish)
    Spacer(Modifier.height(16.dp))
}
