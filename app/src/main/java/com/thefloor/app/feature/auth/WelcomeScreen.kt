package com.thefloor.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.data.AuthRepository
import com.thefloor.app.core.demo.DemoMode
import com.thefloor.app.core.demo.DemoStore
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorLogoMark
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DemoEntryViewModel @javax.inject.Inject constructor(
    private val demoStore: DemoStore,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val busy = MutableStateFlow(false)

    /** Turns demo mode on, then signs in — the interceptor answers the call. */
    fun enter(onDone: () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            demoStore.set(true)
            authRepository.logIn(DemoMode.EMAIL, "demo-password")
            busy.value = false
            onDone()
        }
    }
}

@Composable
fun WelcomeScreen(
    onSignUp: () -> Unit,
    onLogIn: () -> Unit,
    onDemo: () -> Unit = {},
    demoBusy: Boolean = false,
) {
    val colors = FloorTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(colors.ink, colors.surface, colors.ink))
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FloorLogoMark(size = 76.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            "THE FLOOR",
            style = FloorTheme.typography.displayL.copy(letterSpacing = 1.5.sp),
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "GLOBAL WORKFORCE ECOSYSTEM",
            style = FloorTheme.typography.eyebrow,
            color = colors.textMuted,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            "The global home of the people behind every customer conversation.",
            style = FloorTheme.typography.bodyL,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Wherever you work, whatever shift you're on — you're never alone on The Floor.",
            style = FloorTheme.typography.body,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        FloorPrimaryButton(text = "Join The Floor", onClick = onSignUp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        FloorSecondaryButton(text = "I already have an account", onClick = onLogIn, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        FloorSecondaryButton(
            text = if (demoBusy) "Opening demo…" else "Continue with demo profile →",
            onClick = onDemo,
            enabled = !demoBusy,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(32.dp))
        Text(
            "Free to join. Free to refer.",
            style = FloorTheme.typography.caption,
            color = colors.teal,
        )
    }
}
