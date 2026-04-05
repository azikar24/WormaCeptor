package com.azikar24.wormaceptor.feature.crypto.vm

import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CryptoViewState(
    val config: CryptoConfig = CryptoConfig.default(),
    val inputText: String = "",
    val currentResult: CryptoResult? = null,
    val history: ImmutableList<CryptoResult> = persistentListOf(),
    val isProcessing: Boolean = false,
    val error: String? = null,
)
