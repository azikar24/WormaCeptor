/*
 * Copyright AziKar24 2025.
 */

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

    // Filter state
    private val _selectedType = MutableStateFlow<StorageType?>(null)
    val selectedType: StateFlow<StorageType?> = _selectedType.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected entry for detail view
    private val _selectedEntry = MutableStateFlow<SecureStorageEntry?>(null)
    val selectedEntry: StateFlow<SecureStorageEntry?> = _selectedEntry.asStateFlow()

    // Loading state
    val isLoading: StateFlow<Boolean> = engine.isLoading

    // Error state
    val error: StateFlow<String?> = engine.error

    // Accessibility status
    val keystoreAccessible: StateFlow<Boolean> = engine.keystoreAccessible
    val encryptedPrefsAccessible: StateFlow<Boolean> = engine.encryptedPrefsAccessible
    val lastRefreshTime: StateFlow<Long?> = engine.lastRefreshTime

    // Summary
    val summary: StateFlow<SecureStorageSummary> = engine.summary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SecureStorageSummary.empty(),
        )

    // Filtered entries
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
