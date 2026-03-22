package com.azikar24.wormaceptor.feature.securestorage.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Secure Storage Viewer screen.
 *
 * Provides filtered access to secure storage entries with search
 * and type filtering capabilities.
 */
class SecureStorageViewModel(
    private val engine: SecureStorageEngine,
) : ViewModel() {

    private val _selectedType = MutableStateFlow<StorageType?>(null)

    /** Currently selected storage type filter, or null if showing all types. */
    val selectedType: StateFlow<StorageType?> = _selectedType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    /** Current search query used to filter storage entries. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedEntry = MutableStateFlow<SecureStorageEntry?>(null)

    /** Currently selected entry for the detail view. */
    val selectedEntry: StateFlow<SecureStorageEntry?> = _selectedEntry.asStateFlow()

    /** Whether a loading operation is in progress. */
    val isLoading: StateFlow<Boolean> = engine.isLoading

    /** Current error message, or null if no error. */
    val error: StateFlow<String?> = engine.error

    /** Whether Android Keystore is accessible on this device. */
    val keystoreAccessible: StateFlow<Boolean> = engine.keystoreAccessible

    /** Whether EncryptedSharedPreferences is accessible on this device. */
    val encryptedPrefsAccessible: StateFlow<Boolean> = engine.encryptedPrefsAccessible

    /** Timestamp of the last successful refresh, or null if never refreshed. */
    val lastRefreshTime: StateFlow<Long?> = engine.lastRefreshTime

    /** Aggregated summary of all secure storage entries. */
    val summary: StateFlow<SecureStorageSummary> = engine.summary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SecureStorageSummary.empty(),
        )

    /** Entries filtered by selected type and search query. */
    val filteredEntries: StateFlow<ImmutableList<SecureStorageEntry>> =
        combine(
            engine.entries,
            _selectedType,
            _searchQuery,
        ) { entries, type, query ->
            var filtered = entries

            // Filter by type if selected
            if (type != null) {
                filtered = filtered.filter { it.storageType == type }
            }

            // Filter by search query
            if (query.isNotBlank()) {
                val lowerQuery = query.lowercase()
                filtered = filtered.filter { entry ->
                    entry.key.lowercase().contains(lowerQuery) ||
                        entry.value.lowercase().contains(lowerQuery)
                }
            }

            filtered.toImmutableList()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            persistentListOf(),
        )

    /**
     * Sets the selected storage type filter.
     *
     * @param type The type to filter by, or null to show all
     */
    fun setSelectedType(type: StorageType?) {
        _selectedType.value = type
    }

    /**
     * Sets the search query.
     *
     * @param query The search query
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Selects an entry to show in detail view.
     *
     * @param entry The entry to show details for
     */
    fun selectEntry(entry: SecureStorageEntry) {
        _selectedEntry.value = entry
    }

    /**
     * Dismisses the detail view.
     */
    fun dismissDetail() {
        _selectedEntry.value = null
    }

    /**
     * Refreshes the secure storage entries.
     */
    fun refresh() {
        engine.refresh()
    }
}
