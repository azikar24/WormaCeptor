package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WebSocketMessageTest {

    private fun defaultMessage() = WebSocketMessage(
        id = 1,
        connectionId = 100,
        type = WebSocketMessageType.TEXT,
        direction = WebSocketMessageDirection.SENT,
        payload = "hello world",
        timestamp = 1_700_000_000_000L,
        size = 11,
    )

    @Nested
    inner class Construction {

        @Test
        fun `constructs with all fields`() {
            val msg = defaultMessage()

            msg.id shouldBe 1
            msg.connectionId shouldBe 100
            msg.type shouldBe WebSocketMessageType.TEXT
            msg.direction shouldBe WebSocketMessageDirection.SENT
            msg.payload shouldBe "hello world"
            msg.timestamp shouldBe 1_700_000_000_000L
            msg.size shouldBe 11
        }
    }

    @Nested
    inner class PayloadPreview {

        @Test
        fun `returns full payload when shorter than maxLength`() {
            val msg = defaultMessage().copy(payload = "short")

            msg.payloadPreview() shouldBe "short"
        }

        @Test
        fun `returns full payload when exactly maxLength`() {
            val exactPayload = "a".repeat(100)
            val msg = defaultMessage().copy(payload = exactPayload)

            msg.payloadPreview() shouldBe exactPayload
        }

        @Test
        fun `truncates and adds ellipsis when longer than maxLength`() {
            val longPayload = "a".repeat(150)
            val msg = defaultMessage().copy(payload = longPayload)

            val preview = msg.payloadPreview()

            preview shouldBe "a".repeat(100) + "..."
        }

        @Test
        fun `respects custom maxLength`() {
            val msg = defaultMessage().copy(payload = "hello world")

            msg.payloadPreview(maxLength = 5) shouldBe "hello..."
        }

        @Test
        fun `returns empty string for empty payload`() {
            val msg = defaultMessage().copy(payload = "")

            msg.payloadPreview() shouldBe ""
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `equal instances have the same hashCode`() {
            val m1 = defaultMessage()
            val m2 = defaultMessage()

            m1 shouldBe m2
            m1.hashCode() shouldBe m2.hashCode()
        }

        @Test
        fun `different payload makes instances unequal`() {
            val m1 = defaultMessage()
            val m2 = defaultMessage().copy(payload = "different")

            m1 shouldNotBe m2
        }
    }
}

class WebSocketMessageTypeTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly four values`() {
            WebSocketMessageType.entries.size shouldBe 4
        }

        @Test
        fun `should contain TEXT, BINARY, PING, and PONG`() {
            WebSocketMessageType.entries.map { it.name } shouldContainExactly listOf(
                "TEXT",
                "BINARY",
                "PING",
                "PONG",
            )
        }
    }
}

class WebSocketMessageDirectionTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly two values`() {
            WebSocketMessageDirection.entries.size shouldBe 2
        }

        @Test
        fun `should contain SENT and RECEIVED`() {
            WebSocketMessageDirection.entries.map { it.name } shouldContainExactly listOf(
                "SENT",
                "RECEIVED",
            )
        }
    }
}

class WebSocketConnectionTest {

    private fun defaultConnection() = WebSocketConnection(
        id = 1,
        url = "wss://echo.example.com",
        state = WebSocketState.OPEN,
        openedAt = 1_700_000_000_000L,
    )

    @Nested
    inner class Construction {

        @Test
        fun `constructs with required fields`() {
            val conn = WebSocketConnection(
                id = 1,
                url = "wss://example.com",
                state = WebSocketState.CONNECTING,
            )

            conn.id shouldBe 1
            conn.url shouldBe "wss://example.com"
            conn.state shouldBe WebSocketState.CONNECTING
        }
    }

    @Nested
    inner class Defaults {

        @Test
        fun `openedAt defaults to null`() {
            val conn = WebSocketConnection(id = 1, url = "ws://x", state = WebSocketState.CONNECTING)

            conn.openedAt shouldBe null
        }

        @Test
        fun `closedAt defaults to null`() {
            val conn = defaultConnection()

            conn.closedAt shouldBe null
        }

        @Test
        fun `closeCode defaults to null`() {
            val conn = defaultConnection()

            conn.closeCode shouldBe null
        }

        @Test
        fun `closeReason defaults to null`() {
            val conn = defaultConnection()

            conn.closeReason shouldBe null
        }
    }

    @Nested
    inner class IsActive {

        @Test
        fun `CONNECTING state is active`() {
            val conn = defaultConnection().copy(state = WebSocketState.CONNECTING)

            conn.isActive shouldBe true
        }

        @Test
        fun `OPEN state is active`() {
            val conn = defaultConnection().copy(state = WebSocketState.OPEN)

            conn.isActive shouldBe true
        }

        @Test
        fun `CLOSING state is not active`() {
            val conn = defaultConnection().copy(state = WebSocketState.CLOSING)

            conn.isActive shouldBe false
        }

        @Test
        fun `CLOSED state is not active`() {
            val conn = defaultConnection().copy(state = WebSocketState.CLOSED)

            conn.isActive shouldBe false
        }
    }

    @Nested
    inner class Duration {

        @Test
        fun `returns null when openedAt is null`() {
            val conn = WebSocketConnection(id = 1, url = "ws://x", state = WebSocketState.CONNECTING)

            conn.duration shouldBe null
        }

        @Test
        fun `computes duration for closed connection`() {
            val conn = defaultConnection().copy(
                openedAt = 1_000L,
                closedAt = 1_500L,
            )

            conn.duration shouldBe 500L
        }

        @Test
        fun `computes duration from openedAt to now for open connection`() {
            val recentOpen = System.currentTimeMillis() - 100
            val conn = defaultConnection().copy(openedAt = recentOpen, closedAt = null)

            val duration = conn.duration
            (duration != null && duration >= 100) shouldBe true
        }
    }
}

class WebSocketStateTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly four values`() {
            WebSocketState.entries.size shouldBe 4
        }

        @Test
        fun `should contain CONNECTING, OPEN, CLOSING, and CLOSED`() {
            WebSocketState.entries.map { it.name } shouldContainExactly listOf(
                "CONNECTING",
                "OPEN",
                "CLOSING",
                "CLOSED",
            )
        }
    }
}
