package com.azikar24.wormaceptor.mcp.server.streaming

import android.util.Log
import io.ktor.server.routing.Routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "StreamRoutes"

internal fun Routing.streamRoutes() {
    val eventManager = EventStreamManager()
    val json = Json { ignoreUnknownKeys = true }

    webSocket("/api/stream") {
        val sessionId = UUID.randomUUID().toString()
        eventManager.addSession(sessionId, this)
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    try {
                        val message = json.decodeFromString<SubscribeMessage>(frame.readText())
                        when (message.type) {
                            "subscribe" -> eventManager.subscribe(sessionId, message.channels)
                            "unsubscribe" -> eventManager.unsubscribe(sessionId, message.channels)
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Log.w(TAG, "Failed to parse client message", e)
                    }
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "WebSocket session error: $sessionId", e)
        } finally {
            eventManager.removeSession(sessionId)
        }
    }
}
