package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.thefloor.app.core.designsystem.FloorTheme

@Composable
fun FloorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    supporting: String? = null,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = FloorTheme.typography.label) },
        isError = error != null,
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        supportingText = when {
            error != null -> ({ Text(error, style = FloorTheme.typography.caption, color = FloorTheme.colors.coral) })
            supporting != null -> ({ Text(supporting, style = FloorTheme.typography.caption, color = FloorTheme.colors.textMuted) })
            else -> null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FloorTheme.colors.surfaceAlt,
            unfocusedContainerColor = FloorTheme.colors.surfaceAlt,
            errorContainerColor = FloorTheme.colors.surfaceAlt,
            focusedBorderColor = FloorTheme.colors.amber,
            unfocusedBorderColor = FloorTheme.colors.borderSoft,
            errorBorderColor = FloorTheme.colors.coral,
            focusedTextColor = FloorTheme.colors.textPrimary,
            unfocusedTextColor = FloorTheme.colors.textPrimary,
            focusedLabelColor = FloorTheme.colors.amber,
            unfocusedLabelColor = FloorTheme.colors.textMuted,
            cursorColor = FloorTheme.colors.amber,
        ),
    )
}
