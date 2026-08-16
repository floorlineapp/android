package com.thefloor.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.referralDataStore: DataStore<Preferences> by preferencesDataStore("floor_referral")

/**
 * Holds the pending referral code between click/install and signup.
 * This is a TRANSPORT cache only — attribution truth lives on the server,
 * which also matches click records independently of this value.
 */
@Singleton
class ReferralStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyCode = stringPreferencesKey("pending_code")
    private val keySource = stringPreferencesKey("pending_source")

    suspend fun pending(): Pair<String, String>? {
        val prefs = context.referralDataStore.data.map { it }.first()
        val code = prefs[keyCode] ?: return null
        return code to (prefs[keySource] ?: "MANUAL_CODE")
    }

    /** Install-referrer attribution wins over deep link, which wins over manual entry. */
    suspend fun save(code: String, source: String) {
        val priority = mapOf("INSTALL_REFERRER" to 3, "DEEP_LINK" to 2, "MANUAL_CODE" to 1)
        context.referralDataStore.edit { prefs ->
            val existingPriority = priority[prefs[keySource]] ?: 0
            if ((priority[source] ?: 0) >= existingPriority) {
                prefs[keyCode] = code.trim().uppercase()
                prefs[keySource] = source
            }
        }
    }

    suspend fun clear() {
        context.referralDataStore.edit { it.clear() }
    }
}
