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
    /** Type of secure storage backend. */
    enum class StorageType {
        /** AndroidX EncryptedSharedPreferences. */
        ENCRYPTED_SHARED_PREFS,

        /** Android KeyStore system for cryptographic keys and certificates. */
        KEYSTORE,

        /** Jetpack DataStore (Proto or Preferences). */
        DATASTORE,
    }

    /** Factory methods for [SecureStorageEntry]. */
    companion object {
        /** Creates an empty entry with default storage type. */
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
    /** Combined count across all secure storage backends. */
    val totalCount: Int
        get() = encryptedPrefsCount + keystoreAliasCount + dataStoreFileCount

    /** Factory methods for [SecureStorageSummary]. */
    companion object {
        /** Creates an empty summary with zero counts. */
        fun empty() = SecureStorageSummary(0, 0, 0)
    }
}
