package com.thefloor.app.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // Password fields get a show/hide eye so people can sense-check what they typed.
    var revealed by remember { mutableStateOf(false) }
    val hidden = isPassword && !revealed
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
        visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { revealed = !revealed }) {
                    Icon(
                        imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (revealed) "Hide password" else "Show password",
                        tint = FloorTheme.colors.textMuted,
                    )
                }
            }
        } else {
            null
        },
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
