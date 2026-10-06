package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable

/** Deliberately has no value: secrets stay on the device. */
@Serializable
internal data class SecureStorageEntryDto(
    val key: String,
    val storageType: String,
    val isEncrypted: Boolean,
    val lastModified: Long?,
)

@Serializable
internal data class SecureStorageSummaryDto(
    val encryptedPrefsCount: Int,
    val keystoreAliasCount: Int,
    val dataStoreFileCount: Int,
    val totalCount: Int,
)
