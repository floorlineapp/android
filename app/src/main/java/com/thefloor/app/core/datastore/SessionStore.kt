package com.thefloor.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("floor_session")

data class Session(val userId: String, val accessToken: String, val refreshToken: String, val emailVerified: Boolean)

/**
 * Session persistence. Tokens are Keystore-encrypted before hitting DataStore;
 * backup rules additionally exclude the datastore directory.
 */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crypto: CryptoManager,
) {
    private val keyUserId = stringPreferencesKey("user_id")
    private val keyAccess = stringPreferencesKey("access_enc")
    private val keyRefresh = stringPreferencesKey("refresh_enc")
    private val keyVerified = stringPreferencesKey("email_verified")

    val session: Flow<Session?> = context.sessionDataStore.data.map { prefs ->
        val userId = prefs[keyUserId] ?: return@map null
        val access = prefs[keyAccess]?.let(crypto::decrypt) ?: return@map null
        val refresh = prefs[keyRefresh]?.let(crypto::decrypt) ?: return@map null
        Session(userId, access, refresh, prefs[keyVerified] == "true")
    }

    suspend fun current(): Session? = session.first()

    suspend fun save(userId: String, accessToken: String, refreshToken: String, emailVerified: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[keyUserId] = userId
            prefs[keyAccess] = crypto.encrypt(accessToken)
            prefs[keyRefresh] = crypto.encrypt(refreshToken)
            prefs[keyVerified] = emailVerified.toString()
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[keyAccess] = crypto.encrypt(accessToken)
            prefs[keyRefresh] = crypto.encrypt(refreshToken)
        }
    }

    suspend fun markVerified() {
        context.sessionDataStore.edit { it[keyVerified] = "true" }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
