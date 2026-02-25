package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.domain.entities.WebSocketState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.atomic.AtomicLong

/**
 * Engine that monitors WebSocket connections and messages.
 *
 * Provides a WebSocketListener wrapper that intercepts all WebSocket events
 * and stores them in memory for inspection.
 *
 * Usage:
 * ```kotlin
 * val engine = WebSocketMonitorEngine()
 * val listener = engine.wrap(yourWebSocketListener)
 * okHttpClient.newWebSocket(request, listener)
 * ```
 *
 * @property maxMessages Maximum number of messages to store in the circular buffer
 */
class WebSocketMonitorEngine(
    private val maxMessages: Int = DEFAULT_MAX_MESSAGES,
) {
    private val connectionIdGenerator = AtomicLong(0)
    private val messageIdGenerator = AtomicLong(0)

    private val connectionMap = mutableMapOf<WebSocket, Long>()
    private val connectionMapLock = Any()

    private val _connections = MutableStateFlow<List<WebSocketConnection>>(emptyList())
    val connections: StateFlow<List<WebSocketConnection>> = _connections.asStateFlow()

    private val _messages = MutableStateFlow<List<WebSocketMessage>>(emptyList())
    val messages: StateFlow<List<WebSocketMessage>> = _messages.asStateFlow()

    // Internal storage
    private val connectionBuffer = mutableListOf<WebSocketConnection>()
    private val messageBuffer = ArrayDeque<WebSocketMessage>(maxMessages)
    private val bufferLock = Any()

    /**
     * Creates a monitoring wrapper around the given WebSocketListener.
     * All events will be intercepted and stored before being forwarded
     * to the original listener.
     *
     * @param delegate The original WebSocketListener to wrap
     * @param url The WebSocket URL for identification
     * @return A wrapped listener that monitors all events
     */
    fun wrap(
        delegate: WebSocketListener,
        url: String,
    ): MonitoringWebSocketListener {
        return MonitoringWebSocketListener(delegate, url)
    }

    /**
     * Creates a monitoring wrapper without a delegate listener.
     * Use this when you only need monitoring without event forwarding.
     *
     * @param url The WebSocket URL for identification
     * @return A wrapped listener that monitors all events
     */
    fun wrap(url: String): MonitoringWebSocketListener {
        return MonitoringWebSocketListener(null, url)
    }

    /**
     * Gets all messages for a specific connection.
     *
     * @param connectionId The connection ID to filter by
     * @return List of messages for the specified connection
     */
    fun getMessagesForConnection(connectionId: Long): List<WebSocketMessage> {
        synchronized(bufferLock) {
            return messageBuffer.filter { it.connectionId == connectionId }
        }
    }

    /**
     * Gets the message count for a specific connection.
     *
     * @param connectionId The connection ID to count messages for
     * @return Number of messages for the specified connection
     */
    fun getMessageCountForConnection(connectionId: Long): Int {
        synchronized(bufferLock) {
            return messageBuffer.count { it.connectionId == connectionId }
        }
    }

    /**
     * Clears all stored connections and messages.
     */
    fun clear() {
        synchronized(bufferLock) {
            connectionBuffer.clear()
            messageBuffer.clear()
        }
        synchronized(connectionMapLock) {
            connectionMap.clear()
        }
        _connections.value = emptyList()
        _messages.value = emptyList()
    }

    /**
     * Clears messages for a specific connection.
     *
     * @param connectionId The connection ID to clear messages for
     */
    fun clearMessagesForConnection(connectionId: Long) {
        synchronized(bufferLock) {
            messageBuffer.removeAll { it.connectionId == connectionId }
            _messages.value = messageBuffer.toList()
        }
    }

    private fun registerConnection(
        webSocket: WebSocket,
        url: String,
    ): Long {
        val connectionId = connectionIdGenerator.incrementAndGet()
        synchronized(connectionMapLock) {
            connectionMap[webSocket] = connectionId
        }
        val connection = WebSocketConnection(
            id = connectionId,
            url = url,
            state = WebSocketState.CONNECTING,
        )
        addConnection(connection)
        return connectionId
    }

    private fun getConnectionId(webSocket: WebSocket): Long? {
        synchronized(connectionMapLock) {
            return connectionMap[webSocket]
        }
    }

    private fun addConnection(connection: WebSocketConnection) {
        synchronized(bufferLock) {
            connectionBuffer.add(connection)
            _connections.value = connectionBuffer.toList()
        }
    }

    private fun updateConnection(
        connectionId: Long,
        updater: (WebSocketConnection) -> WebSocketConnection,
    ) {
        synchronized(bufferLock) {
            val index = connectionBuffer.indexOfFirst { it.id == connectionId }
            if (index >= 0) {
                connectionBuffer[index] = updater(connectionBuffer[index])
                _connections.value = connectionBuffer.toList()
            }
        }
    }

    private fun addMessage(message: WebSocketMessage) {
        synchronized(bufferLock) {
            if (messageBuffer.size >= maxMessages) {
                messageBuffer.removeFirst()
            }
            messageBuffer.addLast(message)
            _messages.value = messageBuffer.toList()
        }
    }

    /**
     * WebSocketListener that intercepts all events for monitoring.
     */
    inner class MonitoringWebSocketListener(
        private val delegate: WebSocketListener?,
        private val url: String,
    ) : WebSocketListener() {

        private var connectionId: Long = -1

        override fun onOpen(
            webSocket: WebSocket,
            response: Response,
        ) {
            connectionId = registerConnection(webSocket, url)
            updateConnection(connectionId) { conn ->
                conn.copy(
                    state = WebSocketState.OPEN,
                    openedAt = System.currentTimeMillis(),
                )
            }
            delegate?.onOpen(webSocket, response)
        }

        override fun onMessage(
            webSocket: WebSocket,
            text: String,
        ) {
            val connId = getConnectionId(webSocket) ?: connectionId
            addMessage(
                WebSocketMessage(
                    id = messageIdGenerator.incrementAndGet(),
                    connectionId = connId,
                    type = WebSocketMessageType.TEXT,
                    direction = WebSocketMessageDirection.RECEIVED,
                    payload = text,
                    timestamp = System.currentTimeMillis(),
                    size = text.toByteArray(Charsets.UTF_8).size.toLong(),
                ),
            )
            delegate?.onMessage(webSocket, text)
        }

        override fun onMessage(
            webSocket: WebSocket,
            bytes: ByteString,
        ) {
            val connId = getConnectionId(webSocket) ?: connectionId
            addMessage(
                WebSocketMessage(
                    id = messageIdGenerator.incrementAndGet(),
                    connectionId = connId,
                    type = WebSocketMessageType.BINARY,
                    direction = WebSocketMessageDirection.RECEIVED,
                    payload = bytes.hex(),
                    timestamp = System.currentTimeMillis(),
                    size = bytes.size.toLong(),
                ),
            )
            delegate?.onMessage(webSocket, bytes)
        }

        override fun onClosing(
            webSocket: WebSocket,
            code: Int,
            reason: String,
        ) {
            val connId = getConnectionId(webSocket) ?: connectionId
            updateConnection(connId) { conn ->
                conn.copy(
                    state = WebSocketState.CLOSING,
                    closeCode = code,
                    closeReason = reason,
                )
            }
            delegate?.onClosing(webSocket, code, reason)
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String,
        ) {
            val connId = getConnectionId(webSocket) ?: connectionId
            updateConnection(connId) { conn ->
                conn.copy(
                    state = WebSocketState.CLOSED,
                    closedAt = System.currentTimeMillis(),
                    closeCode = code,
                    closeReason = reason,
                )
            }
            synchronized(connectionMapLock) {
                connectionMap.remove(webSocket)
            }
            delegate?.onClosed(webSocket, code, reason)
        }

        override fun onFailure(
            webSocket: WebSocket,
            t: Throwable,
            response: Response?,
        ) {
            val connId = getConnectionId(webSocket) ?: connectionId
            updateConnection(connId) { conn ->
                conn.copy(
                    state = WebSocketState.CLOSED,
                    closedAt = System.currentTimeMillis(),
                    closeReason = t.message ?: "Connection failed",
                )
            }
            synchronized(connectionMapLock) {
                connectionMap.remove(webSocket)
            }
            delegate?.onFailure(webSocket, t, response)
        }

        /**
         * Records a sent text message. Call this when sending messages
         * to track outgoing communication.
         *
         * @param text The text message being sent
         */
        fun recordSentMessage(text: String) {
            addMessage(
                WebSocketMessage(
                    id = messageIdGenerator.incrementAndGet(),
                    connectionId = connectionId,
                    type = WebSocketMessageType.TEXT,
                    direction = WebSocketMessageDirection.SENT,
                    payload = text,
                    timestamp = System.currentTimeMillis(),
                    size = text.toByteArray(Charsets.UTF_8).size.toLong(),
                ),
            )
        }

        /**
         * Records a sent binary message. Call this when sending messages
         * to track outgoing communication.
         *
         * @param bytes The binary message being sent
         */
        fun recordSentMessage(bytes: ByteString) {
            addMessage(
                WebSocketMessage(
                    id = messageIdGenerator.incrementAndGet(),
                    connectionId = connectionId,
                    type = WebSocketMessageType.BINARY,
                    direction = WebSocketMessageDirection.SENT,
                    payload = bytes.hex(),
                    timestamp = System.currentTimeMillis(),
                    size = bytes.size.toLong(),
                ),
            )
        }

        /**
         * Records a ping frame.
         *
         * @param payload The ping payload
         */
        fun recordPing(payload: ByteString) {
            addMessage(
                WebSocketMessage(
                    id = messageIdGenerator.incrementAndGet(),
                    connectionId = connectionId,
                    type = WebSocketMessageType.PING,
                    direction = WebSocketMessageDirection.SENT,
                    payload = payload.hex(),
                    timestamp = System.currentTimeMillis(),
                    size = payload.size.toLong(),
                ),
            )
        }

        /**
         * Records a pong frame.
         *
         * @param payload The pong payload
         */
        fun recordPong(payload: ByteString) {
            addMessage(
                WebSocketMessage(
                    id = messageIdGenerator.incrementAndGet(),
                    connectionId = connectionId,
                    type = WebSocketMessageType.PONG,
                    direction = WebSocketMessageDirection.RECEIVED,
                    payload = payload.hex(),
                    timestamp = System.currentTimeMillis(),
                    size = payload.size.toLong(),
                ),
            )
        }

        /**
         * Gets the connection ID for this listener.
         */
        fun getConnectionId(): Long = connectionId
    }

    companion object {
        const val DEFAULT_MAX_MESSAGES = 500
    }
}
