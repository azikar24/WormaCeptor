package com.azikar24.wormaceptor.mcp.server.streaming

import io.kotest.matchers.shouldBe
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Test
import io.ktor.client.plugins.websocket.WebSockets as ClientWebSockets
import io.ktor.server.websocket.WebSockets as ServerWebSockets

class StreamRoutesTest {

    private fun event(channel: String) = StreamEvent(channel, "new", 0, JsonPrimitive(channel))

    @Test
    fun `client receives events only for subscribed channels`() = testApplication {
        val manager = EventStreamManager()
        application {
            install(ServerWebSockets)
            routing { streamRoutes(manager) }
        }
        val client = createClient { install(ClientWebSockets) }

        client.webSocket("/api/stream") {
            send("""{"type":"subscribe","channels":["transactions"]}""")
            // The subscription is applied asynchronously, so keep publishing until it lands.
            val publisher = launch {
                while (isActive) {
                    manager.broadcast("logs", event("logs"))
                    manager.broadcast("transactions", event("transactions"))
                    delay(PUBLISH_INTERVAL_MS)
                }
            }
            val frame = withTimeout(TIMEOUT_MS) { incoming.receive() as Frame.Text }
            publisher.cancel()

            Json.decodeFromString<StreamEvent>(frame.readText()).channel shouldBe "transactions"
        }
    }

    @Test
    fun `closed sessions are removed`() = testApplication {
        val manager = EventStreamManager()
        application {
            install(ServerWebSockets)
            routing { streamRoutes(manager) }
        }
        val client = createClient { install(ClientWebSockets) }

        client.webSocket("/api/stream") { awaitSessionCount(manager, 1) }
        awaitSessionCount(manager, 0)
    }

    private suspend fun awaitSessionCount(
        manager: EventStreamManager,
        expected: Int,
    ) = withTimeout(TIMEOUT_MS) {
        while (manager.activeSessionCount != expected) delay(PUBLISH_INTERVAL_MS)
    }

    private companion object {
        const val PUBLISH_INTERVAL_MS = 20L
        const val TIMEOUT_MS = 5_000L
    }
}
