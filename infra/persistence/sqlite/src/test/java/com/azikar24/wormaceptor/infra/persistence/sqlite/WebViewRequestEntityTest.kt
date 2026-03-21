package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class WebViewRequestEntityTest {

    private fun fullRequest() = WebViewRequest(
        id = "req_001",
        url = "https://api.example.com/data",
        method = "POST",
        headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer token",
        ),
        timestamp = 1_700_000_000_000L,
        webViewId = "webview_42",
        resourceType = WebViewResourceType.XHR,
        isForMainFrame = true,
        hasGesture = true,
        isRedirect = false,
        statusCode = 200,
        responseHeaders = mapOf(
            "X-Request-Id" to "abc123",
            "Content-Length" to "1024",
        ),
        errorMessage = null,
        mimeType = "application/json",
        encoding = "utf-8",
        contentLength = 1024L,
        duration = 150L,
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly`() {
            val entity = WebViewRequestEntity.fromDomain(fullRequest())

            val domain = entity.toDomain()

            domain.id shouldBe "req_001"
            domain.url shouldBe "https://api.example.com/data"
            domain.method shouldBe "POST"
            domain.timestamp shouldBe 1_700_000_000_000L
            domain.webViewId shouldBe "webview_42"
            domain.resourceType shouldBe WebViewResourceType.XHR
            domain.isForMainFrame shouldBe true
            domain.hasGesture shouldBe true
            domain.isRedirect shouldBe false
            domain.statusCode shouldBe 200
            domain.errorMessage.shouldBeNull()
            domain.mimeType shouldBe "application/json"
            domain.encoding shouldBe "utf-8"
            domain.contentLength shouldBe 1024L
            domain.duration shouldBe 150L
        }

        @Test
        fun `deserializes request headers from JSON`() {
            val entity = WebViewRequestEntity.fromDomain(fullRequest())

            val domain = entity.toDomain()

            domain.headers shouldBe mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer token",
            )
        }

        @Test
        fun `deserializes response headers from JSON`() {
            val entity = WebViewRequestEntity.fromDomain(fullRequest())

            val domain = entity.toDomain()

            domain.responseHeaders shouldBe mapOf(
                "X-Request-Id" to "abc123",
                "Content-Length" to "1024",
            )
        }

        @Test
        fun `handles malformed headers JSON gracefully`() {
            val entity = WebViewRequestEntity(
                id = "bad",
                url = "https://example.com",
                method = "GET",
                headersJson = "invalid json",
                timestamp = 0L,
                webViewId = "wv1",
                resourceType = "DOCUMENT",
                isForMainFrame = false,
                hasGesture = false,
                isRedirect = false,
                statusCode = null,
                responseHeadersJson = "also invalid",
                errorMessage = null,
                mimeType = null,
                encoding = null,
                contentLength = null,
                duration = null,
            )

            val domain = entity.toDomain()

            domain.headers shouldBe emptyMap()
            domain.responseHeaders shouldBe emptyMap()
        }

        @Test
        fun `handles unknown resource type gracefully`() {
            val entity = WebViewRequestEntity(
                id = "unknown_type",
                url = "https://example.com",
                method = "GET",
                headersJson = "{}",
                timestamp = 0L,
                webViewId = "wv1",
                resourceType = "NONEXISTENT_TYPE",
                isForMainFrame = false,
                hasGesture = false,
                isRedirect = false,
                statusCode = null,
                responseHeadersJson = "{}",
                errorMessage = null,
                mimeType = null,
                encoding = null,
                contentLength = null,
                duration = null,
            )

            val domain = entity.toDomain()

            domain.resourceType shouldBe WebViewResourceType.UNKNOWN
        }

        @ParameterizedTest
        @EnumSource(WebViewResourceType::class)
        fun `maps all resource types`(type: WebViewResourceType) {
            val entity = WebViewRequestEntity.fromDomain(
                fullRequest().copy(resourceType = type),
            )

            val domain = entity.toDomain()

            domain.resourceType shouldBe type
        }

        @Test
        fun `handles null optional fields`() {
            val request = fullRequest().copy(
                statusCode = null,
                errorMessage = null,
                mimeType = null,
                encoding = null,
                contentLength = null,
                duration = null,
            )
            val entity = WebViewRequestEntity.fromDomain(request)

            val domain = entity.toDomain()

            domain.statusCode.shouldBeNull()
            domain.errorMessage.shouldBeNull()
            domain.mimeType.shouldBeNull()
            domain.encoding.shouldBeNull()
            domain.contentLength.shouldBeNull()
            domain.duration.shouldBeNull()
        }

        @Test
        fun `handles error message`() {
            val request = fullRequest().copy(
                statusCode = null,
                errorMessage = "Connection refused",
            )
            val entity = WebViewRequestEntity.fromDomain(request)

            val domain = entity.toDomain()

            domain.errorMessage shouldBe "Connection refused"
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all fields correctly`() {
            val domain = fullRequest()

            val entity = WebViewRequestEntity.fromDomain(domain)

            entity.id shouldBe "req_001"
            entity.url shouldBe "https://api.example.com/data"
            entity.method shouldBe "POST"
            entity.timestamp shouldBe 1_700_000_000_000L
            entity.webViewId shouldBe "webview_42"
            entity.resourceType shouldBe "XHR"
            entity.isForMainFrame shouldBe true
            entity.hasGesture shouldBe true
            entity.isRedirect shouldBe false
            entity.statusCode shouldBe 200
            entity.errorMessage.shouldBeNull()
            entity.mimeType shouldBe "application/json"
            entity.encoding shouldBe "utf-8"
            entity.contentLength shouldBe 1024L
            entity.duration shouldBe 150L
        }

        @Test
        fun `serializes headers to JSON`() {
            val entity = WebViewRequestEntity.fromDomain(fullRequest())

            // Verify by round-tripping
            val roundTripped = entity.toDomain()
            roundTripped.headers shouldBe fullRequest().headers
        }

        @Test
        fun `serializes response headers to JSON`() {
            val entity = WebViewRequestEntity.fromDomain(fullRequest())

            // Verify by round-tripping
            val roundTripped = entity.toDomain()
            roundTripped.responseHeaders shouldBe fullRequest().responseHeaders
        }

        @Test
        fun `handles empty headers`() {
            val domain = fullRequest().copy(headers = emptyMap())

            val entity = WebViewRequestEntity.fromDomain(domain)
            val roundTripped = entity.toDomain()

            roundTripped.headers shouldBe emptyMap()
        }

        @Test
        fun `handles empty response headers`() {
            val domain = fullRequest().copy(responseHeaders = emptyMap())

            val entity = WebViewRequestEntity.fromDomain(domain)
            val roundTripped = entity.toDomain()

            roundTripped.responseHeaders shouldBe emptyMap()
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all fields`() {
            val original = fullRequest()

            val entity = WebViewRequestEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips request with no response data`() {
            val original = fullRequest().copy(
                statusCode = null,
                responseHeaders = emptyMap(),
                errorMessage = null,
                mimeType = null,
                encoding = null,
                contentLength = null,
                duration = null,
            )

            val entity = WebViewRequestEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips request with error`() {
            val original = fullRequest().copy(
                statusCode = 500,
                errorMessage = "Internal Server Error",
            )

            val entity = WebViewRequestEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips all resource types`() {
            WebViewResourceType.entries.forEach { type ->
                val original = fullRequest().copy(resourceType = type)

                val entity = WebViewRequestEntity.fromDomain(original)
                val roundTripped = entity.toDomain()

                roundTripped.resourceType shouldBe type
            }
        }

        @Test
        fun `round-trips request with boolean flags`() {
            val original = fullRequest().copy(
                isForMainFrame = false,
                hasGesture = false,
                isRedirect = true,
            )

            val entity = WebViewRequestEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped.isForMainFrame shouldBe false
            roundTripped.hasGesture shouldBe false
            roundTripped.isRedirect shouldBe true
        }
    }
}
