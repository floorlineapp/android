package com.thefloor.app.core.datastore

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.radioDataStore: DataStore<Preferences> by preferencesDataStore("floor_radio")

/** Which Floor Radio region this device listens to. */
@Singleton
class RadioRegionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyRegion = stringPreferencesKey("region_key")

    val regionKey: Flow<String> = context.radioDataStore.data.map { it[keyRegion] ?: "global" }

    suspend fun setRegion(key: String) {
        context.radioDataStore.edit { it[keyRegion] = key }
    }
}
