package com.azikar24.wormaceptor.feature.securestorage.vm

import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SecureStorageViewState(
    val filteredEntries: ImmutableList<SecureStorageEntry> = persistentListOf(),
    val summary: SecureStorageSummary = SecureStorageSummary.empty(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: StorageType? = null,
    val searchQuery: String = "",
    val selectedEntry: SecureStorageEntry? = null,
    val keystoreAccessible: Boolean = false,
    val encryptedPrefsAccessible: Boolean = false,
    val lastRefreshTime: Long? = null,
)
