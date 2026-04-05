package com.azikar24.wormaceptorapp.screens.securestorage

sealed class SecureStorageTestViewEffect {
    data class ShowError(val message: String) : SecureStorageTestViewEffect()
}
