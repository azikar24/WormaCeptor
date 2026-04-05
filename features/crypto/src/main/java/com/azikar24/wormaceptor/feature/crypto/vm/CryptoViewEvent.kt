package com.azikar24.wormaceptor.feature.crypto.vm

import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme

sealed class CryptoViewEvent {

    sealed class Config : CryptoViewEvent() {
        data class ApplyPreset(val preset: CryptoPreset) : Config()
        data class SetAlgorithm(val algorithm: CryptoAlgorithm) : Config()
        data class SetMode(val mode: CipherMode) : Config()
        data class SetPadding(val padding: PaddingScheme) : Config()
        data class SetKeyFormat(val format: KeyFormat) : Config()
        data class SetKey(val key: String) : Config()
        data class SetIv(val iv: String) : Config()
        data object GenerateKey : Config()
        data object GenerateIv : Config()
    }

    sealed class Input : CryptoViewEvent() {
        data class UpdateText(val text: String) : Input()
    }

    sealed class Operation : CryptoViewEvent() {
        data object Encrypt : Operation()
        data object Decrypt : Operation()
    }

    sealed class Result : CryptoViewEvent() {
        data class Copy(val text: String) : Result()
        data object Clear : Result()
        data class UseAsInput(val text: String) : Result()
    }

    sealed class History : CryptoViewEvent() {
        data object ClearAll : History()
        data class Remove(val id: String) : History()
        data class Load(val result: CryptoResult) : History()
    }
}
