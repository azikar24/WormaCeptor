/*
 * Copyright AziKar24 2023.
 */

package com.azikar24.wormaceptorapp.sampleservice

import android.util.Log
import com.azikar24.wormaceptor.api.WormaCeptorInterceptor
import com.azikar24.wormaceptor.api.WormaCeptorWebSocket
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Sample WebSocket service for testing WormaCeptor's WebSocket monitoring.
 * Uses Postman's public echo WebSocket server.
 */
object SampleWebSocketService {

    private const val TAG = "SampleWebSocketService"
    private const val ECHO_SERVER_URL = "wss://ws.postman-echo.com/raw"

    private var webSocket: WebSocket? = null
    private var monitor: WormaCeptorWebSocket? = null

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                WormaCeptorInterceptor()
                    .showNotification(true)
                    .maxContentLength(250000L),
            )
            .build()
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket opened: ${response.message}")
            // Send some test messages and record them
            sendAndRecord("Hello from WormaCeptor!")
            sendAndRecord("""{"type":"test","message":"JSON message","timestamp":${System.currentTimeMillis()}}""")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "WebSocket message received: $text")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closing: $code - $reason")
            webSocket.close(1000, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "WebSocket closed: $code - $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket failure: ${t.message}", t)
        }
    }

    private fun sendAndRecord(message: String) {
        webSocket?.send(message)
        monitor?.recordSentMessage(message)
    }

    fun connect() {
        val request = Request.Builder()
            .url(ECHO_SERVER_URL)
            .build()

        // Use the public API to wrap the listener for monitoring
        monitor = WormaCeptorWebSocket.wrap(listener, ECHO_SERVER_URL)

        webSocket = client.newWebSocket(request, monitor!!.listener)
    }

    fun sendMessage(message: String) {
        sendAndRecord(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "User requested disconnect")
        webSocket = null
        monitor = null
    }
}
