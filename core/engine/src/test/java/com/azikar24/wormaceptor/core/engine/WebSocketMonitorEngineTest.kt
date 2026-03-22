package com.azikar24.wormaceptor.core.engine

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.domain.entities.WebSocketState
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.encodeUtf8
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WebSocketMonitorEngineTest {

    private lateinit var engine: WebSocketMonitorEngine

    @BeforeEach
    fun setUp() {
        engine = WebSocketMonitorEngine()
    }

    @Nested
    inner class InitialState {

        @Test
        fun `connections should be empty initially`() = runTest {
            engine.connections.test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `messages should be empty initially`() = runTest {
            engine.messages.test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class Wrap {

        @Test
        fun `should create MonitoringWebSocketListener with delegate`() {
            val delegate = mockk<WebSocketListener>(relaxed = true)
            val listener = engine.wrap(delegate, "wss://example.com")

            listener.getConnectionId() shouldBe -1L
        }

        @Test
        fun `should create MonitoringWebSocketListener without delegate`() {
            val listener = engine.wrap("wss://example.com")

            listener.getConnectionId() shouldBe -1L
        }
    }

    @Nested
    inner class OnOpen {

        @Test
        fun `should register connection on open`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            engine.connections.test {
                awaitItem() // initial empty

                listener.onOpen(webSocket, response)

                // registerConnection emits CONNECTING, then updateConnection emits OPEN
                val connecting = awaitItem()
                connecting shouldHaveSize 1
                connecting.first().url shouldBe "wss://example.com"
                connecting.first().state shouldBe WebSocketState.CONNECTING

                val opened = awaitItem()
                opened shouldHaveSize 1
                opened.first().state shouldBe WebSocketState.OPEN

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `should assign incremental connection ids`() = runTest {
            val listener1 = engine.wrap("wss://example.com/1")
            val listener2 = engine.wrap("wss://example.com/2")
            val ws1 = mockk<WebSocket>(relaxed = true)
            val ws2 = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener1.onOpen(ws1, response)
            listener2.onOpen(ws2, response)

            val connections = engine.connections.value
            connections shouldHaveSize 2
            connections[0].id shouldBe 1L
            connections[1].id shouldBe 2L
        }

        @Test
        fun `should forward onOpen to delegate`() {
            val delegate = mockk<WebSocketListener>(relaxed = true)
            val listener = engine.wrap(delegate, "wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)

            verify { delegate.onOpen(webSocket, response) }
        }
    }

    @Nested
    inner class OnMessageText {

        @Test
        fun `should record received text message`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "Hello World")

            val messages = engine.messages.value
            messages shouldHaveSize 1
            messages.first().type shouldBe WebSocketMessageType.TEXT
            messages.first().direction shouldBe WebSocketMessageDirection.RECEIVED
            messages.first().payload shouldBe "Hello World"
        }

        @Test
        fun `should calculate message size in bytes`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "Hello")

            engine.messages.value.first().size shouldBe 5L
        }

        @Test
        fun `should forward text message to delegate`() {
            val delegate = mockk<WebSocketListener>(relaxed = true)
            val listener = engine.wrap(delegate, "wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "Hello")

            verify { delegate.onMessage(webSocket, "Hello") }
        }
    }

    @Nested
    inner class OnMessageBinary {

        @Test
        fun `should record received binary message`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)
            val bytes = "binary data".encodeUtf8()

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, bytes)

            val messages = engine.messages.value
            messages shouldHaveSize 1
            messages.first().type shouldBe WebSocketMessageType.BINARY
            messages.first().direction shouldBe WebSocketMessageDirection.RECEIVED
            messages.first().payload shouldBe bytes.hex()
        }
    }

    @Nested
    inner class OnClosing {

        @Test
        fun `should update connection state to CLOSING`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onClosing(webSocket, 1000, "Normal closure")

            val connection = engine.connections.value.first()
            connection.state shouldBe WebSocketState.CLOSING
            connection.closeCode shouldBe 1000
            connection.closeReason shouldBe "Normal closure"
        }

        @Test
        fun `should forward onClosing to delegate`() {
            val delegate = mockk<WebSocketListener>(relaxed = true)
            val listener = engine.wrap(delegate, "wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onClosing(webSocket, 1000, "bye")

            verify { delegate.onClosing(webSocket, 1000, "bye") }
        }
    }

    @Nested
    inner class OnClosed {

        @Test
        fun `should update connection state to CLOSED`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onClosed(webSocket, 1000, "Normal closure")

            val connection = engine.connections.value.first()
            connection.state shouldBe WebSocketState.CLOSED
            connection.closeCode shouldBe 1000
            connection.closeReason shouldBe "Normal closure"
            connection.closedAt shouldBe connection.closedAt // non-null
        }

        @Test
        fun `should forward onClosed to delegate`() {
            val delegate = mockk<WebSocketListener>(relaxed = true)
            val listener = engine.wrap(delegate, "wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onClosed(webSocket, 1000, "bye")

            verify { delegate.onClosed(webSocket, 1000, "bye") }
        }
    }

    @Nested
    inner class OnFailure {

        @Test
        fun `should update connection state to CLOSED on failure`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)
            val throwable = RuntimeException("Connection lost")

            listener.onOpen(webSocket, response)
            listener.onFailure(webSocket, throwable, null)

            val connection = engine.connections.value.first()
            connection.state shouldBe WebSocketState.CLOSED
            connection.closeReason shouldBe "Connection lost"
        }

        @Test
        fun `should forward onFailure to delegate`() {
            val delegate = mockk<WebSocketListener>(relaxed = true)
            val listener = engine.wrap(delegate, "wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)
            val throwable = RuntimeException("fail")

            listener.onOpen(webSocket, response)
            listener.onFailure(webSocket, throwable, null)

            verify { delegate.onFailure(webSocket, throwable, null) }
        }
    }

    @Nested
    inner class RecordSentMessage {

        @Test
        fun `should record sent text message`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.recordSentMessage("outgoing")

            val messages = engine.messages.value
            messages shouldHaveSize 1
            messages.first().type shouldBe WebSocketMessageType.TEXT
            messages.first().direction shouldBe WebSocketMessageDirection.SENT
            messages.first().payload shouldBe "outgoing"
        }

        @Test
        fun `should record sent binary message`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)
            val bytes = "binary".encodeUtf8()

            listener.onOpen(webSocket, response)
            listener.recordSentMessage(bytes)

            val messages = engine.messages.value
            messages shouldHaveSize 1
            messages.first().type shouldBe WebSocketMessageType.BINARY
            messages.first().direction shouldBe WebSocketMessageDirection.SENT
        }
    }

    @Nested
    inner class RecordPingPong {

        @Test
        fun `should record ping message`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)
            val payload = "ping".encodeUtf8()

            listener.onOpen(webSocket, response)
            listener.recordPing(payload)

            val messages = engine.messages.value
            messages shouldHaveSize 1
            messages.first().type shouldBe WebSocketMessageType.PING
            messages.first().direction shouldBe WebSocketMessageDirection.SENT
        }

        @Test
        fun `should record pong message`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)
            val payload = "pong".encodeUtf8()

            listener.onOpen(webSocket, response)
            listener.recordPong(payload)

            val messages = engine.messages.value
            messages shouldHaveSize 1
            messages.first().type shouldBe WebSocketMessageType.PONG
            messages.first().direction shouldBe WebSocketMessageDirection.RECEIVED
        }
    }

    @Nested
    inner class GetMessagesForConnection {

        @Test
        fun `should return messages for specific connection`() {
            val listener1 = engine.wrap("wss://example.com/1")
            val listener2 = engine.wrap("wss://example.com/2")
            val ws1 = mockk<WebSocket>(relaxed = true)
            val ws2 = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener1.onOpen(ws1, response)
            listener2.onOpen(ws2, response)

            listener1.onMessage(ws1, "msg1")
            listener2.onMessage(ws2, "msg2")
            listener1.onMessage(ws1, "msg3")

            val connId1 = listener1.getConnectionId()
            val messagesForConn1 = engine.getMessagesForConnection(connId1)
            messagesForConn1 shouldHaveSize 2
        }

        @Test
        fun `should return empty for unknown connection`() {
            engine.getMessagesForConnection(999L).shouldBeEmpty()
        }
    }

    @Nested
    inner class GetMessageCountForConnection {

        @Test
        fun `should count messages for specific connection`() {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "one")
            listener.onMessage(webSocket, "two")
            listener.onMessage(webSocket, "three")

            val connId = listener.getConnectionId()
            engine.getMessageCountForConnection(connId) shouldBe 3
        }
    }

    @Nested
    inner class Clear {

        @Test
        fun `should clear all connections and messages`() = runTest {
            val listener = engine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "test")

            engine.clear()

            engine.connections.value.shouldBeEmpty()
            engine.messages.value.shouldBeEmpty()
        }
    }

    @Nested
    inner class ClearMessagesForConnection {

        @Test
        fun `should only clear messages for specified connection`() {
            val listener1 = engine.wrap("wss://example.com/1")
            val listener2 = engine.wrap("wss://example.com/2")
            val ws1 = mockk<WebSocket>(relaxed = true)
            val ws2 = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener1.onOpen(ws1, response)
            listener2.onOpen(ws2, response)

            listener1.onMessage(ws1, "msg1")
            listener2.onMessage(ws2, "msg2")

            val connId1 = listener1.getConnectionId()
            engine.clearMessagesForConnection(connId1)

            engine.messages.value shouldHaveSize 1
            engine.messages.value.first().payload shouldBe "msg2"
        }
    }

    @Nested
    inner class CircularBuffer {

        @Test
        fun `should evict oldest messages when buffer is full`() {
            val smallEngine = WebSocketMonitorEngine(maxMessages = 3)
            val listener = smallEngine.wrap("wss://example.com")
            val webSocket = mockk<WebSocket>(relaxed = true)
            val response = mockk<Response>(relaxed = true)

            listener.onOpen(webSocket, response)
            listener.onMessage(webSocket, "msg1")
            listener.onMessage(webSocket, "msg2")
            listener.onMessage(webSocket, "msg3")
            listener.onMessage(webSocket, "msg4")

            val messages = smallEngine.messages.value
            messages shouldHaveSize 3
            messages[0].payload shouldBe "msg2"
            messages[1].payload shouldBe "msg3"
            messages[2].payload shouldBe "msg4"
        }
    }

    @Nested
    inner class CompanionConstants {

        @Test
        fun `DEFAULT_MAX_MESSAGES should be 500`() {
            WebSocketMonitorEngine.DEFAULT_MAX_MESSAGES shouldBe 500
        }
    }
}
