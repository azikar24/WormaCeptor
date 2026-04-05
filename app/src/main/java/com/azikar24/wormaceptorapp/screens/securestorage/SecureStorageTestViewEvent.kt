package com.azikar24.wormaceptorapp.screens.securestorage

sealed class SecureStorageTestViewEvent {
    data object SetupTestData : SecureStorageTestViewEvent()
    data object ShowAddDialog : SecureStorageTestViewEvent()
    data object DismissAddDialog : SecureStorageTestViewEvent()
    data class AddEntry(val key: String, val value: String) : SecureStorageTestViewEvent()
    data class FabExpandedChanged(val expanded: Boolean) : SecureStorageTestViewEvent()
}
