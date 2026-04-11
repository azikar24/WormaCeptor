package com.azikar24.wormaceptor.feature.pushtoken.vm

/** User-initiated events for the Push Token management screen. */
sealed class PushTokenViewEvent {
    /** Fetch the current push token from the messaging provider. */
    data object FetchToken : PushTokenViewEvent()

    /** Request a new push token, invalidating the current one. */
    data object RefreshToken : PushTokenViewEvent()

    /** Delete the current push token. */
    data object DeleteToken : PushTokenViewEvent()

    /** Copy the current token to the clipboard. */
    data object CopyToken : PushTokenViewEvent()

    /** Clear all token history entries. */
    data object ClearHistory : PushTokenViewEvent()

    /** Dismiss the current error message. */
    data object DismissError : PushTokenViewEvent()
}
