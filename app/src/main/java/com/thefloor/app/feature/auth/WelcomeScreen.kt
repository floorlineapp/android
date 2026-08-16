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
import com.thefloor.app.core.designsystem.FloorTheme
import com.thefloor.app.core.designsystem.components.FloorPrimaryButton
import com.thefloor.app.core.designsystem.components.FloorSecondaryButton

@Composable
fun WelcomeScreen(
    onSignUp: () -> Unit,
    onLogIn: () -> Unit,
) {
    val colors = FloorTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            // The single sanctioned gradient in the app: the brand hero moment.
            .background(
                Brush.verticalGradient(listOf(colors.ink, colors.surface, colors.ink))
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("THE FLOOR", style = FloorTheme.typography.displayL, color = colors.amber)
        Spacer(Modifier.height(12.dp))
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
        Spacer(Modifier.height(32.dp))
        Text(
            "Free to join. Free to refer.",
            style = FloorTheme.typography.caption,
            color = colors.teal,
        )
    }
}
