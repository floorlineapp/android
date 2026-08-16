package com.thefloor.app.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.domain.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordUiState(
    val input: String = "",
    val error: String? = null,
    val submitting: Boolean = false,
    val done: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class PasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val forgotState = MutableStateFlow(PasswordUiState())
    val resetState = MutableStateFlow(PasswordUiState())

    fun onForgotEmail(value: String) = forgotState.update { it.copy(input = value, error = null) }
    fun onNewPassword(value: String) = resetState.update { it.copy(input = value, error = null) }

    fun submitForgot() {
        val email = forgotState.value.input
        Validators.email(email)?.let { err ->
            forgotState.update { it.copy(error = err) }
            return
        }
        forgotState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            authRepository.forgotPassword(email)
            // Always confirm — never reveal whether an account exists.
            forgotState.update {
                it.copy(submitting = false, done = true, message = "If that address has an account, a reset link is on its way.")
            }
        }
    }

    fun submitReset(token: String) {
        val password = resetState.value.input
        Validators.password(password)?.let { err ->
            resetState.update { it.copy(error = err) }
            return
        }
        resetState.update { it.copy(submitting = true) }
        viewModelScope.launch {
            authRepository.resetPassword(token, password)
                .onSuccess { resetState.update { it.copy(submitting = false, done = true) } }
                .onError { error -> resetState.update { it.copy(submitting = false, error = error.userMessage) } }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: PasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.forgotState.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Reset password", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            Spacer(Modifier.height(16.dp))
            if (state.done) {
                Text(state.message.orEmpty(), style = FloorTheme.typography.bodyL, color = FloorTheme.colors.textPrimary)
            } else {
                Text(
                    "Enter your email and we'll send a reset link.",
                    style = FloorTheme.typography.body,
                    color = FloorTheme.colors.textSecondary,
                )
                Spacer(Modifier.height(16.dp))
                FloorTextField(
                    value = state.input,
                    onValueChange = viewModel::onForgotEmail,
                    label = "Email",
                    error = state.error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(Modifier.height(24.dp))
                FloorPrimaryButton(
                    text = "Send reset link",
                    onClick = viewModel::submitForgot,
                    loading = state.submitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun ResetPasswordScreen(
    token: String,
    onDone: () -> Unit,
    viewModel: PasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.resetState.collectAsStateWithLifecycle()
    if (state.done) {
        androidx.compose.runtime.LaunchedEffect(Unit) { onDone() }
    }
    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Choose a new password") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            Spacer(Modifier.height(16.dp))
            FloorTextField(
                value = state.input,
                onValueChange = viewModel::onNewPassword,
                label = "New password",
                error = state.error,
                supporting = "10+ characters with a number or symbol",
                isPassword = true,
            )
            Spacer(Modifier.height(24.dp))
            FloorPrimaryButton(
                text = "Set new password",
                onClick = { viewModel.submitReset(token) },
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
