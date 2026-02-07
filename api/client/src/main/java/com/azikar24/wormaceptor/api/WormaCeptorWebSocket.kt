package com.azikar24.wormaceptor.api

import android.util.Log
import okhttp3.WebSocketListener
import okio.ByteString

private const val TAG = "WormaCeptorWebSocket"

/**
 * Public API for monitoring WebSocket connections with WormaCeptor.
 *
 * Unlike HTTP which uses [WormaCeptorInterceptor], WebSocket monitoring requires
 * wrapping your [WebSocketListener] since OkHttp's interceptors don't intercept
 * WebSocket traffic.
 *
 * Usage:
 * ```kotlin
 * val monitor = WormaCeptorWebSocket.wrap(myListener, "wss://example.com/ws")
 *
 * // Use the wrapped listener with OkHttp
 * val webSocket = client.newWebSocket(request, monitor.listener)
 *
 * // Record sent messages (OkHttp doesn't notify listeners of outgoing messages)
 * webSocket.send(message)
 * monitor.recordSentMessage(message)
 * ```
 */
class WormaCeptorWebSocket private constructor(
    delegate: WebSocketListener?,
    url: String,
) {
    private var wrappedListener: Any? = null
    private var wrappedListenerClass: Class<*>? = null

    /**
     * The wrapped listener to pass to [okhttp3.OkHttpClient.newWebSocket].
     * This listener forwards all events to your original listener while
     * recording them for inspection in WormaCeptor.
     */
    val listener: WebSocketListener

    init {
        // Use reflection to wrap with WebSocketMonitorEngine
        val result = try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine",
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            val engine = getMethod.invoke(null, engineClass)

            val wrapMethod = if (delegate != null) {
                engineClass.getMethod("wrap", WebSocketListener::class.java, String::class.java)
            } else {
                engineClass.getMethod("wrap", String::class.java)
            }

            val wrapped = if (delegate != null) {
                wrapMethod.invoke(engine, delegate, url)
            } else {
                wrapMethod.invoke(engine, url)
            }

            wrappedListener = wrapped
            wrappedListenerClass = wrapped?.javaClass
            wrapped as? WebSocketListener
        } catch (e: Exception) {
            Log.d(TAG, "WebSocket monitoring not available: ${e.message}")
            null
        }

        listener = result ?: delegate ?: NoOpWebSocketListener()
    }

    /**
     * Records a sent text message.
     *
     * OkHttp's [WebSocketListener] does not have a callback for outgoing messages,
     * so you must call this method after calling [okhttp3.WebSocket.send] to track
     * sent messages in WormaCeptor.
     *
     * @param text The text message being sent
     */
    fun recordSentMessage(text: String) {
        val wrapped = wrappedListener ?: return
        val clazz = wrappedListenerClass ?: return
        try {
            val method = clazz.getMethod("recordSentMessage", String::class.java)
            method.invoke(wrapped, text)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to record sent message: ${e.message}")
        }
    }

    /**
     * Records a sent binary message.
     *
     * @param bytes The binary message being sent
     */
    fun recordSentMessage(bytes: ByteString) {
        val wrapped = wrappedListener ?: return
        val clazz = wrappedListenerClass ?: return
        try {
            val method = clazz.getMethod("recordSentMessage", ByteString::class.java)
            method.invoke(wrapped, bytes)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to record sent binary message: ${e.message}")
        }
    }

    /**
     * Records a ping frame.
     *
     * @param payload The ping payload
     */
    fun recordPing(payload: ByteString) {
        val wrapped = wrappedListener ?: return
        val clazz = wrappedListenerClass ?: return
        try {
            val method = clazz.getMethod("recordPing", ByteString::class.java)
            method.invoke(wrapped, payload)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to record ping: ${e.message}")
        }
    }

    /**
     * Records a pong frame.
     *
     * @param payload The pong payload
     */
    fun recordPong(payload: ByteString) {
        val wrapped = wrappedListener ?: return
        val clazz = wrappedListenerClass ?: return
        try {
            val method = clazz.getMethod("recordPong", ByteString::class.java)
            method.invoke(wrapped, payload)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to record pong: ${e.message}")
        }
    }

    /**
     * Gets the connection ID for this WebSocket connection.
     * Returns -1 if monitoring is not available.
     */
    fun getConnectionId(): Long {
        val wrapped = wrappedListener ?: return -1
        val clazz = wrappedListenerClass ?: return -1
        return try {
            val method = clazz.getMethod("getConnectionId")
            method.invoke(wrapped) as? Long ?: -1
        } catch (e: Exception) {
            -1
        }
    }

    private class NoOpWebSocketListener : WebSocketListener()

    companion object {
        /**
         * Wraps a [WebSocketListener] for monitoring.
         *
         * All WebSocket events will be intercepted and recorded before being
         * forwarded to your original listener.
         *
         * @param delegate Your original WebSocketListener
         * @param url The WebSocket URL (for identification in the UI)
         * @return A [WormaCeptorWebSocket] containing the wrapped listener
         */
        fun wrap(delegate: WebSocketListener, url: String): WormaCeptorWebSocket {
            return WormaCeptorWebSocket(delegate, url)
        }

        /**
         * Creates a monitoring-only listener without a delegate.
         *
         * Use this when you only need monitoring without event forwarding.
         *
         * @param url The WebSocket URL (for identification in the UI)
         * @return A [WormaCeptorWebSocket] containing the monitoring listener
         */
        fun wrap(url: String): WormaCeptorWebSocket {
            return WormaCeptorWebSocket(null, url)
        }
    }
}
