package com.azikar24.wormaceptor.feature.preferences.data

import android.content.SharedPreferences
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Flow-based implementation of PreferencesRepository.
 * Uses polling for file list changes and SharedPreferences listeners for item changes.
 */
class PreferencesRepositoryImpl(
    private val dataSource: PreferencesDataSource,
) : PreferencesRepository {

    private val clearedFiles = MutableSharedFlow<String>()

    override fun observePreferenceFiles(): Flow<List<PreferenceFile>> = flow {
        while (true) {
            emit(dataSource.getPreferenceFiles())
            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)

    override fun observePreferenceItems(fileName: String): Flow<List<PreferenceItem>> = callbackFlow {
        // Subscribe before the initial emission so a clear right after it is not missed.
        launch(start = CoroutineStart.UNDISPATCHED) {
            clearedFiles.filter { it == fileName }.collect {
                trySend(dataSource.getPreferenceItems(fileName))
            }
        }

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

    override suspend fun getPreference(
        fileName: String,
        key: String,
    ): PreferenceValue? = withContext(Dispatchers.IO) {
        dataSource.getPreference(fileName, key)
    }

    override suspend fun setPreference(
        fileName: String,
        key: String,
        value: PreferenceValue,
    ) = withContext(Dispatchers.IO) {
        dataSource.setPreference(fileName, key, value)
    }

    override suspend fun deletePreference(
        fileName: String,
        key: String,
    ) = withContext(Dispatchers.IO) {
        dataSource.deletePreference(fileName, key)
    }

    override suspend fun clearFile(fileName: String) {
        withContext(Dispatchers.IO) {
            dataSource.clearFile(fileName)
        }
        // Below API 30, clear() does not notify change listeners, so observers would keep stale items.
        clearedFiles.emit(fileName)
    }

    /** Polling interval for preference file changes. */
    companion object {
        private const val POLL_INTERVAL_MS = 2000L
    }
}
