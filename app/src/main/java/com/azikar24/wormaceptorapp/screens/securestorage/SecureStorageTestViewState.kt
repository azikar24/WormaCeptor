package com.azikar24.wormaceptorapp.screens.securestorage

data class SecureStorageTestViewState(
    val encryptedPrefs: List<EncryptedPrefEntry> = emptyList(),
    val keyStoreEntries: List<KeyStoreEntry> = emptyList(),
    val showAddDialog: Boolean = false,
    val isFabExpanded: Boolean = false,
)
