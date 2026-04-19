package com.azikar24.wormaceptorapp.screens.securestorage

data class KeyStoreEntry(
    val alias: String,
    val algorithm: String,
    val keySize: Int?,
    val creationDate: String?,
)
