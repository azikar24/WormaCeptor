package com.azikar24.wormaceptorapp.sampleservice

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Writes files/datastore/demo_settings.preferences_pb. One instance per file, or DataStore throws.
private val Context.demoSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "demo_settings")

object SampleDataStoreService {

    private const val SYNC_INTERVAL_MIN = 15
    private const val TEXT_SCALE = 1.15f
    private const val DOWNLOAD_QUOTA_GB = 12.5

    suspend fun seed(context: Context) {
        context.applicationContext.demoSettingsDataStore.edit {
            it[stringPreferencesKey("language")] = "en-US"
            it[intPreferencesKey("sync_interval_min")] = SYNC_INTERVAL_MIN
            it[longPreferencesKey("last_sync")] = System.currentTimeMillis()
            it[floatPreferencesKey("text_scale")] = TEXT_SCALE
            it[doublePreferencesKey("download_quota_gb")] = DOWNLOAD_QUOTA_GB
            it[booleanPreferencesKey("onboarding_done")] = true
            it[stringSetPreferencesKey("pinned_tools")] = setOf("network", "preferences", "logs")
            it[byteArrayPreferencesKey("device_salt")] = "wormaceptor-demo".encodeToByteArray()
        }
    }
}
