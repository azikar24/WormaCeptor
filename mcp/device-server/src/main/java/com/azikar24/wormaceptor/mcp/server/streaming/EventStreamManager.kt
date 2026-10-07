package com.azikar24.wormaceptor.mcp.server.streaming

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import io.ktor.websocket.send
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

internal class EventStreamManager {

    private val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()
    private val subscriptions = ConcurrentHashMap<String, MutableSet<String>>()
    private val json = Json { encodeDefaults = true }

    fun addSession(
        sessionId: String,
        session: DefaultWebSocketServerSession,
    ) {
        sessions[sessionId] = session
        subscriptions[sessionId] = Collections.newSetFromMap(ConcurrentHashMap())
    }

    fun removeSession(sessionId: String) {
        sessions.remove(sessionId)
        subscriptions.remove(sessionId)
    }

    fun subscribe(
        sessionId: String,
        channels: List<String>,
    ) {
        subscriptions[sessionId]?.addAll(channels)
    }

    fun unsubscribe(
        sessionId: String,
        channels: List<String>,
    ) {
        subscriptions[sessionId]?.removeAll(channels.toSet())
    }

    suspend fun broadcast(
        channel: String,
        event: StreamEvent,
    ) {
        val message = json.encodeToString(event)
        val deadSessions = mutableListOf<String>()

        subscriptions.forEach { (sessionId, channels) ->
            if (channel in channels) {
                try {
                    sessions[sessionId]?.send(Frame.Text(message))
                } catch (_: Exception) {
                    deadSessions.add(sessionId)
                }
            }
        }

        deadSessions.forEach { removeSession(it) }
    }

    val activeSessionCount: Int get() = sessions.size
}
