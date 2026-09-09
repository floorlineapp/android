package com.thefloor.app.core.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefloor.app.core.datastore.ThemeMode
import com.thefloor.app.core.datastore.ThemeStore
import com.thefloor.app.core.designsystem.FloorTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeToggleViewModel @Inject constructor(
    private val themeStore: ThemeStore,
) : ViewModel() {
    fun set(mode: ThemeMode) {
        viewModelScope.launch { themeStore.setMode(mode) }
    }
}

/**
 * Sun / moon switch, mirroring the prototype's top-bar control. The current
 * state is read straight from the resolved palette, so it stays correct even
 * when the theme is following the system.
 */
@Composable
fun ThemeToggleAction(viewModel: ThemeToggleViewModel = hiltViewModel()) {
    val isLight = FloorTheme.colors.isLight
    IconButton(
        onClick = { viewModel.set(if (isLight) ThemeMode.DARK else ThemeMode.LIGHT) },
    ) {
        Icon(
            imageVector = if (isLight) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = if (isLight) "Switch to dark mode" else "Switch to light mode",
            tint = FloorTheme.colors.textPrimary,
        )
    }
}
