package com.thefloor.app.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.analytics.AnalyticsTracker
import com.thefloor.app.core.analytics.Events
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.AuthRepository
import com.thefloor.app.core.data.ConfigRepository
import com.thefloor.app.core.datastore.ReferralStore
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.BadgeTone
import com.thefloor.app.core.designsystem.components.FloorBadge
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorTextButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.domain.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val inviteCode: String = "",
    val inviterName: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val submitting: Boolean = false,
    val generalError: String? = null,
    val done: Boolean = false,
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val referralStore: ReferralStore,
    private val configRepository: ConfigRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    init {
        analytics.track(Events.SIGNUP_STARTED)
        // Deep-link code (nav arg) or previously captured pending code → inviter chip.
        val navCode: String? = savedStateHandle.get<String>("code")
        viewModelScope.launch {
            val code = navCode ?: referralStore.pending()?.first
            if (code != null && Validators.referralCodeFormat(code)) {
                if (navCode != null) referralStore.save(navCode, "DEEP_LINK")
                _state.update { it.copy(inviteCode = code) }
                configRepository.resolveReferral(code).onSuccess { (valid, name) ->
                    if (valid) _state.update { it.copy(inviterName = name) }
                }
            }
        }
    }

    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, generalError = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, generalError = null) }
    fun onName(value: String) = _state.update { it.copy(displayName = value, nameError = null, generalError = null) }

    fun onInviteCode(value: String) {
        _state.update { it.copy(inviteCode = value.uppercase(), inviterName = null) }
        viewModelScope.launch {
            if (Validators.referralCodeFormat(value)) {
                referralStore.save(value, "MANUAL_CODE")
                configRepository.resolveReferral(value).onSuccess { (valid, name) ->
                    if (valid) _state.update { it.copy(inviterName = name) }
                }
            }
        }
    }

    fun dismissInviter() {
        _state.update { it.copy(inviterName = null, inviteCode = "") }
        viewModelScope.launch { referralStore.clear() }
    }

    fun submit() {
        val s = _state.value
        val emailError = Validators.email(s.email)
        val passwordError = Validators.password(s.password)
        val nameError = Validators.displayName(s.displayName)
        if (emailError != null || passwordError != null || nameError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError, nameError = nameError) }
            return
        }
        _state.update { it.copy(submitting = true, generalError = null) }
        viewModelScope.launch {
            authRepository.signUp(s.email, s.password, s.displayName)
                .onSuccess {
                    analytics.track(Events.SIGNUP_COMPLETED, mapOf("hadReferral" to (s.inviteCode.isNotBlank()).toString()))
                    _state.update { it.copy(submitting = false, done = true) }
                }
                .onError { error ->
                    _state.update { it.copy(submitting = false, generalError = error.userMessage) }
                }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onLogIn: () -> Unit,
    onBack: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.done) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onSignedUp() }
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Join The Floor", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            if (state.inviterName != null) {
                FloorBadge(
                    text = "Invited by ${state.inviterName} — tap to remove",
                    tone = BadgeTone.TEAL,
                    modifier = Modifier.padding(vertical = 8.dp)
                        .let { m ->
                            m.then(
                                Modifier
                                    .fillMaxWidth()
                            )
                        },
                )
                FloorTextButton(text = "Not invited? Remove", onClick = viewModel::dismissInviter)
            }
            Spacer(Modifier.height(8.dp))
            FloorTextField(
                value = state.displayName,
                onValueChange = viewModel::onName,
                label = "Display name",
                error = state.nameError,
                supporting = "How other members see you",
            )
            Spacer(Modifier.height(12.dp))
            FloorTextField(
                value = state.email,
                onValueChange = viewModel::onEmail,
                label = "Email",
                error = state.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(12.dp))
            FloorTextField(
                value = state.password,
                onValueChange = viewModel::onPassword,
                label = "Password",
                error = state.passwordError,
                supporting = "10+ characters with a number or symbol",
                isPassword = true,
            )
            if (state.inviterName == null) {
                Spacer(Modifier.height(12.dp))
                FloorTextField(
                    value = state.inviteCode,
                    onValueChange = viewModel::onInviteCode,
                    label = "Invite code (optional)",
                    supporting = "Have a friend on The Floor? Add their code.",
                )
            }
            if (state.generalError != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.generalError!!, style = FloorTheme.typography.body, color = FloorTheme.colors.coral)
            }
            Spacer(Modifier.height(24.dp))
            FloorPrimaryButton(
                text = "Create free account",
                onClick = viewModel::submit,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            FloorTextButton(text = "Already a member? Log in", onClick = onLogIn)
            Spacer(Modifier.height(16.dp))
            Text(
                "Joining is free. By continuing you accept the Terms and Privacy Policy.",
                style = FloorTheme.typography.caption,
                color = FloorTheme.colors.textMuted,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
