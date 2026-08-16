package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thefloor.app.core.designsystem.FloorTheme

@Composable
fun FloorPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FloorTheme.spacing.touchTarget),
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = FloorTheme.colors.amber,
            contentColor = FloorTheme.colors.onAmber,
            disabledContainerColor = FloorTheme.colors.amber.copy(alpha = 0.38f),
            disabledContentColor = FloorTheme.colors.onAmber.copy(alpha = 0.55f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = FloorTheme.colors.onAmber,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text, style = FloorTheme.typography.label)
        }
    }
}

@Composable
fun FloorSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FloorTheme.spacing.touchTarget),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, FloorTheme.colors.border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = FloorTheme.colors.textPrimary),
    ) {
        Text(text, style = FloorTheme.typography.label)
    }
}

@Composable
fun FloorTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FloorTheme.spacing.touchTarget),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(contentColor = FloorTheme.colors.amber),
    ) {
        Text(text, style = FloorTheme.typography.label)
    }
}

@Composable
fun FloorDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FloorTheme.spacing.touchTarget),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, FloorTheme.colors.coral),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = FloorTheme.colors.coral),
    ) {
        Text(text, style = FloorTheme.typography.label)
    }
}
