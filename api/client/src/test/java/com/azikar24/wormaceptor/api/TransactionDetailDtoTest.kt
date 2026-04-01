package com.azikar24.wormaceptor.api

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TransactionDetailDtoTest {

    private fun createDto(
        id: String = "test-id",
        method: String = "GET",
        url: String = "https://example.com/api",
        host: String = "example.com",
        path: String = "/api",
        code: Int? = 200,
        duration: Long? = 150L,
        status: String = "COMPLETED",
        timestamp: Long = 1_700_000_000L,
        requestHeaders: Map<String, List<String>> = emptyMap(),
        requestBody: String? = null,
        requestSize: Long = 0L,
        responseHeaders: Map<String, List<String>> = emptyMap(),
        responseBody: String? = null,
        responseSize: Long = 0L,
        responseMessage: String? = "OK",
        protocol: String? = "HTTP/1.1",
        tlsVersion: String? = "TLSv1.3",
        error: String? = null,
        contentType: String? = "application/json",
        extensions: Map<String, String> = emptyMap(),
    ) = TransactionDetailDto(
        id = id,
        method = method,
        url = url,
        host = host,
        path = path,
        code = code,
        duration = duration,
        status = status,
        timestamp = timestamp,
        requestHeaders = requestHeaders,
        requestBody = requestBody,
        requestSize = requestSize,
        responseHeaders = responseHeaders,
        responseBody = responseBody,
        responseSize = responseSize,
        responseMessage = responseMessage,
        protocol = protocol,
        tlsVersion = tlsVersion,
        error = error,
        contentType = contentType,
        extensions = extensions,
    )

    @Nested
    inner class `construction` {

        @Test
        fun `stores all provided values`() {
            val requestHeaders = mapOf("Content-Type" to listOf("application/json"))
            val responseHeaders = mapOf("X-Request-Id" to listOf("abc-123"))
            val extensions = mapOf("custom" to "value")

            val dto = createDto(
                id = "uuid-1",
                method = "POST",
                url = "https://api.test.com/users",
                host = "api.test.com",
                path = "/users",
                code = 201,
                duration = 250L,
                status = "COMPLETED",
                timestamp = 1_700_000_000L,
                requestHeaders = requestHeaders,
                requestBody = """{"name":"John"}""",
                requestSize = 15L,
                responseHeaders = responseHeaders,
                responseBody = """{"id":"1"}""",
                responseSize = 10L,
                responseMessage = "Created",
                protocol = "h2",
                tlsVersion = "TLSv1.3",
                error = null,
                contentType = "application/json",
                extensions = extensions,
            )

            dto.id shouldBe "uuid-1"
            dto.method shouldBe "POST"
            dto.url shouldBe "https://api.test.com/users"
            dto.host shouldBe "api.test.com"
            dto.path shouldBe "/users"
            dto.code shouldBe 201
            dto.duration shouldBe 250L
            dto.status shouldBe "COMPLETED"
            dto.timestamp shouldBe 1_700_000_000L
            dto.requestHeaders shouldBe requestHeaders
            dto.requestBody shouldBe """{"name":"John"}"""
            dto.requestSize shouldBe 15L
            dto.responseHeaders shouldBe responseHeaders
            dto.responseBody shouldBe """{"id":"1"}"""
            dto.responseSize shouldBe 10L
            dto.responseMessage shouldBe "Created"
            dto.protocol shouldBe "h2"
            dto.tlsVersion shouldBe "TLSv1.3"
            dto.error shouldBe null
            dto.contentType shouldBe "application/json"
            dto.extensions shouldBe extensions
        }

        @Test
        fun `nullable fields accept null`() {
            val dto = createDto(
                code = null,
                duration = null,
                requestBody = null,
                responseBody = null,
                responseMessage = null,
                protocol = null,
                tlsVersion = null,
                error = null,
                contentType = null,
            )

            dto.code shouldBe null
            dto.duration shouldBe null
            dto.requestBody shouldBe null
            dto.responseBody shouldBe null
            dto.responseMessage shouldBe null
            dto.protocol shouldBe null
            dto.tlsVersion shouldBe null
            dto.error shouldBe null
            dto.contentType shouldBe null
        }

        @Test
        fun `extensions defaults to empty map`() {
            val dto = TransactionDetailDto(
                id = "id",
                method = "GET",
                url = "https://example.com",
                host = "example.com",
                path = "/",
                code = 200,
                duration = 100L,
                status = "COMPLETED",
                timestamp = 0L,
                requestHeaders = emptyMap(),
                requestBody = null,
                requestSize = 0L,
                responseHeaders = emptyMap(),
                responseBody = null,
                responseSize = 0L,
                responseMessage = "OK",
                protocol = null,
                tlsVersion = null,
                error = null,
                contentType = null,
            )

            dto.extensions shouldBe emptyMap()
        }
    }

    @Nested
    inner class `data class behaviour` {

        @Test
        fun `equals works for identical DTOs`() {
            val dto1 = createDto(id = "same")
            val dto2 = createDto(id = "same")

            dto1 shouldBe dto2
        }

        @Test
        fun `equals detects different values`() {
            val dto1 = createDto(id = "one")
            val dto2 = createDto(id = "two")

            dto1 shouldNotBe dto2
        }

        @Test
        fun `hashCode is consistent for equal objects`() {
            val dto1 = createDto()
            val dto2 = createDto()

            dto1.hashCode() shouldBe dto2.hashCode()
        }

        @Test
        fun `copy creates independent instance with overridden field`() {
            val original = createDto(code = 200)
            val copied = original.copy(code = 404)

            copied.code shouldBe 404
            original.code shouldBe 200
            copied.id shouldBe original.id
        }

        @Test
        fun `toString contains field values`() {
            val dto = createDto(id = "abc", method = "DELETE")
            val str = dto.toString()

            str.contains("abc") shouldBe true
            str.contains("DELETE") shouldBe true
        }
    }

    @Nested
    inner class `failed transaction` {

        @Test
        fun `represents a failed request with error and no response`() {
            val dto = createDto(
                code = null,
                duration = null,
                status = "FAILED",
                requestBody = """{"query":"test"}""",
                requestSize = 16L,
                responseBody = null,
                responseSize = 0L,
                responseMessage = null,
                error = "java.net.SocketTimeoutException: timeout",
            )

            dto.status shouldBe "FAILED"
            dto.error shouldBe "java.net.SocketTimeoutException: timeout"
            dto.code shouldBe null
            dto.responseBody shouldBe null
        }
    }
}
