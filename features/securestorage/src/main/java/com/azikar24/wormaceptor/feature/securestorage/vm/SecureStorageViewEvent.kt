package com.azikar24.wormaceptor.feature.securestorage.vm

import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType

/** User actions dispatched from the Secure Storage UI. */
sealed class SecureStorageViewEvent {
    data class SelectType(val type: StorageType?) : SecureStorageViewEvent()
    data class UpdateSearchQuery(val query: String) : SecureStorageViewEvent()
    data class SelectEntry(val entry: SecureStorageEntry) : SecureStorageViewEvent()
    data object DismissDetail : SecureStorageViewEvent()
    data object Refresh : SecureStorageViewEvent()
}
