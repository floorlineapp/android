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
import androidx.compose.runtime.LaunchedEffect
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
import com.thefloor.app.core.designsystem.components.FloorTextButton
import com.thefloor.app.core.designsystem.components.FloorTextField
import com.thefloor.app.core.designsystem.components.FloorTopBar
import com.thefloor.app.domain.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val submitting: Boolean = false,
    val generalError: String? = null,
    /** null until success; then carries email verification status for routing. */
    val loggedInVerified: Boolean? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, generalError = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, generalError = null) }

    fun submit() {
        val s = _state.value
        val emailError = Validators.email(s.email)
        if (emailError != null) {
            _state.update { it.copy(emailError = emailError) }
            return
        }
        _state.update { it.copy(submitting = true, generalError = null) }
        viewModelScope.launch {
            authRepository.logIn(s.email, s.password)
                .onSuccess {
                    // The session store now holds the verification flag; read it once for routing.
                    val current = authRepository.session.firstOrNull()
                    _state.update { it.copy(submitting = false, loggedInVerified = current?.emailVerified ?: true) }
                }
                .onError { error ->
                    _state.update { it.copy(submitting = false, generalError = error.userMessage) }
                }
        }
    }
}

@Composable
fun LoginScreen(
    onLoggedIn: (verified: Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onBack: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.loggedInVerified) {
        state.loggedInVerified?.let(onLoggedIn)
    }

    Scaffold(
        containerColor = FloorTheme.colors.ink,
        topBar = { FloorTopBar(title = "Log in", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = FloorTheme.spacing.gutter),
        ) {
            Spacer(Modifier.height(16.dp))
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
                isPassword = true,
            )
            if (state.generalError != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.generalError!!, style = FloorTheme.typography.body, color = FloorTheme.colors.coral)
            }
            Spacer(Modifier.height(24.dp))
            FloorPrimaryButton(
                text = "Log in",
                onClick = viewModel::submit,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            FloorTextButton(text = "Forgot password?", onClick = onForgotPassword)
        }
    }
}
