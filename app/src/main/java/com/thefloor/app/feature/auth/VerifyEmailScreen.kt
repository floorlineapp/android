package com.thefloor.app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.common.onError
import com.thefloor.app.core.common.onSuccess
import com.thefloor.app.core.data.AuthRepository
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton
import com.thefloor.app.core.designsystem.components.FloorTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyUiState(
    val resendCooldownSeconds: Int = 0,
    val verified: Boolean = false,
    val checking: Boolean = false,
    val info: String? = null,
    val error: String? = null,
    /** False when the verify link was opened without a logged-in session. */
    val signedIn: Boolean = true,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyUiState())
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()

    init {
        // Deep-linked verify token (from the emailed link, routed here by the
        // nav host) — consume it server-side. The confirm endpoint is public,
        // so this works even before login.
        savedStateHandle.get<String>("token")?.let { token ->
            viewModelScope.launch {
                authRepository.confirmVerification(token)
                    .onSuccess { _state.update { it.copy(verified = true) } }
                    .onError { e -> _state.update { it.copy(error = e.userMessage) } }
            }
        }
        viewModelScope.launch {
            val session = authRepository.session.firstOrNull()
            _state.update { it.copy(signedIn = session != null) }
            // Auto-verified (beta) sessions are already verified — advance instantly.
            if (session?.emailVerified == true) {
                _state.update { it.copy(verified = true) }
                return@launch
            }
            // Poll only with a session — signed-out polling would just spray
            // 401s at /users/me every five seconds.
            if (session != null) {
                while (!_state.value.verified) {
                    checkStatus()
                    delay(5_000)
                }
            }
        }
    }

    fun checkStatus() {
        if (_state.value.checking) return
        _state.update { it.copy(checking = true) }
        viewModelScope.launch {
            authRepository.refreshVerificationStatus().onSuccess { verified ->
                if (verified) _state.update { it.copy(verified = true) }
            }
            _state.update { it.copy(checking = false) }
        }
    }

    fun resend() {
        if (_state.value.resendCooldownSeconds > 0) return
        viewModelScope.launch {
            // Surface failures honestly — never claim "sent" when it wasn't.
            authRepository.resendVerification()
                .onSuccess {
                    _state.update { it.copy(resendCooldownSeconds = 60, info = "Verification email sent.", error = null) }
                    while (_state.value.resendCooldownSeconds > 0) {
                        delay(1_000)
                        _state.update { it.copy(resendCooldownSeconds = it.resendCooldownSeconds - 1) }
                    }
                }
                .onError { e -> _state.update { it.copy(info = null, error = e.userMessage) } }
        }
    }
}

@Composable
fun VerifyEmailScreen(
    onVerified: () -> Unit,
    onLogIn: () -> Unit = {},
    viewModel: VerifyEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Onboarding requires a session — signed-out verification success shows
    // a log-in prompt instead of dead-ending into authenticated screens.
    LaunchedEffect(state.verified, state.signedIn) {
        if (state.verified && state.signedIn) onVerified()
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Check your email") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "We sent you a verification link",
                style = FloorTheme.typography.headline,
                color = FloorTheme.colors.textPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Open the email and tap the link. This screen moves on automatically once you're verified.",
                style = FloorTheme.typography.body,
                color = FloorTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            if (state.info != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.info!!, style = FloorTheme.typography.caption, color = FloorTheme.colors.teal)
            }
            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error!!, style = FloorTheme.typography.caption, color = FloorTheme.colors.coral)
            }
            Spacer(Modifier.height(32.dp))
            if (state.verified && !state.signedIn) {
                Text(
                    "Email verified ✓",
                    style = FloorTheme.typography.title,
                    color = FloorTheme.colors.teal,
                )
                Spacer(Modifier.height(12.dp))
                FloorPrimaryButton(
                    text = "Log in to continue",
                    onClick = onLogIn,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                FloorPrimaryButton(
                    text = "I've verified — check now",
                    onClick = viewModel::checkStatus,
                    loading = state.checking,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                FloorSecondaryButton(
                    text = if (state.resendCooldownSeconds > 0) {
                        "Resend in ${state.resendCooldownSeconds}s"
                    } else {
                        "Resend email"
                    },
                    onClick = viewModel::resend,
                    enabled = state.signedIn && state.resendCooldownSeconds == 0,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
