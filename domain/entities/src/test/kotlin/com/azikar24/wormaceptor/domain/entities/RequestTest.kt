package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RequestTest {

    @Nested
    inner class Construction {

        @Test
        fun `constructs with all required fields`() {
            val request = Request(
                url = "https://api.example.com/users",
                method = "GET",
                headers = mapOf("Accept" to listOf("application/json")),
                bodyRef = null,
            )

            request.url shouldBe "https://api.example.com/users"
            request.method shouldBe "GET"
            request.headers shouldBe mapOf("Accept" to listOf("application/json"))
            request.bodyRef shouldBe null
        }

        @Test
        fun `constructs with body reference and size`() {
            val request = Request(
                url = "https://api.example.com/upload",
                method = "POST",
                headers = mapOf("Content-Type" to listOf("multipart/form-data")),
                bodyRef = "blob_001",
                bodySize = 4096,
            )

            request.bodyRef shouldBe "blob_001"
            request.bodySize shouldBe 4096
        }
    }

    @Nested
    inner class Defaults {

        @Test
        fun `bodySize defaults to 0`() {
            val request = Request(
                url = "https://example.com",
                method = "GET",
                headers = emptyMap(),
                bodyRef = null,
            )

            request.bodySize shouldBe 0
        }
    }

    @Nested
    inner class EqualityAndHashCode {

        @Test
        fun `equal instances have the same hashCode`() {
            val r1 = Request("https://example.com", "GET", emptyMap(), null, 0)
            val r2 = Request("https://example.com", "GET", emptyMap(), null, 0)

            r1 shouldBe r2
            r1.hashCode() shouldBe r2.hashCode()
        }

        @Test
        fun `different url makes instances unequal`() {
            val r1 = Request("https://a.com", "GET", emptyMap(), null)
            val r2 = Request("https://b.com", "GET", emptyMap(), null)

            r1 shouldNotBe r2
        }

        @Test
        fun `different headers makes instances unequal`() {
            val r1 = Request("https://a.com", "GET", mapOf("X" to listOf("1")), null)
            val r2 = Request("https://a.com", "GET", mapOf("X" to listOf("2")), null)

            r1 shouldNotBe r2
        }
    }

    @Nested
    inner class CopyBehavior {

        @Test
        fun `copy can change method`() {
            val original = Request("https://example.com", "GET", emptyMap(), null)
            val copied = original.copy(method = "POST")

            copied.method shouldBe "POST"
            copied.url shouldBe original.url
        }
    }

    @Nested
    inner class MultiValueHeaders {

        @Test
        fun `supports multi-value headers`() {
            val request = Request(
                url = "https://example.com",
                method = "GET",
                headers = mapOf(
                    "Accept" to listOf("application/json", "text/html"),
                    "Cache-Control" to listOf("no-cache"),
                ),
                bodyRef = null,
            )

            request.headers["Accept"]?.size shouldBe 2
            request.headers["Cache-Control"]?.size shouldBe 1
        }
    }
}
