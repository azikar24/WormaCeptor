package com.azikar24.wormaceptor.feature.preferences.data

import android.content.SharedPreferences
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Flow-based implementation of PreferencesRepository.
 * Uses polling for file list changes and SharedPreferences listeners for item changes.
 */
class PreferencesRepositoryImpl(
    private val dataSource: PreferencesDataSource,
) : PreferencesRepository {

    override fun observePreferenceFiles(): Flow<List<PreferenceFile>> = flow {
        while (true) {
            emit(dataSource.getPreferenceFiles())
            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)

    override fun observePreferenceItems(fileName: String): Flow<List<PreferenceItem>> = callbackFlow {
        // Emit initial value
        trySend(dataSource.getPreferenceItems(fileName))

        // Register listener for changes
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(dataSource.getPreferenceItems(fileName))
        }

        val prefs = dataSource.registerChangeListener(fileName, listener)

        awaitClose {
            dataSource.unregisterChangeListener(prefs, listener)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getPreference(fileName: String, key: String): PreferenceValue? = withContext(Dispatchers.IO) {
        dataSource.getPreference(fileName, key)
    }

    override suspend fun setPreference(fileName: String, key: String, value: PreferenceValue) =
        withContext(Dispatchers.IO) {
            dataSource.setPreference(fileName, key, value)
        }

    override suspend fun deletePreference(fileName: String, key: String) = withContext(Dispatchers.IO) {
        dataSource.deletePreference(fileName, key)
    }

    override suspend fun clearFile(fileName: String) = withContext(Dispatchers.IO) {
        dataSource.clearFile(fileName)
    }

    companion object {
        private const val POLL_INTERVAL_MS = 2000L
    }
}
