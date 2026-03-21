package com.azikar24.wormaceptor.infra.parser.multipart

import com.azikar24.wormaceptor.domain.contracts.ContentType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class MultipartBodyParserTest {

    private val parser = MultipartBodyParser()

    @Nested
    inner class `supportedContentTypes` {

        @Test
        fun `contains all expected multipart MIME types`() {
            parser.supportedContentTypes shouldBe listOf(
                "multipart/form-data",
                "multipart/mixed",
                "multipart/related",
                "multipart/alternative",
            )
        }
    }

    @Nested
    inner class `priority` {

        @Test
        fun `is 300`() {
            parser.priority shouldBe 300
        }
    }

    @Nested
    inner class `canParse` {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "multipart/form-data",
                "multipart/mixed",
                "multipart/related",
                "multipart/alternative",
                "multipart/form-data; boundary=abc123",
            ],
        )
        fun `returns true for multipart content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for uppercase multipart content type`() {
            parser.canParse("MULTIPART/FORM-DATA", ByteArray(0)) shouldBe true
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/html",
                "application/x-www-form-urlencoded",
                "text/plain",
            ],
        )
        fun `returns false for non-multipart content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns false when content type is null`() {
            parser.canParse(null, ByteArray(0)) shouldBe false
        }
    }

    @Nested
    inner class `parse with explicit boundary` {

        @Test
        fun `parses single part with name`() {
            val data = "--boundary\r\nContent-Disposition: form-data; name=\"field1\"\r\n\r\nvalue1\r\n--boundary--"
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].name shouldBe "field1"
            result[0].body shouldBe "value1"
        }

        @Test
        fun `parses multiple parts`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"field1\"\r\n")
                append("\r\n")
                append("value1\r\n")
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"field2\"\r\n")
                append("\r\n")
                append("value2\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 2
            result[0].name shouldBe "field1"
            result[0].body shouldBe "value1"
            result[1].name shouldBe "field2"
            result[1].body shouldBe "value2"
        }

        @Test
        fun `extracts filename from Content-Disposition`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n")
                append("Content-Type: text/plain\r\n")
                append("\r\n")
                append("file content\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].name shouldBe "file"
            result[0].fileName shouldBe "test.txt"
            result[0].contentType shouldBe "text/plain"
            result[0].body shouldBe "file content"
        }

        @Test
        fun `parses part headers and removes Content-Disposition and Content-Type from display headers`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"field\"\r\n")
                append("Content-Type: text/plain\r\n")
                append("X-Custom-Header: custom-value\r\n")
                append("\r\n")
                append("content\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].headers.containsKey("Content-Disposition") shouldBe false
            result[0].headers.containsKey("Content-Type") shouldBe false
            result[0].headers["X-Custom-Header"] shouldBe "custom-value"
        }

        @Test
        fun `sets name to unnamed when Content-Disposition has no name`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data\r\n")
                append("\r\n")
                append("content\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].name shouldBe "unnamed"
        }

        @Test
        fun `calculates body size correctly`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"field\"\r\n")
                append("\r\n")
                append("hello\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].size shouldBe result[0].body.length
        }

        @Test
        fun `returns null fileName when no filename in disposition`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"field\"\r\n")
                append("\r\n")
                append("content\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].fileName.shouldBeNull()
        }
    }

    @Nested
    inner class `parse with auto-detected boundary` {

        @Test
        fun `auto-detects boundary from first line`() {
            val data = buildString {
                append("--auto-boundary\r\n")
                append("Content-Disposition: form-data; name=\"field\"\r\n")
                append("\r\n")
                append("value\r\n")
                append("--auto-boundary--")
            }
            val result = parser.parse(data, null)

            result shouldHaveSize 1
            result[0].name shouldBe "field"
            result[0].body shouldBe "value"
        }

        @Test
        fun `returns empty list when boundary cannot be detected`() {
            val result = parser.parse("no boundary here", null)

            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class `edge cases` {

        @Test
        fun `returns empty list for blank input`() {
            parser.parse("", null).shouldBeEmpty()
            parser.parse("   ", null).shouldBeEmpty()
        }

        @Test
        fun `handles LF line endings instead of CRLF`() {
            val data = "--boundary\nContent-Disposition: form-data; name=\"field\"\n\nvalue\n--boundary--"
            val result = parser.parse(data, "boundary")

            result shouldHaveSize 1
            result[0].name shouldBe "field"
            result[0].body shouldBe "value"
        }
    }

    @Nested
    inner class `extractMultipartBoundary` {

        @Test
        fun `extracts boundary from content type header`() {
            val result = parser.extractMultipartBoundary("multipart/form-data; boundary=abc123")

            result shouldBe "abc123"
        }

        @Test
        fun `extracts quoted boundary`() {
            val result = parser.extractMultipartBoundary("""multipart/form-data; boundary="abc123"""")

            result shouldBe "abc123"
        }

        @Test
        fun `returns null when no boundary parameter`() {
            val result = parser.extractMultipartBoundary("multipart/form-data")

            result.shouldBeNull()
        }
    }

    @Nested
    inner class `parseBody` {

        @Test
        fun `returns empty parsed body for empty byte array`() {
            val result = parser.parse(ByteArray(0))

            result.formatted shouldBe ""
            result.contentType shouldBe ContentType.MULTIPART
            result.isValid shouldBe true
        }

        @Test
        fun `metadata contains part count`() {
            val data = buildString {
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"a\"\r\n")
                append("\r\n")
                append("1\r\n")
                append("--boundary\r\n")
                append("Content-Disposition: form-data; name=\"b\"\r\n")
                append("\r\n")
                append("2\r\n")
                append("--boundary--")
            }
            val result = parser.parse(data.toByteArray())

            result.metadata["partCount"] shouldBe "2"
            result.isValid shouldBe true
        }

        @Test
        fun `isValid is false when no parts are parsed`() {
            val body = "this is not multipart data".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe false
            result.metadata["partCount"] shouldBe "0"
        }
    }
}
