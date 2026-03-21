package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DefaultParserRegistryTest {

    private lateinit var registry: DefaultParserRegistry

    @BeforeEach
    fun setUp() {
        registry = DefaultParserRegistry()
    }

    @Nested
    inner class Register {

        @Test
        fun `should register parser and maintain priority ordering`() {
            val lowPriority = createMockParser(priority = 10, canParse = true)
            val highPriority = createMockParser(priority = 100, canParse = true)

            val highParsedBody = ParsedBody("high", ContentType.JSON, isValid = true)
            every { highPriority.parse(any()) } returns highParsedBody

            registry.register(lowPriority)
            registry.register(highPriority)

            val result = registry.parseBody(null, "test".toByteArray())
            result shouldBe highParsedBody
        }

        @Test
        fun `should prefer higher priority parser when multiple can parse`() {
            val low = createMockParser(priority = 50, canParse = true)
            val mid = createMockParser(priority = 150, canParse = true)
            val high = createMockParser(priority = 250, canParse = true)

            val highBody = ParsedBody("high-priority", ContentType.JSON, isValid = true)
            every { high.parse(any()) } returns highBody

            registry.register(low)
            registry.register(high)
            registry.register(mid)

            val result = registry.parseBody(null, "body".toByteArray())
            result shouldBe highBody
            verify(exactly = 0) { low.parse(any()) }
            verify(exactly = 0) { mid.parse(any()) }
        }
    }

    @Nested
    inner class ParseBody {

        @Test
        fun `should delegate to matching parser`() {
            val parser = createMockParser(priority = 100, canParse = true)
            val expected = ParsedBody("formatted", ContentType.JSON, isValid = true)
            every { parser.parse(any()) } returns expected

            registry.register(parser)

            val result = registry.parseBody("application/json", """{"key": "value"}""".toByteArray())
            result shouldBe expected
        }

        @Test
        fun `empty body should return UNKNOWN content type`() {
            val result = registry.parseBody("application/json", byteArrayOf())

            result.contentType shouldBe ContentType.UNKNOWN
            result.formatted shouldBe ""
            result.isValid shouldBe true
        }

        @Test
        fun `should fallback to plain text for text-like content without parser`() {
            val textBytes = "Hello, World!".toByteArray()
            val result = registry.parseBody(null, textBytes)

            result.contentType shouldBe ContentType.PLAIN_TEXT
            result.formatted shouldBe "Hello, World!"
            result.isValid shouldBe true
        }

        @Test
        fun `should fallback to binary for non-text content without parser`() {
            // Create bytes with lots of non-printable characters (>10% threshold)
            val binaryBytes = ByteArray(100) { 0x01 }
            val result = registry.parseBody(null, binaryBytes)

            result.contentType shouldBe ContentType.BINARY
            result.isValid shouldBe true
        }

        @Test
        fun `binary fallback should include hex preview`() {
            val binaryBytes = ByteArray(100) { 0x01 }
            val result = registry.parseBody(null, binaryBytes)

            result.formatted shouldContain "01"
        }

        @Test
        fun `binary fallback should include size metadata`() {
            val binaryBytes = ByteArray(100) { 0x01 }
            val result = registry.parseBody(null, binaryBytes)

            result.metadata["size"] shouldBe "100"
        }

        @Test
        fun `should skip parser that cannot parse`() {
            val cantParse = createMockParser(priority = 200, canParse = false)
            val canParse = createMockParser(priority = 100, canParse = true)
            val expected = ParsedBody("parsed", ContentType.XML, isValid = true)
            every { canParse.parse(any()) } returns expected

            registry.register(cantParse)
            registry.register(canParse)

            val result = registry.parseBody(null, "<xml/>".toByteArray())
            result shouldBe expected
            verify(exactly = 0) { cantParse.parse(any()) }
        }
    }

    @Nested
    inner class DetectContentTypeFromHeader {

        @Test
        fun `should detect JSON from content type header`() {
            registry.detectContentType("application/json", byteArrayOf()) shouldBe ContentType.JSON
        }

        @Test
        fun `should detect JSON with charset`() {
            registry.detectContentType("application/json; charset=utf-8", byteArrayOf()) shouldBe ContentType.JSON
        }

        @ParameterizedTest
        @ValueSource(strings = ["application/xml", "text/xml"])
        fun `should detect XML from content type header`(mime: String) {
            registry.detectContentType(mime, byteArrayOf()) shouldBe ContentType.XML
        }

        @Test
        fun `should detect HTML from content type header`() {
            registry.detectContentType("text/html", byteArrayOf()) shouldBe ContentType.HTML
        }

        @Test
        fun `should detect form data from content type header`() {
            registry.detectContentType(
                "application/x-www-form-urlencoded",
                byteArrayOf(),
            ) shouldBe ContentType.FORM_DATA
        }

        @Test
        fun `should detect multipart from content type header`() {
            registry.detectContentType(
                "multipart/form-data; boundary=---abc",
                byteArrayOf(),
            ) shouldBe ContentType.MULTIPART
        }

        @ParameterizedTest
        @ValueSource(strings = ["application/protobuf", "application/grpc", "application/x-proto+proto"])
        fun `should detect protobuf from content type header`(mime: String) {
            registry.detectContentType(mime, byteArrayOf()) shouldBe ContentType.PROTOBUF
        }

        @Test
        fun `should detect PDF from content type header`() {
            registry.detectContentType("application/pdf", byteArrayOf()) shouldBe ContentType.PDF
        }

        @Test
        fun `should detect PNG image from content type header`() {
            registry.detectContentType("image/png", byteArrayOf()) shouldBe ContentType.IMAGE_PNG
        }

        @ParameterizedTest
        @ValueSource(strings = ["image/jpeg", "image/jpg"])
        fun `should detect JPEG image from content type header`(mime: String) {
            registry.detectContentType(mime, byteArrayOf()) shouldBe ContentType.IMAGE_JPEG
        }

        @Test
        fun `should detect GIF image from content type header`() {
            registry.detectContentType("image/gif", byteArrayOf()) shouldBe ContentType.IMAGE_GIF
        }

        @Test
        fun `should detect WebP image from content type header`() {
            registry.detectContentType("image/webp", byteArrayOf()) shouldBe ContentType.IMAGE_WEBP
        }

        @Test
        fun `should detect SVG content type as XML due to xml suffix`() {
            // image/svg+xml contains "xml" which matches the XML branch first in detectFromHeader
            registry.detectContentType("image/svg+xml", byteArrayOf()) shouldBe ContentType.XML
        }

        @Test
        fun `should detect SVG image from image svg content type without xml`() {
            // image/svg (without +xml) matches image/svg check
            registry.detectContentType("image/svg", byteArrayOf()) shouldBe ContentType.IMAGE_SVG
        }

        @Test
        fun `should detect other image types`() {
            registry.detectContentType("image/tiff", byteArrayOf()) shouldBe ContentType.IMAGE_OTHER
        }

        @Test
        fun `should detect plain text from text content type`() {
            registry.detectContentType("text/plain", byteArrayOf()) shouldBe ContentType.PLAIN_TEXT
        }

        @Test
        fun `should detect json suffix patterns`() {
            registry.detectContentType("application/vnd.api+json", byteArrayOf()) shouldBe ContentType.JSON
        }

        @Test
        fun `should detect xml suffix patterns`() {
            registry.detectContentType("application/atom+xml", byteArrayOf()) shouldBe ContentType.XML
        }

        @Test
        fun `null header with empty body should return UNKNOWN`() {
            registry.detectContentType(null, byteArrayOf()) shouldBe ContentType.UNKNOWN
        }
    }

    @Nested
    inner class DetectContentTypeFromContent {

        @Test
        fun `should detect JSON from object content`() {
            val body = """{"key": "value"}"""
            registry.detectContentType(null, body) shouldBe ContentType.JSON
        }

        @Test
        fun `should detect JSON from array content`() {
            val body = """[1, 2, 3]"""
            registry.detectContentType(null, body) shouldBe ContentType.JSON
        }

        @Test
        fun `should detect XML from xml declaration`() {
            val body = """<?xml version="1.0"?><root/>"""
            registry.detectContentType(null, body) shouldBe ContentType.XML
        }

        @Test
        fun `should detect XML from tag content`() {
            val body = "<root><child/></root>"
            registry.detectContentType(null, body) shouldBe ContentType.XML
        }

        @Test
        fun `should detect HTML from doctype`() {
            val body = "<!DOCTYPE html><html></html>"
            registry.detectContentType(null, body) shouldBe ContentType.HTML
        }

        @Test
        fun `should detect HTML from html tag`() {
            val body = "<html><body>Hello</body></html>"
            registry.detectContentType(null, body) shouldBe ContentType.HTML
        }

        @Test
        fun `should detect form data from key-value pairs`() {
            val body = "name=John&age=30"
            registry.detectContentType(null, body) shouldBe ContentType.FORM_DATA
        }

        @Test
        fun `should return PLAIN_TEXT for regular text`() {
            val body = "Hello, World!"
            registry.detectContentType(null, body) shouldBe ContentType.PLAIN_TEXT
        }

        @Test
        fun `null body should return UNKNOWN`() {
            registry.detectContentType(null, null as String?) shouldBe ContentType.UNKNOWN
        }
    }

    @Nested
    inner class DetectContentTypeByteArrayFromContent {

        @Test
        fun `should detect JSON from body bytes when no header`() {
            val body = """{"key": "value"}""".toByteArray()
            registry.detectContentType(null, body) shouldBe ContentType.JSON
        }

        @Test
        fun `should detect BINARY for non-text bytes`() {
            val binaryBytes = ByteArray(200) { 0x01 }
            registry.detectContentType(null, binaryBytes) shouldBe ContentType.BINARY
        }
    }

    @Nested
    inner class IsLikelyText {

        @Test
        fun `body with only printable characters should be text`() {
            val textBytes = "Hello, World! 123 \n\t".toByteArray()
            val result = registry.parseBody(null, textBytes)
            result.contentType shouldBe ContentType.PLAIN_TEXT
        }

        @Test
        fun `body with more than 10 percent non-printable should be binary`() {
            // Create a body that is exactly at the boundary: 10 non-printable out of 100
            val body = ByteArray(100) { if (it < 10) 0x01 else 0x41 }
            // 10/100 = 0.10 which is NOT < 0.1, so should be binary
            val result = registry.parseBody(null, body)
            result.contentType shouldBe ContentType.BINARY
        }

        @Test
        fun `body with less than 10 percent non-printable should be text`() {
            // 9 non-printable out of 100 = 9% < 10%
            val body = ByteArray(100) { if (it < 9) 0x01 else 0x41 }
            val result = registry.parseBody(null, body)
            result.contentType shouldBe ContentType.PLAIN_TEXT
        }
    }

    @Nested
    inner class ExtractMultipartBoundary {

        @Test
        fun `should extract boundary from content type`() {
            val contentType = "multipart/form-data; boundary=----WebKitFormBoundary"
            registry.extractMultipartBoundary(contentType) shouldBe "----WebKitFormBoundary"
        }

        @Test
        fun `should extract quoted boundary`() {
            val contentType = """multipart/form-data; boundary="----abc123" """
            registry.extractMultipartBoundary(contentType) shouldBe "----abc123"
        }

        @Test
        fun `should return null when no boundary`() {
            val contentType = "application/json"
            registry.extractMultipartBoundary(contentType) shouldBe null
        }
    }

    private fun createMockParser(
        priority: Int,
        canParse: Boolean,
    ): BodyParser {
        val parser = mockk<BodyParser>(relaxed = true)
        every { parser.priority } returns priority
        every { parser.canParse(any(), any()) } returns canParse
        return parser
    }
}
