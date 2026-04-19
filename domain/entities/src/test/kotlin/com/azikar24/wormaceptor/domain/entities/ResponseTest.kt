package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ResponseTest {

    @Nested
    inner class Construction {

        @Test
        fun `constructs with required fields`() {
            val response = Response(
                code = 200,
                message = "OK",
                headers = mapOf("Content-Type" to listOf("application/json")),
                bodyRef = "blob_001",
            )

            response.code shouldBe 200
            response.message shouldBe "OK"
            response.headers shouldBe mapOf("Content-Type" to listOf("application/json"))
            response.bodyRef shouldBe "blob_001"
        }

        @Test
        fun `constructs with all optional fields`() {
            val response = Response(
                code = 500,
                message = "Internal Server Error",
                headers = emptyMap(),
                bodyRef = null,
                error = "Connection reset",
                protocol = "h2",
                tlsVersion = "TLSv1.3",
                bodySize = 2048,
            )

            response.error shouldBe "Connection reset"
            response.protocol shouldBe "h2"
            response.tlsVersion shouldBe "TLSv1.3"
            response.bodySize shouldBe 2048
        }
    }

    @Nested
    inner class Defaults {

        @Test
        fun `error defaults to null`() {
            val response = Response(code = 200, message = "OK", headers = emptyMap(), bodyRef = null)

            response.error shouldBe null
        }

        @Test
        fun `protocol defaults to null`() {
            val response = Response(code = 200, message = "OK", headers = emptyMap(), bodyRef = null)

            response.protocol shouldBe null
        }

        @Test
        fun `tlsVersion defaults to null`() {
            val response = Response(code = 200, message = "OK", headers = emptyMap(), bodyRef = null)

            response.tlsVersion shouldBe null
        }

        @Test
        fun `bodySize defaults to 0`() {
            val response = Response(code = 200, message = "OK", headers = emptyMap(), bodyRef = null)

            response.bodySize shouldBe 0
        }
    }

    @Nested
    inner class EqualityAndHashCode {

        @Test
        fun `equal instances have the same hashCode`() {
            val r1 = Response(200, "OK", emptyMap(), null, null, "h2", "TLSv1.3", 0)
            val r2 = Response(200, "OK", emptyMap(), null, null, "h2", "TLSv1.3", 0)

            r1 shouldBe r2
            r1.hashCode() shouldBe r2.hashCode()
        }

        @Test
        fun `different code makes instances unequal`() {
            val r1 = Response(200, "OK", emptyMap(), null)
            val r2 = Response(404, "Not Found", emptyMap(), null)

            r1 shouldNotBe r2
        }
    }

    @Nested
    inner class CopyBehavior {

        @Test
        fun `copy can add error to response`() {
            val original = Response(code = 200, message = "OK", headers = emptyMap(), bodyRef = null)
            val withError = original.copy(error = "timeout")

            withError.error shouldBe "timeout"
            withError.code shouldBe 200
        }
    }
}
