package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the state of a WebSocket connection.
 */
enum class WebSocketState {
    /** Connection handshake is in progress. */
    CONNECTING,

    /** Connection is established and ready for data transfer. */
    OPEN,

    /** Close handshake has been initiated but not yet completed. */
    CLOSING,

    /** Connection has been fully closed. */
    CLOSED,
}

/**
 * Represents a WebSocket connection being monitored.
 *
 * @property id Unique identifier for the connection
 * @property url The WebSocket URL
 * @property state Current state of the connection
 * @property openedAt Timestamp when the connection was opened (null if not yet open)
 * @property closedAt Timestamp when the connection was closed (null if still open)
 * @property closeCode WebSocket close code if connection was closed
 * @property closeReason WebSocket close reason if connection was closed
 */
data class WebSocketConnection(
    val id: Long,
    val url: String,
    val state: WebSocketState,
    val openedAt: Long? = null,
    val closedAt: Long? = null,
    val closeCode: Int? = null,
    val closeReason: String? = null,
) {
    /**
     * Duration in milliseconds that the connection was/has been open.
     * Returns null if the connection hasn't been opened yet.
     */
    val duration: Long?
        get() {
            val start = openedAt ?: return null
            val end = closedAt ?: System.currentTimeMillis()
            return end - start
        }

    /**
     * Whether the connection is currently active (connecting or open).
     */
    val isActive: Boolean
        get() = state == WebSocketState.CONNECTING || state == WebSocketState.OPEN
}
