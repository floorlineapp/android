package com.thefloor.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** The app follows the phone by default: someone whose device is in dark mode gets dark, someone in light… */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore("floor_theme")

/** Persists the user's appearance choice. */
@Singleton
class ThemeStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyMode = stringPreferencesKey("theme_mode")

    val mode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[keyMode]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun setMode(mode: ThemeMode) {
        context.themeDataStore.edit { it[keyMode] = mode.name }
    }
}
