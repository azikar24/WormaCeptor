/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.websocket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.domain.entities.WebSocketState

/**
 * WebSocket-specific colors for consistent visual representation.
 *
 * Color scheme:
 * - CONNECTING: Amber (in progress)
 * - OPEN: Green (active, healthy)
 * - CLOSING: Orange (transitioning)
 * - CLOSED: Gray (inactive)
 *
 * Direction colors:
 * - SENT: Blue (outgoing)
 * - RECEIVED: Green (incoming)
 *
 * Message type colors:
 * - TEXT: Default (most common)
 * - BINARY: Purple (special handling)
 * - PING/PONG: Cyan (control frames)
 */
@Immutable
data class WebSocketColors(
    // Connection state colors
    val connecting: Color,
    val open: Color,
    val closing: Color,
    val closed: Color,
    // Direction colors
    val sent: Color,
    val received: Color,
    // Message type colors
    val textMessage: Color,
    val binaryMessage: Color,
    val pingPong: Color,
    // Backgrounds
    val sentBackground: Color,
    val receivedBackground: Color,
    val connectingBackground: Color,
    val openBackground: Color,
    val closingBackground: Color,
    val closedBackground: Color,
) {
    /**
     * Returns the foreground color for the given connection state.
     */
    fun forState(state: WebSocketState): Color = when (state) {
        WebSocketState.CONNECTING -> connecting
        WebSocketState.OPEN -> open
        WebSocketState.CLOSING -> closing
        WebSocketState.CLOSED -> closed
    }

    /**
     * Returns the background color for the given connection state.
     */
    fun backgroundForState(state: WebSocketState): Color = when (state) {
        WebSocketState.CONNECTING -> connectingBackground
        WebSocketState.OPEN -> openBackground
        WebSocketState.CLOSING -> closingBackground
        WebSocketState.CLOSED -> closedBackground
    }

    /**
     * Returns the color for the given message direction.
     */
    fun forDirection(direction: WebSocketMessageDirection): Color = when (direction) {
        WebSocketMessageDirection.SENT -> sent
        WebSocketMessageDirection.RECEIVED -> received
    }

    /**
     * Returns the background color for the given message direction.
     */
    fun backgroundForDirection(direction: WebSocketMessageDirection): Color = when (direction) {
        WebSocketMessageDirection.SENT -> sentBackground
        WebSocketMessageDirection.RECEIVED -> receivedBackground
    }

    /**
     * Returns the color for the given message type.
     */
    fun forMessageType(type: WebSocketMessageType): Color = when (type) {
        WebSocketMessageType.TEXT -> textMessage
        WebSocketMessageType.BINARY -> binaryMessage
        WebSocketMessageType.PING -> pingPong
        WebSocketMessageType.PONG -> pingPong
    }
}

/**
 * Light theme WebSocket colors.
 */
val LightWebSocketColors = WebSocketColors(
    // Connection states
    connecting = Color(0xFFF57C00), // Orange 700
    open = Color(0xFF388E3C), // Green 700
    closing = Color(0xFFE65100), // Deep Orange 700
    closed = Color(0xFF757575), // Gray 600
    // Directions
    sent = Color(0xFF1976D2), // Blue 700
    received = Color(0xFF388E3C), // Green 700
    // Message types
    textMessage = Color(0xFF424242), // Gray 800
    binaryMessage = Color(0xFF7B1FA2), // Purple 700
    pingPong = Color(0xFF0097A7), // Cyan 700
    // Backgrounds
    sentBackground = Color(0xFFE3F2FD), // Blue 50
    receivedBackground = Color(0xFFE8F5E9), // Green 50
    connectingBackground = Color(0xFFFFF3E0), // Orange 50
    openBackground = Color(0xFFE8F5E9), // Green 50
    closingBackground = Color(0xFFFBE9E7), // Deep Orange 50
    closedBackground = Color(0xFFF5F5F5), // Gray 100
)

/**
 * Dark theme WebSocket colors.
 */
val DarkWebSocketColors = WebSocketColors(
    // Connection states
    connecting = Color(0xFFFFB74D), // Orange 300
    open = Color(0xFF81C784), // Green 300
    closing = Color(0xFFFF8A65), // Deep Orange 300
    closed = Color(0xFF9E9E9E), // Gray 500
    // Directions
    sent = Color(0xFF64B5F6), // Blue 300
    received = Color(0xFF81C784), // Green 300
    // Message types
    textMessage = Color(0xFFE0E0E0), // Gray 300
    binaryMessage = Color(0xFFBA68C8), // Purple 300
    pingPong = Color(0xFF4DD0E1), // Cyan 300
    // Backgrounds (dark tints)
    sentBackground = Color(0xFF0D47A1).copy(alpha = 0.2f), // Blue with alpha
    receivedBackground = Color(0xFF1B5E20).copy(alpha = 0.2f), // Green with alpha
    connectingBackground = Color(0xFFE65100).copy(alpha = 0.2f), // Orange with alpha
    openBackground = Color(0xFF1B5E20).copy(alpha = 0.2f), // Green with alpha
    closingBackground = Color(0xFFBF360C).copy(alpha = 0.2f), // Deep Orange with alpha
    closedBackground = Color(0xFF212121), // Gray 900
)

/**
 * Returns the appropriate WebSocket colors based on the current theme.
 */
@Composable
fun webSocketColors(darkTheme: Boolean = isSystemInDarkTheme()): WebSocketColors {
    return if (darkTheme) DarkWebSocketColors else LightWebSocketColors
}
