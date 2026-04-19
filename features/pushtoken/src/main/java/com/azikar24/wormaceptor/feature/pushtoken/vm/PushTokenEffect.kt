package com.azikar24.wormaceptor.feature.pushtoken.vm

/** One-time side effects emitted by [PushTokenViewModel] and consumed by the UI. */
sealed class PushTokenEffect {
    /** Instructs the UI to copy [token] to the system clipboard. */
    data class CopyToClipboard(val token: String) : PushTokenEffect()
}
