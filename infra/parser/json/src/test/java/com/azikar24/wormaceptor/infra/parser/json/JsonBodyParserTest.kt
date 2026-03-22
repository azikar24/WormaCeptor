package com.azikar24.wormaceptor.infra.parser.json

import com.azikar24.wormaceptor.domain.contracts.ContentType
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class JsonBodyParserTest {

    private val parser = JsonBodyParser()

    @Nested
    inner class `supportedContentTypes` {

        @Test
        fun `contains all expected JSON MIME types`() {
            parser.supportedContentTypes shouldBe listOf(
                "application/json",
                "text/json",
                "application/vnd.api+json",
                "application/hal+json",
                "application/ld+json",
                "application/json-patch+json",
            )
        }
    }

    @Nested
    inner class `priority` {

        @Test
        fun `is 250`() {
            parser.priority shouldBe 250
        }
    }

    @Nested
    inner class `canParse` {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/json",
                "application/vnd.api+json",
                "application/hal+json",
                "application/ld+json",
                "application/json-patch+json",
            ],
        )
        fun `returns true for supported JSON content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for content type with charset parameter`() {
            parser.canParse("application/json; charset=utf-8", ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for custom +json suffix`() {
            parser.canParse("application/vnd.custom+json", ByteArray(0)) shouldBe true
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/xml",
                "text/html",
                "text/plain",
                "multipart/form-data",
            ],
        )
        fun `returns false for non-JSON content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns true when body starts with open brace and no content type`() {
            parser.canParse(null, """{"key": "value"}""".toByteArray()) shouldBe true
        }

        @Test
        fun `returns true when body starts with open bracket and no content type`() {
            parser.canParse(null, """[1, 2, 3]""".toByteArray()) shouldBe true
        }

        @Test
        fun `returns true when body has leading whitespace before brace`() {
            parser.canParse(null, """  {"key": "value"}""".toByteArray()) shouldBe true
        }

        @Test
        fun `returns false for empty body and null content type`() {
            parser.canParse(null, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns false for whitespace-only body and null content type`() {
            parser.canParse(null, "   ".toByteArray()) shouldBe false
        }

        @Test
        fun `returns false for non-JSON body and null content type`() {
            parser.canParse(null, "hello world".toByteArray()) shouldBe false
        }
    }

    @Nested
    inner class `parseBody` {

        @Test
        fun `returns empty parsed body for empty byte array`() {
            val result = parser.parse(ByteArray(0))

            result.formatted shouldBe ""
            result.contentType shouldBe ContentType.JSON
            result.isValid shouldBe true
        }

        @Test
        fun `parses JSON object with metadata`() {
            val body = """{"name":"John","age":30}""".toByteArray()
            val result = parser.parse(body)

            result.contentType shouldBe ContentType.JSON
            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "object"
            result.metadata["keyCount"] shouldBe "2"
        }

        @Test
        fun `parses JSON array with metadata`() {
            val body = """[1, 2, 3]""".toByteArray()
            val result = parser.parse(body)

            result.contentType shouldBe ContentType.JSON
            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "array"
            result.metadata["elementCount"] shouldBe "3"
        }

        @Test
        fun `pretty-prints JSON object`() {
            val body = """{"key":"value"}""".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "\"key\""
            result.formatted shouldContain "\"value\""
            result.isValid shouldBe true
        }

        @Test
        fun `handles nested JSON objects`() {
            val body = """{"outer":{"inner":"value"}}""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "object"
            result.metadata["keyCount"] shouldBe "1"
            result.formatted shouldContain "inner"
        }

        @Test
        fun `handles empty JSON object`() {
            val body = """{}""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "object"
            result.metadata["keyCount"] shouldBe "0"
        }

        @Test
        fun `handles empty JSON array`() {
            val body = """[]""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "array"
            result.metadata["elementCount"] shouldBe "0"
        }

        @Test
        fun `returns invalid result for malformed JSON`() {
            val body = """{invalid json""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe false
            result.errorMessage.shouldNotBeNull()
            result.errorMessage shouldContain "Invalid JSON"
        }

        @Test
        fun `uses custom indent spaces`() {
            val customParser = JsonBodyParser(indentSpaces = 4)
            val body = """{"a":"1","b":"2","c":"3"}""".toByteArray()
            val result = customParser.parse(body)

            result.isValid shouldBe true
            result.metadata["keyCount"] shouldBe "3"
        }
    }

    @Nested
    inner class `cleanJson5Syntax` {

        @Test
        fun `removes trailing comma before closing brace`() {
            val body = """{"a":1,"b":2,}""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "object"
        }

        @Test
        fun `removes trailing comma before closing bracket`() {
            val body = """[1, 2, 3,]""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "array"
            result.metadata["elementCount"] shouldBe "3"
        }

        @Test
        fun `strips single-line comments`() {
            val body = """{
                "key": "value" // this is a comment
            }
            """.trimIndent().toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "object"
        }

        @Test
        fun `preserves double slashes inside strings`() {
            val body = """{"url":"https://example.com"}""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.formatted shouldContain "https://example.com"
        }

        @Test
        fun `handles combined trailing comma and comment`() {
            val body = """{
                "a": 1, // comment
                "b": 2,
            }
            """.trimIndent().toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["keyCount"] shouldBe "2"
        }
    }

    @Nested
    inner class `primitive values` {

        @Test
        fun `parses primitive string`() {
            val body = """"hello"""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "primitive"
        }

        @Test
        fun `parses primitive number`() {
            val body = """42""".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe true
            result.metadata["rootType"] shouldBe "primitive"
        }
    }
}
