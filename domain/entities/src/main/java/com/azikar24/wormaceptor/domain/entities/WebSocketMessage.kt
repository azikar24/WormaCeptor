package com.azikar24.wormaceptor.domain.entities

/**
 * Type of WebSocket message.
 */
enum class WebSocketMessageType {
    /** UTF-8 text data frame. */
    TEXT,

    /** Binary data frame. */
    BINARY,

    /** Control frame used to check connection liveness. */
    PING,

    /** Control frame sent in response to a ping. */
    PONG,
}

/**
 * Direction of the WebSocket message.
 */
enum class WebSocketMessageDirection {
    /** Message was sent from the client to the server. */
    SENT,

    /** Message was received from the server by the client. */
    RECEIVED,
}

/**
 * Represents a single WebSocket message.
 *
 * @property id Unique identifier for the message
 * @property connectionId ID of the connection this message belongs to
 * @property type Type of message (TEXT, BINARY, PING, PONG)
 * @property direction Direction of message (SENT or RECEIVED)
 * @property payload The message payload content
 * @property timestamp Timestamp when the message was sent/received
 * @property size Size of the message in bytes
 */
data class WebSocketMessage(
    val id: Long,
    val connectionId: Long,
    val type: WebSocketMessageType,
    val direction: WebSocketMessageDirection,
    val payload: String,
    val timestamp: Long,
    val size: Long,
) {
    /**
     * Returns a truncated preview of the payload suitable for list views.
     */
    fun payloadPreview(maxLength: Int = 100): String {
        return if (payload.length <= maxLength) {
            payload
        } else {
            payload.take(maxLength) + "..."
        }
    }
}
