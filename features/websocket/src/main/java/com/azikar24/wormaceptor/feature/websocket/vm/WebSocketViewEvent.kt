package com.azikar24.wormaceptor.feature.websocket.vm

import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection

/**
 * User-initiated events for the WebSocket Monitoring feature.
 */
sealed class WebSocketViewEvent {

    /**
     * Updates the connection list search filter.
     *
     * @property query The new search text.
     */
    data class ConnectionSearchQueryChanged(val query: String) : WebSocketViewEvent()

    /**
     * Selects a connection to view its messages.
     *
     * @property connectionId The ID of the connection to select.
     */
    data class ConnectionSelected(val connectionId: Long) : WebSocketViewEvent()

    /** Clears the current connection selection and resets message state. */
    data object ConnectionSelectionCleared : WebSocketViewEvent()

    /**
     * Updates the message search filter.
     *
     * @property query The new search text.
     */
    data class MessageSearchQueryChanged(val query: String) : WebSocketViewEvent()

    /**
     * Toggles the direction filter. If the same direction is already active, it clears the filter.
     *
     * @property direction The direction to toggle.
     */
    data class DirectionFilterToggled(val direction: WebSocketMessageDirection) : WebSocketViewEvent()

    /**
     * Toggles the expanded state of a message payload.
     *
     * @property messageId The ID of the message to toggle.
     */
    data class MessageExpandToggled(val messageId: Long) : WebSocketViewEvent()

    /** Clears all connections and messages via the engine. */
    data object ClearAll : WebSocketViewEvent()

    /** Clears messages for the currently selected connection. */
    data object ClearCurrentConnectionMessages : WebSocketViewEvent()
}
