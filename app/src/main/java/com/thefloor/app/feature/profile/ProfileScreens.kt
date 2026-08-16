package com.thefloor.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.UserRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorCard
import com.thefloor.app.core.designsystem.components.FloorChip
import com.thefloor.app.core.designsystem.components.FloorErrorState
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorLoading
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorProgressBar
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.core.model.CareerLevel
import com.thefloor.app.core.model.UserProfile
import com.thefloor.app.core.model.WorkMode
import com.thefloor.app.core.network.UpdateProfileRequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val error: String? = null,
    val saving: Boolean = false,
    val savedMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    val state = MutableStateFlow(ProfileUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            userRepository.me()
                .onSuccess { profile -> state.update { it.copy(loading = false, profile = profile, error = null) } }
                .onError { e -> state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun save(update: UpdateProfileRequestDto) {
        state.update { it.copy(saving = true) }
        viewModelScope.launch {
            userRepository.updateProfile(update)
                .onSuccess { profile ->
                    state.update { it.copy(saving = false, profile = profile, savedMessage = "Saved") }
                }
                .onError { e -> state.update { it.copy(saving = false, error = e.userMessage) } }
        }
    }

    fun setVisibility(field: String, level: String) {
        viewModelScope.launch {
            userRepository.updatePrivacy(mapOf(field to level)).onSuccess { profile ->
                state.update { it.copy(profile = profile) }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onPrivacy: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Profile", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> FloorLoading(Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.profile != null -> {
                val profile = state.profile!!
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        FloorCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(profile.displayName, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                                    Spacer(Modifier.height(4.dp))
                                    profile.careerLevel?.let {
                                        FloorBadge(text = it.label, tone = BadgeTone.AMBER)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            FloorProgressBar(
                                progress = profile.completeness / 100f,
                                label = "Profile ${profile.completeness}% complete",
                            )
                        }
                    }
                    item {
                        FloorCard {
                            Text("Work", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                            Spacer(Modifier.height(8.dp))
                            ProfileField("Employer", profile.employer)
                            ProfileField("Role", profile.role)
                            ProfileField("Industry", profile.industry)
                            ProfileField("Work mode", profile.workMode?.label)
                            ProfileField("Country", profile.country)
                            ProfileField("City", profile.city)
                        }
                    }
                    if (profile.skills.isNotEmpty()) {
                        item {
                            FloorCard {
                                Text("Skills", style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    profile.skills.joinToString(" · "),
                                    style = FloorTheme.typography.body,
                                    color = FloorTheme.colors.textSecondary,
                                )
                            }
                        }
                    }
                    item {
                        FloorCard(contentPadding = 0.dp) {
                            FloorListItem(title = "Edit profile", onClick = onEdit)
                            FloorListItem(title = "Privacy controls", subtitle = "Who sees what", onClick = onPrivacy)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = FloorTheme.typography.body, color = FloorTheme.colors.textSecondary)
        Text(value ?: "—", style = FloorTheme.typography.body, color = FloorTheme.colors.textPrimary)
    }
}

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    var displayName by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.displayName.orEmpty())
    }
    var employer by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.employer.orEmpty())
    }
    var role by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.role.orEmpty())
    }
    var industry by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.industry.orEmpty())
    }
    var country by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.country.orEmpty())
    }
    var city by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.city.orEmpty())
    }
    var level by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.careerLevel)
    }
    var workMode by androidx.compose.runtime.remember(profile) {
        androidx.compose.runtime.mutableStateOf(profile?.workMode)
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Edit profile", onBack = onBack) },
    ) { padding ->
        if (profile == null) {
            FloorLoading(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FloorTextField(value = displayName, onValueChange = { displayName = it }, label = "Display name")
                FloorTextField(value = employer, onValueChange = { employer = it }, label = "Employer / BPO")
                FloorTextField(value = role, onValueChange = { role = it }, label = "Role")
                FloorTextField(value = industry, onValueChange = { industry = it }, label = "Industry")
                FloorTextField(value = country, onValueChange = { country = it.uppercase().take(2) }, label = "Country (2-letter code)")
                FloorTextField(value = city, onValueChange = { city = it }, label = "City")

                Text("Career level", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScrollChips()) {
                    CareerLevel.entries.forEach { entry ->
                        FloorChip(text = entry.label, selected = level == entry, onClick = { level = entry })
                    }
                }
                Text("Work mode", style = FloorTheme.typography.label, color = FloorTheme.colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkMode.entries.forEach { entry ->
                        FloorChip(text = entry.label, selected = workMode == entry, onClick = { workMode = entry })
                    }
                }

                if (state.savedMessage != null) {
                    Text(state.savedMessage!!, style = FloorTheme.typography.caption, color = FloorTheme.colors.teal)
                }
                FloorPrimaryButton(
                    text = "Save changes",
                    loading = state.saving,
                    onClick = {
                        viewModel.save(
                            UpdateProfileRequestDto(
                                displayName = displayName.ifBlank { null },
                                employer = employer.ifBlank { null },
                                role = role.ifBlank { null },
                                industry = industry.ifBlank { null },
                                country = country.takeIf { it.length == 2 },
                                city = city.ifBlank { null },
                                careerLevel = level?.name,
                                workMode = workMode?.name,
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private fun Modifier.horizontalScrollChips(): Modifier = this

private val privacyFields = listOf(
    "employer" to "Employer",
    "city" to "City",
    "country" to "Country",
    "role" to "Role",
    "careerLevel" to "Career level",
    "skills" to "Skills",
    "industry" to "Industry",
)

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Privacy controls", onBack = onBack) },
    ) { padding ->
        if (profile == null) {
            FloorLoading(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(FloorTheme.spacing.gutter),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        "Choose who can see each part of your profile. Sensitive fields are private by default.",
                        style = FloorTheme.typography.body,
                        color = FloorTheme.colors.textSecondary,
                    )
                }
                items(privacyFields.size) { index ->
                    val (field, label) = privacyFields[index]
                    val current = profile.visibility[field] ?: "PRIVATE"
                    FloorCard {
                        Text(label, style = FloorTheme.typography.title, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("PUBLIC" to "Public", "MEMBERS" to "Members", "PRIVATE" to "Only me").forEach { (value, text) ->
                                FloorChip(
                                    text = text,
                                    selected = current == value,
                                    onClick = { viewModel.setVisibility(field, value) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(userId) { viewModel.load(userId) }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Member", onBack = onBack) },
    ) { padding ->
        when {
            state.loading -> FloorLoading(Modifier.padding(padding))
            state.error != null -> FloorErrorState(
                message = state.error!!, onRetry = { viewModel.load(userId) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            state.profile != null -> {
                val profile = state.profile!!
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(FloorTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FloorCard {
                        Text(profile.displayName, style = FloorTheme.typography.headline, color = FloorTheme.colors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        profile.careerLevel?.let { FloorBadge(text = it.label, tone = BadgeTone.AMBER) }
                        Spacer(Modifier.height(8.dp))
                        ProfileField("Role", profile.role)
                        ProfileField("Industry", profile.industry)
                        ProfileField("Country", profile.country)
                    }
                    com.thefloor.app.core.designsystem.components.FloorDestructiveButton(
                        text = "Block this member",
                        onClick = { viewModel.block(userId) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    val state = MutableStateFlow(ProfileUiState())

    fun load(userId: String) {
        viewModelScope.launch {
            userRepository.publicProfile(userId)
                .onSuccess { profile -> state.update { it.copy(loading = false, profile = profile, error = null) } }
                .onError { e -> state.update { it.copy(loading = false, error = e.userMessage) } }
        }
    }

    fun block(userId: String) {
        viewModelScope.launch { userRepository.blockUser(userId) }
    }
}
