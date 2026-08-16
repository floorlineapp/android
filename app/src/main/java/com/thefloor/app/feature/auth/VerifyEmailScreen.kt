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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyUiState(
    val resendCooldownSeconds: Int = 0,
    val verified: Boolean = false,
    val checking: Boolean = false,
    val info: String? = null,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyUiState())
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()

    init {
        // Poll every 5s while this screen is alive — verification usually happens
        // in the mail app, and this brings the user forward without a manual step.
        viewModelScope.launch {
            while (!_state.value.verified) {
                checkStatus()
                delay(5_000)
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
            authRepository.resendVerification()
            _state.update { it.copy(resendCooldownSeconds = 60, info = "Verification email sent.") }
            while (_state.value.resendCooldownSeconds > 0) {
                delay(1_000)
                _state.update { it.copy(resendCooldownSeconds = it.resendCooldownSeconds - 1) }
            }
        }
    }
}

@Composable
fun VerifyEmailScreen(
    onVerified: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.verified) { if (state.verified) onVerified() }

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
            Spacer(Modifier.height(32.dp))
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
                enabled = state.resendCooldownSeconds == 0,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
