package com.azikar24.wormaceptor.feature.preferences.data

import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Read-only [PreferencesRepository] over Preferences DataStore files.
 *
 * DataStore has no change listener reachable from outside the app's instance, so files are
 * polled by modification time. Writes are no-ops: the app's DataStore caches its contents in
 * memory and would overwrite or ignore changes made to the file underneath it.
 */
class DataStoreRepositoryImpl(
    private val dataSource: DataStoreDataSource,
) : PreferencesRepository {

    override fun observePreferenceFiles(): Flow<List<PreferenceFile>> = flow {
        while (true) {
            emit(dataSource.getPreferenceFiles())
            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(Dispatchers.IO)

    override fun observePreferenceItems(fileName: String): Flow<List<PreferenceItem>> = flow {
        while (true) {
            emit(dataSource.lastModified(fileName))
            delay(POLL_INTERVAL_MS)
        }
    }.distinctUntilChanged()
        .map { dataSource.getPreferenceItems(fileName) }
        .flowOn(Dispatchers.IO)

    override suspend fun getPreference(
        fileName: String,
        key: String,
    ): PreferenceValue? = withContext(Dispatchers.IO) {
        dataSource.getPreferenceItems(fileName).firstOrNull { it.key == key }?.value
    }

    override suspend fun setPreference(
        fileName: String,
        key: String,
        value: PreferenceValue,
    ) = Unit

    override suspend fun deletePreference(
        fileName: String,
        key: String,
    ) = Unit

    override suspend fun clearFile(fileName: String) = Unit

    /** Polling interval for DataStore file changes. */
    companion object {
        private const val POLL_INTERVAL_MS = 2000L
    }
}
