package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class TransactionSummaryTest {

    private val testId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    private fun defaultSummary() = TransactionSummary(
        id = testId,
        method = "GET",
        host = "api.example.com",
        path = "/users",
        code = 200,
        tookMs = 150L,
        hasRequestBody = false,
        hasResponseBody = true,
        status = TransactionStatus.COMPLETED,
        timestamp = 1_700_000_000_000L,
    )

    @Nested
    inner class Construction {

        @Test
        fun `constructs with all required fields`() {
            val summary = defaultSummary()

            summary.id shouldBe testId
            summary.method shouldBe "GET"
            summary.host shouldBe "api.example.com"
            summary.path shouldBe "/users"
            summary.code shouldBe 200
            summary.tookMs shouldBe 150L
            summary.hasRequestBody shouldBe false
            summary.hasResponseBody shouldBe true
            summary.status shouldBe TransactionStatus.COMPLETED
            summary.timestamp shouldBe 1_700_000_000_000L
        }

        @Test
        fun `constructs with nullable code`() {
            val summary = defaultSummary().copy(code = null)

            summary.code shouldBe null
        }

        @Test
        fun `constructs with nullable tookMs`() {
            val summary = defaultSummary().copy(tookMs = null)

            summary.tookMs shouldBe null
        }
    }

    @Nested
    inner class Defaults {

        @Test
        fun `isMocked defaults to false`() {
            val summary = defaultSummary()

            summary.isMocked shouldBe false
        }

        @Test
        fun `isMocked can be set to true`() {
            val summary = defaultSummary().copy(isMocked = true)

            summary.isMocked shouldBe true
        }
    }

    @Nested
    inner class CopyBehavior {

        @Test
        fun `copy produces a distinct instance`() {
            val original = defaultSummary()
            val copied = original.copy(method = "POST")

            (original !== copied) shouldBe true
            original.method shouldBe "GET"
            copied.method shouldBe "POST"
        }

        @Test
        fun `copy preserves unchanged fields`() {
            val original = defaultSummary()
            val copied = original.copy(code = 404)

            copied.id shouldBe original.id
            copied.method shouldBe original.method
            copied.host shouldBe original.host
            copied.path shouldBe original.path
            copied.tookMs shouldBe original.tookMs
            copied.code shouldBe 404
        }
    }

    @Nested
    inner class EqualityAndHashCode {

        @Test
        fun `equal instances have the same hashCode`() {
            val s1 = defaultSummary()
            val s2 = defaultSummary()

            s1 shouldBe s2
            s1.hashCode() shouldBe s2.hashCode()
        }

        @Test
        fun `different method makes instances unequal`() {
            val s1 = defaultSummary()
            val s2 = defaultSummary().copy(method = "POST")

            s1 shouldNotBe s2
        }

        @Test
        fun `different isMocked makes instances unequal`() {
            val s1 = defaultSummary()
            val s2 = defaultSummary().copy(isMocked = true)

            s1 shouldNotBe s2
        }
    }
}
