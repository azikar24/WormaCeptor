/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents an entry in secure storage (EncryptedSharedPreferences, KeyStore, or DataStore).
 *
 * @property key The key/alias identifying this entry
 * @property value The value stored (may be masked for sensitive data)
 * @property storageType The type of secure storage this entry belongs to
 * @property isEncrypted Whether this entry is encrypted
 * @property lastModified Timestamp of last modification (if available)
 */
data class SecureStorageEntry(
    val key: String,
    val value: String,
    val storageType: StorageType,
    val isEncrypted: Boolean,
    val lastModified: Long?,
) {
    enum class StorageType {
        ENCRYPTED_SHARED_PREFS,
        KEYSTORE,
        DATASTORE,
    }

    companion object {
        fun empty() = SecureStorageEntry("", "", StorageType.ENCRYPTED_SHARED_PREFS, false, null)
    }
}

/**
 * Summary statistics for secure storage entries.
 *
 * @property encryptedPrefsCount Number of entries in EncryptedSharedPreferences
 * @property keystoreAliasCount Number of aliases in KeyStore
 * @property dataStoreFileCount Number of DataStore files found
 */
data class SecureStorageSummary(
    val encryptedPrefsCount: Int,
    val keystoreAliasCount: Int,
    val dataStoreFileCount: Int,
) {
    val totalCount: Int
        get() = encryptedPrefsCount + keystoreAliasCount + dataStoreFileCount

    companion object {
        fun empty() = SecureStorageSummary(0, 0, 0)
    }
}
