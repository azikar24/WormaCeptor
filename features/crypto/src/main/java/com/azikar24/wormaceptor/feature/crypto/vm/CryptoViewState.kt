package com.azikar24.wormaceptor.feature.crypto.vm

import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** UI state for the Crypto tool screen. */
data class CryptoViewState(
    /** Current algorithm/mode/key configuration. */
    val config: CryptoConfig = CryptoConfig.default(),
    /** User-entered plaintext or ciphertext. */
    val inputText: String = "",
    /** Most recent encryption/decryption result, if any. */
    val currentResult: CryptoResult? = null,
    /** List of past operation results. */
    val history: ImmutableList<CryptoResult> = persistentListOf(),
    /** Whether a crypto operation is in progress. */
    val isProcessing: Boolean = false,
    /** Error message from the last failed operation, if any. */
    val error: String? = null,
    /** Whether the history screen is currently shown. */
    val showHistory: Boolean = false,
    /** Whether the clear-all-history confirmation dialog is visible. */
    val showClearHistoryConfirmation: Boolean = false,
)
