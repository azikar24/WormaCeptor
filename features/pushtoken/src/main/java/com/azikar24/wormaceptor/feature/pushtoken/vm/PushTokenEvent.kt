package com.azikar24.wormaceptor.feature.pushtoken.vm

/** User-initiated events for the Push Token management screen. */
sealed class PushTokenEvent {
    /** Fetch the current push token from the messaging provider. */
    data object FetchToken : PushTokenEvent()

    /** Request a new push token, invalidating the current one. */
    data object RefreshToken : PushTokenEvent()

    /** Delete the current push token. */
    data object DeleteToken : PushTokenEvent()

    /** Copy the current token to the clipboard. */
    data object CopyToken : PushTokenEvent()

    /** Clear all token history entries. */
    data object ClearHistory : PushTokenEvent()

    /** Dismiss the current error message. */
    data object DismissError : PushTokenEvent()
}
