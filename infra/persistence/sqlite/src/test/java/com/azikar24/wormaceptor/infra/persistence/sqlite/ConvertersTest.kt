package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.UUID

class ConvertersTest {

    private val converters = Converters()

    @Nested
    inner class `Date and Timestamp` {

        @Test
        fun `fromTimestamp converts millis to Date`() {
            val timestamp = 1_700_000_000_000L

            val result = converters.fromTimestamp(timestamp)

            result shouldBe Date(1_700_000_000_000L)
        }

        @Test
        fun `fromTimestamp returns null for null input`() {
            converters.fromTimestamp(null).shouldBeNull()
        }

        @Test
        fun `dateToTimestamp converts Date to millis`() {
            val date = Date(1_700_000_000_000L)

            val result = converters.dateToTimestamp(date)

            result shouldBe 1_700_000_000_000L
        }

        @Test
        fun `dateToTimestamp returns null for null input`() {
            converters.dateToTimestamp(null).shouldBeNull()
        }

        @Test
        fun `round-trips through timestamp and back`() {
            val original = 1_234_567_890_123L

            val date = converters.fromTimestamp(original)
            val result = converters.dateToTimestamp(date)

            result shouldBe original
        }

        @Test
        fun `handles epoch zero`() {
            val date = converters.fromTimestamp(0L)
            date shouldBe Date(0L)

            converters.dateToTimestamp(date) shouldBe 0L
        }
    }

    @Nested
    inner class `TransactionStatus conversion` {

        @Test
        fun `fromStatus converts ACTIVE to its name`() {
            converters.fromStatus(TransactionStatus.ACTIVE) shouldBe "ACTIVE"
        }

        @Test
        fun `fromStatus converts COMPLETED to its name`() {
            converters.fromStatus(TransactionStatus.COMPLETED) shouldBe "COMPLETED"
        }

        @Test
        fun `fromStatus converts FAILED to its name`() {
            converters.fromStatus(TransactionStatus.FAILED) shouldBe "FAILED"
        }

        @Test
        fun `toStatus converts ACTIVE string back`() {
            converters.toStatus("ACTIVE") shouldBe TransactionStatus.ACTIVE
        }

        @Test
        fun `toStatus converts COMPLETED string back`() {
            converters.toStatus("COMPLETED") shouldBe TransactionStatus.COMPLETED
        }

        @Test
        fun `toStatus converts FAILED string back`() {
            converters.toStatus("FAILED") shouldBe TransactionStatus.FAILED
        }

        @Test
        fun `toStatus defaults to FAILED for unknown values`() {
            converters.toStatus("UNKNOWN_STATUS") shouldBe TransactionStatus.FAILED
        }

        @Test
        fun `round-trips all status values`() {
            TransactionStatus.entries.forEach { status ->
                val serialized = converters.fromStatus(status)
                val deserialized = converters.toStatus(serialized)
                deserialized shouldBe status
            }
        }
    }

    @Nested
    inner class `UUID conversion` {

        @Test
        fun `fromUUID converts to string representation`() {
            val uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

            converters.fromUUID(uuid) shouldBe "550e8400-e29b-41d4-a716-446655440000"
        }

        @Test
        fun `fromUUID returns null for null input`() {
            converters.fromUUID(null).shouldBeNull()
        }

        @Test
        fun `toUUID converts string back to UUID`() {
            val result = converters.toUUID("550e8400-e29b-41d4-a716-446655440000")

            result shouldBe UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        }

        @Test
        fun `toUUID returns null for null input`() {
            converters.toUUID(null).shouldBeNull()
        }

        @Test
        fun `round-trips UUID through string and back`() {
            val original = UUID.randomUUID()

            val str = converters.fromUUID(original)
            val result = converters.toUUID(str)

            result shouldBe original
        }
    }

    @Nested
    inner class `Headers` {

        @Test
        fun `fromHeaders serializes headers map to JSON`() {
            val headers = mapOf(
                "Content-Type" to listOf("application/json"),
                "Accept" to listOf("text/html", "application/xhtml+xml"),
            )

            val result = converters.fromHeaders(headers)

            val deserialized = converters.toHeaders(result)
            deserialized shouldBe headers
        }

        @Test
        fun `fromHeaders returns null for null input`() {
            converters.fromHeaders(null).shouldBeNull()
        }

        @Test
        fun `toHeaders returns null for null input`() {
            converters.toHeaders(null).shouldBeNull()
        }

        @Test
        fun `handles empty headers map`() {
            val headers = emptyMap<String, List<String>>()

            val json = converters.fromHeaders(headers)
            val result = converters.toHeaders(json)

            result shouldBe emptyMap()
        }

        @Test
        fun `handles headers with empty value lists`() {
            val headers = mapOf("X-Custom" to emptyList<String>())

            val json = converters.fromHeaders(headers)
            val result = converters.toHeaders(json)

            result shouldBe headers
        }

        @Test
        fun `handles headers with multiple values`() {
            val headers = mapOf(
                "Set-Cookie" to listOf("session=abc", "lang=en", "theme=dark"),
            )

            val json = converters.fromHeaders(headers)
            val result = converters.toHeaders(json)

            result shouldBe headers
        }
    }

    @Nested
    inner class `Extensions` {

        @Test
        fun `fromExtensions serializes to JSON`() {
            val ext = mapOf("key1" to "value1", "key2" to "value2")

            val json = converters.fromExtensions(ext)
            val result = converters.toExtensions(json)

            result shouldBe ext
        }

        @Test
        fun `fromExtensions returns null for null input`() {
            converters.fromExtensions(null).shouldBeNull()
        }

        @Test
        fun `toExtensions returns null for null input`() {
            converters.toExtensions(null).shouldBeNull()
        }

        @Test
        fun `handles empty extensions map`() {
            val json = converters.fromExtensions(emptyMap())
            val result = converters.toExtensions(json)

            result shouldBe emptyMap()
        }
    }

    @Nested
    inner class `StringList` {

        @Test
        fun `fromStringList serializes to JSON`() {
            val list = listOf("alpha", "beta", "gamma")

            val json = converters.fromStringList(list)
            val result = converters.toStringList(json)

            result shouldBe list
        }

        @Test
        fun `fromStringList returns null for null input`() {
            converters.fromStringList(null).shouldBeNull()
        }

        @Test
        fun `toStringList returns null for null input`() {
            converters.toStringList(null).shouldBeNull()
        }

        @Test
        fun `handles empty list`() {
            val json = converters.fromStringList(emptyList())
            val result = converters.toStringList(json)

            result shouldBe emptyList()
        }

        @Test
        fun `handles list with single item`() {
            val list = listOf("only")

            val json = converters.fromStringList(list)
            val result = converters.toStringList(json)

            result shouldBe list
        }

        @Test
        fun `handles strings with special characters`() {
            val list = listOf("hello \"world\"", "foo\\bar", "new\nline")

            val json = converters.fromStringList(list)
            val result = converters.toStringList(json)

            result shouldBe list
        }
    }
}
