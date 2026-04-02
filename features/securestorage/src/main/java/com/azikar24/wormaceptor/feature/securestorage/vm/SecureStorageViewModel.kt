package com.azikar24.wormaceptor.feature.securestorage.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the Secure Storage Viewer screen.
 *
 * Consolidates secure storage data from [SecureStorageEngine] into a single
 * [SecureStorageViewState] and exposes user actions via [SecureStorageViewEvent].
 */
class SecureStorageViewModel(
    private val engine: SecureStorageEngine,
) : BaseViewModel<SecureStorageViewState, SecureStorageViewEffect, SecureStorageViewEvent>(
    initialState = SecureStorageViewState(),
) {

    init {
        combine(
            combine(
                engine.entries,
                engine.summary,
                engine.isLoading,
                engine.error,
            ) { entries, summary, isLoading, error ->
                EngineSnapshotA(entries, summary, isLoading, error)
            },
            combine(
                engine.keystoreAccessible,
                engine.encryptedPrefsAccessible,
                engine.lastRefreshTime,
            ) { keystoreAccessible, encryptedPrefsAccessible, lastRefreshTime ->
                EngineSnapshotB(keystoreAccessible, encryptedPrefsAccessible, lastRefreshTime)
            },
        ) { a, b ->
            updateState {
                copy(
                    filteredEntries = filterEntries(a.entries, selectedType, searchQuery),
                    summary = a.summary,
                    isLoading = a.isLoading,
                    error = a.error,
                    keystoreAccessible = b.keystoreAccessible,
                    encryptedPrefsAccessible = b.encryptedPrefsAccessible,
                    lastRefreshTime = b.lastRefreshTime,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: SecureStorageViewEvent) {
        when (event) {
            is SecureStorageViewEvent.SelectType -> updateState {
                copy(
                    selectedType = event.type,
                    filteredEntries = filterEntries(engine.entries.value, event.type, searchQuery),
                )
            }
            is SecureStorageViewEvent.UpdateSearchQuery -> updateState {
                copy(
                    searchQuery = event.query,
                    filteredEntries = filterEntries(engine.entries.value, selectedType, event.query),
                )
            }
            is SecureStorageViewEvent.SelectEntry -> updateState {
                copy(selectedEntry = event.entry)
            }
            is SecureStorageViewEvent.DismissDetail -> updateState {
                copy(selectedEntry = null)
            }
            is SecureStorageViewEvent.Refresh -> engine.refresh()
        }
    }

    private fun filterEntries(
        entries: List<SecureStorageEntry>,
        type: StorageType?,
        query: String,
    ): ImmutableList<SecureStorageEntry> {
        var filtered = entries

        if (type != null) {
            filtered = filtered.filter { it.storageType == type }
        }

        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter { entry ->
                entry.key.lowercase().contains(lowerQuery) ||
                    entry.value.lowercase().contains(lowerQuery)
            }
        }

        return filtered.toImmutableList()
    }
}

private data class EngineSnapshotA(
    val entries: List<SecureStorageEntry>,
    val summary: SecureStorageSummary,
    val isLoading: Boolean,
    val error: String?,
)

private data class EngineSnapshotB(
    val keystoreAccessible: Boolean,
    val encryptedPrefsAccessible: Boolean,
    val lastRefreshTime: Long?,
)
