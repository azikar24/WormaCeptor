/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.content.Context
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.KeyStore

/**
 * Engine that scans and exposes secure storage entries from the app.
 *
 * Supports:
 * - EncryptedSharedPreferences files
 * - Android KeyStore aliases (metadata only, never exposes private keys)
 * - DataStore files
 *
 * SECURITY NOTE: This engine is intended for debugging purposes only.
 * It displays non-sensitive metadata and should never be included in production builds.
 */
class SecureStorageEngine(
    private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // All storage entries
    private val _entries = MutableStateFlow<List<SecureStorageEntry>>(emptyList())
    val entries: StateFlow<List<SecureStorageEntry>> = _entries.asStateFlow()

    // Summary statistics
    private val _summary = MutableStateFlow(SecureStorageSummary.empty())
    val summary: StateFlow<SecureStorageSummary> = _summary.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refresh()
    }

    /**
     * Refreshes all secure storage entries.
     */
    fun refresh() {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val allEntries = mutableListOf<SecureStorageEntry>()

                // Scan EncryptedSharedPreferences
                val encryptedPrefsEntries = scanEncryptedSharedPreferences()
                allEntries.addAll(encryptedPrefsEntries)

                // Scan KeyStore
                val keystoreEntries = scanKeyStore()
                allEntries.addAll(keystoreEntries)

                // Scan DataStore
                val dataStoreEntries = scanDataStore()
                allEntries.addAll(dataStoreEntries)

                _entries.value = allEntries.sortedBy { it.key.lowercase() }
                _summary.value = SecureStorageSummary(
                    encryptedPrefsCount = encryptedPrefsEntries.size,
                    keystoreAliasCount = keystoreEntries.size,
                    dataStoreFileCount = dataStoreEntries.size,
                )
            } catch (e: Exception) {
                _error.value = "Failed to scan secure storage: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Gets entries filtered by storage type.
     */
    fun getEntriesByType(type: StorageType): List<SecureStorageEntry> {
        return _entries.value.filter { it.storageType == type }
    }

    /**
     * Searches entries by key or value.
     */
    fun searchEntries(query: String): List<SecureStorageEntry> {
        if (query.isBlank()) return _entries.value

        val lowercaseQuery = query.lowercase()
        return _entries.value.filter { entry ->
            entry.key.lowercase().contains(lowercaseQuery) ||
                entry.value.lowercase().contains(lowercaseQuery)
        }
    }

    /**
     * Scans for EncryptedSharedPreferences files.
     * These are typically named with "__androidx_security_crypto_encrypted_prefs__" prefix.
     */
    private fun scanEncryptedSharedPreferences(): List<SecureStorageEntry> {
        val entries = mutableListOf<SecureStorageEntry>()
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")

        if (!prefsDir.exists() || !prefsDir.isDirectory) {
            return entries
        }

        try {
            prefsDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".xml")) {
                    val prefsName = file.name.removeSuffix(".xml")

                    // Check if this looks like an encrypted prefs file
                    val isEncrypted = prefsName.contains("encrypted", ignoreCase = true) ||
                        prefsName.contains("crypto", ignoreCase = true) ||
                        prefsName.contains("secure", ignoreCase = true)

                    try {
                        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                        val allEntries = prefs.all

                        allEntries.forEach { (key, value) ->
                            entries.add(
                                SecureStorageEntry(
                                    key = "$prefsName/$key",
                                    value = maskSensitiveValue(value?.toString() ?: "null", isEncrypted),
                                    storageType = StorageType.ENCRYPTED_SHARED_PREFS,
                                    isEncrypted = isEncrypted,
                                    lastModified = file.lastModified(),
                                ),
                            )
                        }
                    } catch (e: Exception) {
                        // Skip files that cannot be read as SharedPreferences
                        entries.add(
                            SecureStorageEntry(
                                key = prefsName,
                                value = "[Unable to read: ${e.message}]",
                                storageType = StorageType.ENCRYPTED_SHARED_PREFS,
                                isEncrypted = true,
                                lastModified = file.lastModified(),
                            ),
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission denied
        }

        return entries
    }

    /**
     * Scans the Android KeyStore for aliases.
     * Only exposes metadata - never exposes private keys.
     */
    private fun scanKeyStore(): List<SecureStorageEntry> {
        val entries = mutableListOf<SecureStorageEntry>()

        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                val alias = aliases.nextElement()

                try {
                    val entry = keyStore.getEntry(alias, null)
                    val keyInfo = buildKeyInfo(keyStore, alias)

                    entries.add(
                        SecureStorageEntry(
                            key = alias,
                            value = keyInfo,
                            storageType = StorageType.KEYSTORE,
                            isEncrypted = true, // KeyStore entries are always secure
                            lastModified = keyStore.getCreationDate(alias)?.time,
                        ),
                    )
                } catch (e: Exception) {
                    entries.add(
                        SecureStorageEntry(
                            key = alias,
                            value = "[KeyStore entry - ${e.javaClass.simpleName}]",
                            storageType = StorageType.KEYSTORE,
                            isEncrypted = true,
                            lastModified = null,
                        ),
                    )
                }
            }
        } catch (e: Exception) {
            // KeyStore not available or error accessing it
        }

        return entries
    }

    /**
     * Builds a safe info string about a KeyStore entry without exposing sensitive data.
     */
    private fun buildKeyInfo(keyStore: KeyStore, alias: String): String {
        val info = StringBuilder()

        try {
            val certificate = keyStore.getCertificate(alias)
            if (certificate != null) {
                info.append("Type: Certificate")
                info.append("\nAlgorithm: ${certificate.publicKey?.algorithm ?: "Unknown"}")
            }

            if (keyStore.isKeyEntry(alias)) {
                if (info.isNotEmpty()) info.append("\n")
                info.append("Type: Key Entry")

                // Try to get key info without exposing the key itself
                try {
                    val key = keyStore.getKey(alias, null)
                    key?.let {
                        info.append("\nKey Algorithm: ${it.algorithm}")
                        info.append("\nKey Format: ${it.format ?: "Hardware-backed"}")
                    }
                } catch (e: Exception) {
                    info.append("\n[Key protected]")
                }
            }

            val creationDate = keyStore.getCreationDate(alias)
            creationDate?.let {
                info.append(
                    "\nCreated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(it)}",
                )
            }
        } catch (e: Exception) {
            info.append("[KeyStore alias]")
        }

        return info.toString().ifEmpty { "[KeyStore entry]" }
    }

    /**
     * Scans for DataStore files in the app's data directory.
     */
    private fun scanDataStore(): List<SecureStorageEntry> {
        val entries = mutableListOf<SecureStorageEntry>()
        val dataStoreDir = File(context.filesDir, "datastore")

        if (!dataStoreDir.exists() || !dataStoreDir.isDirectory) {
            return entries
        }

        try {
            scanDataStoreDirectory(dataStoreDir, entries)
        } catch (e: SecurityException) {
            // Permission denied
        }

        return entries
    }

    /**
     * Recursively scans a directory for DataStore files.
     */
    private fun scanDataStoreDirectory(directory: File, entries: MutableList<SecureStorageEntry>) {
        directory.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                scanDataStoreDirectory(file, entries)
            } else if (file.isFile) {
                val isPreferencesDataStore = file.name.endsWith(".preferences_pb")
                val isProtoDataStore = file.name.endsWith(".pb")

                if (isPreferencesDataStore || isProtoDataStore) {
                    val relativePath = file.absolutePath
                        .removePrefix(context.filesDir.absolutePath)
                        .trimStart('/')

                    entries.add(
                        SecureStorageEntry(
                            key = relativePath,
                            value = buildDataStoreInfo(file, isPreferencesDataStore),
                            storageType = StorageType.DATASTORE,
                            isEncrypted = false, // Standard DataStore is not encrypted by default
                            lastModified = file.lastModified(),
                        ),
                    )
                }
            }
        }
    }

    /**
     * Builds info about a DataStore file.
     */
    private fun buildDataStoreInfo(file: File, isPreferences: Boolean): String {
        val info = StringBuilder()
        info.append("Type: ${if (isPreferences) "Preferences DataStore" else "Proto DataStore"}")
        info.append("\nSize: ${formatFileSize(file.length())}")
        info.append("\nPath: ${file.name}")
        return info.toString()
    }

    /**
     * Masks potentially sensitive values for display.
     */
    private fun maskSensitiveValue(value: String, isEncrypted: Boolean): String {
        if (value.length <= 4) return value

        // Check for common sensitive patterns
        val sensitivePatterns = listOf(
            "password",
            "secret",
            "token",
            "key",
            "auth",
            "credential",
            "api_key",
        )

        val lowerValue = value.lowercase()
        val isSensitive = sensitivePatterns.any { pattern ->
            lowerValue.contains(pattern)
        }

        // For encrypted prefs or sensitive-looking values, show partial mask
        return if (isEncrypted || isSensitive) {
            if (value.length > 8) {
                "${value.take(4)}${"*".repeat(minOf(value.length - 8, 20))}${value.takeLast(4)}"
            } else {
                "${value.first()}${"*".repeat(value.length - 2)}${value.last()}"
            }
        } else {
            value
        }
    }

    /**
     * Formats file size to human-readable string.
     */
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
            bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
            else -> "$bytes B"
        }
    }

    companion object {
        private const val TAG = "SecureStorageEngine"
    }
}
