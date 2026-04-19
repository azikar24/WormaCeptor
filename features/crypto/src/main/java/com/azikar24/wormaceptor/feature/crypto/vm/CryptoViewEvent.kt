package com.azikar24.wormaceptor.feature.crypto.vm

import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme

/** User-initiated events in the Crypto feature. */
sealed class CryptoViewEvent {

    /** Events related to algorithm and key configuration. */
    sealed class Config : CryptoViewEvent() {

        /** Apply a predefined [preset] configuration. */
        data class ApplyPreset(val preset: CryptoPreset) : Config()

        /** Select a crypto [algorithm]. */
        data class SetAlgorithm(val algorithm: CryptoAlgorithm) : Config()

        /** Select a cipher [mode]. */
        data class SetMode(val mode: CipherMode) : Config()

        /** Select a [padding] scheme. */
        data class SetPadding(val padding: PaddingScheme) : Config()

        /** Select a key [format]. */
        data class SetKeyFormat(val format: KeyFormat) : Config()

        /** Update the encryption [key]. */
        data class SetKey(val key: String) : Config()

        /** Update the initialization vector ([iv]). */
        data class SetIv(val iv: String) : Config()

        /** Generate a random key for the current algorithm. */
        data object GenerateKey : Config()

        /** Generate a random IV for the current mode. */
        data object GenerateIv : Config()
    }

    /** Events related to user input text. */
    sealed class Input : CryptoViewEvent() {

        /** Update the plaintext/ciphertext input [text]. */
        data class UpdateText(val text: String) : Input()
    }

    /** Events that trigger crypto operations. */
    sealed class Operation : CryptoViewEvent() {

        /** Encrypt the current input. */
        data object Encrypt : Operation()

        /** Decrypt the current input. */
        data object Decrypt : Operation()
    }

    /** Events related to the operation result. */
    sealed class Result : CryptoViewEvent() {

        /** Copy [text] to clipboard. */
        data class Copy(val text: String) : Result()

        /** Clear the current result. */
        data object Clear : Result()

        /** Feed the result [text] back as input. */
        data class UseAsInput(val text: String) : Result()
    }

    /** Events related to operation history management. */
    sealed class History : CryptoViewEvent() {

        /** Request confirmation before clearing all history. */
        data object RequestClearAll : History()

        /** Confirmed: clear all history entries. */
        data object ConfirmClearAll : History()

        /** Dismiss the clear-all confirmation dialog. */
        data object DismissClearConfirmation : History()

        /** Remove a single history entry by [id]. */
        data class Remove(val id: String) : History()

        /** Load a historical [result] into the tool. */
        data class Load(val result: CryptoResult) : History()
    }

    sealed class Navigation : CryptoViewEvent() {
        data object ShowHistory : Navigation()
        data object HideHistory : Navigation()
        data object BackPressed : Navigation()
    }
}
