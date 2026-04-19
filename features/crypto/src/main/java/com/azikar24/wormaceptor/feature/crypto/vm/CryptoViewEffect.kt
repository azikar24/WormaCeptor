package com.azikar24.wormaceptor.feature.crypto.vm

/**
 * One-time side-effects emitted by [CryptoViewModel] and consumed by the UI layer.
 */
sealed class CryptoViewEffect {

    /**
     * Copy the given text to the system clipboard.
     *
     * @property text The text to copy.
     */
    data class CopyToClipboard(val text: String) : CryptoViewEffect()

    /** A random encryption key was generated and applied. */
    data object KeyGenerated : CryptoViewEffect()

    /** A random initialization vector was generated and applied. */
    data object IvGenerated : CryptoViewEffect()

    /** The previous output was loaded back as input text. */
    data object OutputLoadedAsInput : CryptoViewEffect()

    /** A history entry was loaded into the tool. */
    data object HistoryLoaded : CryptoViewEffect()

    data object NavigateBack : CryptoViewEffect()
}
