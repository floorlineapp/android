package com.thefloor.app.feature.settings

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.BuildConfig
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.AuthRepository
import com.thefloor.app.core.data.UserRepository
import com.thefloor.app.core.datastore.ThemeMode
import com.thefloor.app.core.datastore.ThemeStore
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorDestructiveButton
import com.thefloor.app.core.designsystem.components.FloorListItem
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val themeStore: ThemeStore,
) : ViewModel() {

    data class DeleteState(
        val password: String = "",
        val submitting: Boolean = false,
        val error: String? = null,
        val done: Boolean = false,
    )

    val deleteState = MutableStateFlow(DeleteState())

    val themeMode: StateFlow<ThemeMode> = themeStore.mode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.LIGHT)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { themeStore.setMode(mode) }
    }

    fun logOut() {
        viewModelScope.launch { authRepository.logOut() }
    }

    fun onDeletePassword(value: String) = deleteState.update { it.copy(password = value, error = null) }

    fun deleteAccount() {
        val password = deleteState.value.password
        if (password.isEmpty()) {
            deleteState.update { it.copy(error = "Enter your password to confirm") }
            return
        }
        deleteState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            userRepository.deleteAccount(password)
                .onSuccess {
                    // Do NOT log out yet — the grace-period explanation must be
                    // readable first; acknowledgeDeletion() completes the logout.
                    deleteState.update { it.copy(submitting = false, done = true) }
                }
                .onError { e -> deleteState.update { it.copy(submitting = false, error = e.userMessage) } }
        }
    }

    /** User has read the grace-period message — now clear the session. */
    fun acknowledgeDeletion() {
        viewModelScope.launch { authRepository.logOut() }
    }
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNotificationPrefs: () -> Unit,
    onPrivacy: () -> Unit,
    onDeleteAccount: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Settings", onBack = onBack) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier.padding(
                    horizontal = FloorTheme.spacing.gutter,
                    vertical = FloorTheme.spacing.s,
                ),
            ) {
                Text(
                    "APPEARANCE",
                    style = FloorTheme.typography.label,
                    color = FloorTheme.colors.textMuted,
                )
                Spacer(Modifier.height(10.dp))
                ThemeModeSelector(selected = themeMode, onSelect = viewModel::setThemeMode)
            }

            FloorListItem(title = "Notification preferences", onClick = onNotificationPrefs)
            FloorListItem(title = "Privacy controls", onClick = onPrivacy)
            FloorListItem(
                title = "Delete account",
                subtitle = "14-day grace period, then permanent",
                onClick = onDeleteAccount,
            )
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = FloorTheme.spacing.gutter)) {
                FloorSecondaryButton(
                    text = "Log out",
                    onClick = viewModel::logOut,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "The Floor v${BuildConfig.VERSION_NAME}",
                    style = FloorTheme.typography.caption,
                    color = FloorTheme.colors.textMuted,
                )
            }
        }
    }
}

/** Segmented Light / Dark / System control, styled from brand tokens. */
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark",
        ThemeMode.SYSTEM to "System",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FloorTheme.colors.surfaceAlt)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (mode, label) ->
            val active = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) FloorTheme.colors.amber else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = FloorTheme.typography.label,
                    color = if (active) FloorTheme.colors.onAmber else FloorTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.deleteState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Delete account", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(FloorTheme.spacing.gutter),
        ) {
            if (state.done) {
                Text(
                    "Your deletion request is in. Your account will be removed after a 14-day grace period — log in before then to cancel.",
                    style = FloorTheme.typography.bodyL,
                    color = FloorTheme.colors.textPrimary,
                )
                Spacer(Modifier.height(24.dp))
                com.thefloor.app.core.designsystem.components.FloorPrimaryButton(
                    text = "Got it — log me out",
                    onClick = viewModel::acknowledgeDeletion,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Text(
                    "This requests permanent deletion of your account and personal data.",
                    style = FloorTheme.typography.bodyL,
                    color = FloorTheme.colors.textPrimary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "There's a 14-day grace period: log back in before it ends to cancel. " +
                        "Earned rewards history is retained in de-identified form where the law requires financial records.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
                Spacer(Modifier.height(24.dp))
                FloorTextField(
                    value = state.password,
                    onValueChange = viewModel::onDeletePassword,
                    label = "Confirm your password",
                    error = state.error,
                    isPassword = true,
                )
                Spacer(Modifier.height(24.dp))
                FloorDestructiveButton(
                    text = if (state.submitting) "Submitting…" else "Request deletion",
                    onClick = viewModel::deleteAccount,
                    enabled = !state.submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
