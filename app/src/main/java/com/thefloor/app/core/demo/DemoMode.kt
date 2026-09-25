package com.thefloor.app.core.demo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.demoDataStore: DataStore<Preferences> by preferencesDataStore("floor_demo")

/** Demo mode lets the app run with no server at all: [DemoInterceptor] answers every API call from canned data. */
@Singleton
class DemoStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val key = booleanPreferencesKey("demo_enabled")

    suspend fun set(enabled: Boolean) {
        DemoMode.enabled = enabled
        context.demoDataStore.edit { it[key] = enabled }
    }

    /** Restores the flag at process start. */
    suspend fun restore() {
        DemoMode.enabled = runCatching {
            context.demoDataStore.data.map { it[key] ?: false }.first()
        }.getOrDefault(false)
    }

    /** Blocking restore for the earliest startup path. */
    fun restoreBlocking() {
        runCatching { runBlocking { restore() } }
    }
}

/** Process-wide switch read by the interceptor. */
object DemoMode {
    @Volatile
    var enabled: Boolean = false

    const val EMAIL = "demo@thefloor.app"
    const val USER_ID = "11111111-1111-4111-8111-111111111111"
}
