package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class NetworkTransactionTest {

    private val testId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    private fun minimalRequest() = Request(
        url = "https://example.com/api",
        method = "GET",
        headers = emptyMap(),
        bodyRef = null,
    )

    private fun fullRequest() = Request(
        url = "https://api.example.com/users/42",
        method = "POST",
        headers = mapOf(
            "Content-Type" to listOf("application/json"),
            "Authorization" to listOf("Bearer token123"),
        ),
        bodyRef = "blob_req_001",
        bodySize = 512,
    )

    private fun fullResponse() = Response(
        code = 200,
        message = "OK",
        headers = mapOf("Content-Type" to listOf("application/json")),
        bodyRef = "blob_res_001",
        protocol = "h2",
        tlsVersion = "TLSv1.3",
        bodySize = 1024,
    )

    @Nested
    inner class Construction {

        @Test
        fun `minimal construction with only request`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.request.url shouldBe "https://example.com/api"
            tx.request.method shouldBe "GET"
        }

        @Test
        fun `full construction with all fields`() {
            val tx = NetworkTransaction(
                id = testId,
                timestamp = 1_700_000_000_000L,
                durationMs = 250L,
                status = TransactionStatus.COMPLETED,
                request = fullRequest(),
                response = fullResponse(),
                extensions = mapOf("tag" to "test"),
                isMocked = true,
            )

            tx.id shouldBe testId
            tx.timestamp shouldBe 1_700_000_000_000L
            tx.durationMs shouldBe 250L
            tx.status shouldBe TransactionStatus.COMPLETED
            tx.request.url shouldBe "https://api.example.com/users/42"
            tx.response shouldNotBe null
            tx.response?.code shouldBe 200
            tx.extensions shouldBe mapOf("tag" to "test")
            tx.isMocked shouldBe true
        }
    }

    @Nested
    inner class Defaults {

        @Test
        fun `id defaults to a random UUID`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.id.shouldBeInstanceOf<UUID>()
        }

        @Test
        fun `two instances get different default ids`() {
            val tx1 = NetworkTransaction(request = minimalRequest())
            val tx2 = NetworkTransaction(request = minimalRequest())

            tx1.id shouldNotBe tx2.id
        }

        @Test
        fun `timestamp defaults to current time`() {
            val before = System.currentTimeMillis()
            val tx = NetworkTransaction(request = minimalRequest())
            val after = System.currentTimeMillis()

            (tx.timestamp in before..after) shouldBe true
        }

        @Test
        fun `durationMs defaults to null`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.durationMs shouldBe null
        }

        @Test
        fun `status defaults to ACTIVE`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.status shouldBe TransactionStatus.ACTIVE
        }

        @Test
        fun `response defaults to null`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.response shouldBe null
        }

        @Test
        fun `extensions defaults to empty map`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.extensions shouldBe emptyMap()
        }

        @Test
        fun `isMocked defaults to false`() {
            val tx = NetworkTransaction(request = minimalRequest())

            tx.isMocked shouldBe false
        }
    }

    @Nested
    inner class CopyBehavior {

        @Test
        fun `copy preserves unchanged fields`() {
            val original = NetworkTransaction(
                id = testId,
                timestamp = 1_700_000_000_000L,
                request = fullRequest(),
                status = TransactionStatus.ACTIVE,
            )

            val copied = original.copy(status = TransactionStatus.COMPLETED)

            copied.id shouldBe testId
            copied.timestamp shouldBe 1_700_000_000_000L
            copied.request shouldBe original.request
            copied.status shouldBe TransactionStatus.COMPLETED
        }

        @Test
        fun `copy produces a distinct instance`() {
            val original = NetworkTransaction(
                id = testId,
                request = minimalRequest(),
            )

            val copied = original.copy(durationMs = 100L)

            (original !== copied) shouldBe true
            original.durationMs shouldBe null
            copied.durationMs shouldBe 100L
        }

        @Test
        fun `copy can add response to an active transaction`() {
            val original = NetworkTransaction(
                id = testId,
                request = minimalRequest(),
                status = TransactionStatus.ACTIVE,
            )

            val completed = original.copy(
                status = TransactionStatus.COMPLETED,
                response = fullResponse(),
                durationMs = 150L,
            )

            completed.response shouldNotBe null
            completed.durationMs shouldBe 150L
            completed.status shouldBe TransactionStatus.COMPLETED
            original.response shouldBe null
        }
    }

    @Nested
    inner class EqualityAndHashCode {

        @Test
        fun `equal instances have the same hashCode`() {
            val request = minimalRequest()
            val tx1 = NetworkTransaction(id = testId, timestamp = 1L, request = request)
            val tx2 = NetworkTransaction(id = testId, timestamp = 1L, request = request)

            tx1 shouldBe tx2
            tx1.hashCode() shouldBe tx2.hashCode()
        }

        @Test
        fun `different id makes instances unequal`() {
            val request = minimalRequest()
            val tx1 = NetworkTransaction(id = testId, timestamp = 1L, request = request)
            val tx2 = NetworkTransaction(
                id = UUID.fromString("660e8400-e29b-41d4-a716-446655440000"),
                timestamp = 1L,
                request = request,
            )

            tx1 shouldNotBe tx2
        }

        @Test
        fun `different status makes instances unequal`() {
            val request = minimalRequest()
            val tx1 = NetworkTransaction(id = testId, timestamp = 1L, request = request, status = TransactionStatus.ACTIVE)
            val tx2 = NetworkTransaction(id = testId, timestamp = 1L, request = request, status = TransactionStatus.COMPLETED)

            tx1 shouldNotBe tx2
        }
    }
}
