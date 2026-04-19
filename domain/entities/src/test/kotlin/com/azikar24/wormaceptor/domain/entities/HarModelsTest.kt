package com.azikar24.wormaceptor.domain.entities

import com.azikar24.wormaceptor.domain.entities.har.HarContent
import com.azikar24.wormaceptor.domain.entities.har.HarCookie
import com.azikar24.wormaceptor.domain.entities.har.HarCreator
import com.azikar24.wormaceptor.domain.entities.har.HarEntry
import com.azikar24.wormaceptor.domain.entities.har.HarHeader
import com.azikar24.wormaceptor.domain.entities.har.HarLog
import com.azikar24.wormaceptor.domain.entities.har.HarPostData
import com.azikar24.wormaceptor.domain.entities.har.HarPostParam
import com.azikar24.wormaceptor.domain.entities.har.HarQueryParam
import com.azikar24.wormaceptor.domain.entities.har.HarRequest
import com.azikar24.wormaceptor.domain.entities.har.HarResponse
import com.azikar24.wormaceptor.domain.entities.har.HarTimings
import com.azikar24.wormaceptor.domain.entities.har.HarWebSocketFrame
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HarModelsTest {

    private fun defaultCreator() = HarCreator(name = "WormaCeptor", version = "2.3.0")

    private fun defaultRequest() = HarRequest(
        method = "GET",
        url = "https://api.example.com/users",
        httpVersion = "HTTP/1.1",
        cookies = emptyList(),
        headers = listOf(HarHeader("Accept", "application/json")),
        queryString = listOf(HarQueryParam("page", "1")),
        headersSize = 50,
        bodySize = 0,
    )

    private fun defaultResponse() = HarResponse(
        status = 200,
        statusText = "OK",
        httpVersion = "HTTP/1.1",
        cookies = emptyList(),
        headers = listOf(HarHeader("Content-Type", "application/json")),
        content = HarContent(size = 256, mimeType = "application/json", text = """{"users":[]}"""),
        redirectURL = "",
        headersSize = 80,
        bodySize = 256,
    )

    private fun defaultTimings() = HarTimings()

    private fun defaultEntry() = HarEntry(
        startedDateTime = "2024-01-15T10:30:00.000Z",
        time = 150,
        request = defaultRequest(),
        response = defaultResponse(),
        timings = defaultTimings(),
    )

    @Nested
    inner class HarLogConstruction {

        @Test
        fun `constructs with default version`() {
            val log = HarLog(
                creator = defaultCreator(),
                entries = listOf(defaultEntry()),
            )

            log.version shouldBe "1.2"
        }

        @Test
        fun `constructs with custom version`() {
            val log = HarLog(
                version = "1.3",
                creator = defaultCreator(),
                entries = emptyList(),
            )

            log.version shouldBe "1.3"
        }

        @Test
        fun `constructs with empty entries`() {
            val log = HarLog(creator = defaultCreator(), entries = emptyList())

            log.entries.size shouldBe 0
        }

        @Test
        fun `constructs with multiple entries`() {
            val log = HarLog(
                creator = defaultCreator(),
                entries = listOf(defaultEntry(), defaultEntry()),
            )

            log.entries.size shouldBe 2
        }
    }

    @Nested
    inner class HarCreatorConstruction {

        @Test
        fun `stores name and version`() {
            val creator = HarCreator(name = "WormaCeptor", version = "2.3.0")

            creator.name shouldBe "WormaCeptor"
            creator.version shouldBe "2.3.0"
        }

        @Test
        fun `equality works`() {
            HarCreator("A", "1.0") shouldBe HarCreator("A", "1.0")
            HarCreator("A", "1.0") shouldNotBe HarCreator("B", "1.0")
        }
    }

    @Nested
    inner class HarEntryDefaults {

        @Test
        fun `serverIPAddress defaults to null`() {
            val entry = defaultEntry()

            entry.serverIPAddress shouldBe null
        }

        @Test
        fun `connection defaults to null`() {
            val entry = defaultEntry()

            entry.connection shouldBe null
        }

        @Test
        fun `webSocketFrames defaults to null`() {
            val entry = defaultEntry()

            entry.webSocketFrames shouldBe null
        }

        @Test
        fun `tlsVersion defaults to null`() {
            val entry = defaultEntry()

            entry.tlsVersion shouldBe null
        }

        @Test
        fun `cipherSuite defaults to null`() {
            val entry = defaultEntry()

            entry.cipherSuite shouldBe null
        }
    }

    @Nested
    inner class HarEntryCustomExtensions {

        @Test
        fun `constructs with WebSocket frames`() {
            val frame = HarWebSocketFrame(
                type = "text",
                direction = "send",
                data = "hello",
                timestamp = "2024-01-15T10:30:01.000Z",
                size = 5,
            )

            val entry = defaultEntry().copy(webSocketFrames = listOf(frame))

            entry.webSocketFrames shouldNotBe null
            entry.webSocketFrames?.size shouldBe 1
            entry.webSocketFrames?.first()?.data shouldBe "hello"
        }

        @Test
        fun `constructs with TLS info`() {
            val entry = defaultEntry().copy(
                tlsVersion = "TLSv1.3",
                cipherSuite = "TLS_AES_256_GCM_SHA384",
            )

            entry.tlsVersion shouldBe "TLSv1.3"
            entry.cipherSuite shouldBe "TLS_AES_256_GCM_SHA384"
        }
    }

    @Nested
    inner class HarTimingsDefaults {

        @Test
        fun `all timing fields default to -1`() {
            val timings = HarTimings()

            timings.blocked shouldBe -1
            timings.dns shouldBe -1
            timings.connect shouldBe -1
            timings.ssl shouldBe -1
            timings.send shouldBe -1
            timings.wait shouldBe -1
            timings.receive shouldBe -1
        }

        @Test
        fun `constructs with custom values`() {
            val timings = HarTimings(
                blocked = 0,
                dns = 10,
                connect = 20,
                ssl = 15,
                send = 5,
                wait = 80,
                receive = 20,
            )

            timings.blocked shouldBe 0
            timings.dns shouldBe 10
            timings.connect shouldBe 20
            timings.ssl shouldBe 15
            timings.send shouldBe 5
            timings.wait shouldBe 80
            timings.receive shouldBe 20
        }
    }

    @Nested
    inner class HarRequestConstruction {

        @Test
        fun `postData defaults to null`() {
            val request = defaultRequest()

            request.postData shouldBe null
        }

        @Test
        fun `constructs with postData`() {
            val request = defaultRequest().copy(
                postData = HarPostData(
                    mimeType = "application/json",
                    text = """{"name":"test"}""",
                ),
            )

            request.postData shouldNotBe null
            request.postData?.mimeType shouldBe "application/json"
            request.postData?.text shouldBe """{"name":"test"}"""
        }
    }

    @Nested
    inner class HarCookieDefaults {

        @Test
        fun `optional fields default to null`() {
            val cookie = HarCookie(name = "session", value = "abc123")

            cookie.path shouldBe null
            cookie.domain shouldBe null
            cookie.expires shouldBe null
            cookie.httpOnly shouldBe null
            cookie.secure shouldBe null
        }

        @Test
        fun `constructs with all optional fields`() {
            val cookie = HarCookie(
                name = "session",
                value = "abc123",
                path = "/",
                domain = ".example.com",
                expires = "2025-12-31T23:59:59Z",
                httpOnly = true,
                secure = true,
            )

            cookie.path shouldBe "/"
            cookie.domain shouldBe ".example.com"
            cookie.httpOnly shouldBe true
            cookie.secure shouldBe true
        }
    }

    @Nested
    inner class HarContentConstruction {

        @Test
        fun `optional fields default to null`() {
            val content = HarContent(size = 100, mimeType = "text/plain")

            content.text shouldBe null
            content.encoding shouldBe null
        }

        @Test
        fun `constructs with text and encoding`() {
            val content = HarContent(
                size = 200,
                mimeType = "text/html",
                text = "<html></html>",
                encoding = "utf-8",
            )

            content.text shouldBe "<html></html>"
            content.encoding shouldBe "utf-8"
        }
    }

    @Nested
    inner class HarPostDataConstruction {

        @Test
        fun `optional fields default to null`() {
            val postData = HarPostData(mimeType = "application/json")

            postData.text shouldBe null
            postData.params shouldBe null
        }

        @Test
        fun `constructs with params`() {
            val postData = HarPostData(
                mimeType = "application/x-www-form-urlencoded",
                params = listOf(
                    HarPostParam(name = "username", value = "admin"),
                    HarPostParam(name = "password", value = "secret"),
                ),
            )

            postData.params?.size shouldBe 2
        }
    }

    @Nested
    inner class HarPostParamDefaults {

        @Test
        fun `optional fields default to null`() {
            val param = HarPostParam(name = "file")

            param.value shouldBe null
            param.fileName shouldBe null
            param.contentType shouldBe null
        }

        @Test
        fun `constructs with all fields for file upload`() {
            val param = HarPostParam(
                name = "attachment",
                value = null,
                fileName = "document.pdf",
                contentType = "application/pdf",
            )

            param.fileName shouldBe "document.pdf"
            param.contentType shouldBe "application/pdf"
        }
    }

    @Nested
    inner class HarHeaderAndQueryParam {

        @Test
        fun `HarHeader stores name and value`() {
            val header = HarHeader(name = "Content-Type", value = "application/json")

            header.name shouldBe "Content-Type"
            header.value shouldBe "application/json"
        }

        @Test
        fun `HarQueryParam stores name and value`() {
            val param = HarQueryParam(name = "limit", value = "10")

            param.name shouldBe "limit"
            param.value shouldBe "10"
        }

        @Test
        fun `HarHeader equality works`() {
            HarHeader("A", "1") shouldBe HarHeader("A", "1")
            HarHeader("A", "1") shouldNotBe HarHeader("A", "2")
        }
    }

    @Nested
    inner class HarWebSocketFrameConstruction {

        @Test
        fun `stores all fields`() {
            val frame = HarWebSocketFrame(
                type = "binary",
                direction = "receive",
                data = "AQIDBA==",
                timestamp = "2024-01-15T10:30:01.500Z",
                size = 4,
            )

            frame.type shouldBe "binary"
            frame.direction shouldBe "receive"
            frame.data shouldBe "AQIDBA=="
            frame.timestamp shouldBe "2024-01-15T10:30:01.500Z"
            frame.size shouldBe 4
        }
    }
}
