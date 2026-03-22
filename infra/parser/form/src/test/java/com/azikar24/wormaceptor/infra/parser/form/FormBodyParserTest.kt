package com.azikar24.wormaceptor.infra.parser.form

import com.azikar24.wormaceptor.domain.contracts.ContentType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FormBodyParserTest {

    private val parser = FormBodyParser()

    @Nested
    inner class `supportedContentTypes` {

        @Test
        fun `contains application x-www-form-urlencoded`() {
            parser.supportedContentTypes shouldBe listOf("application/x-www-form-urlencoded")
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
                "application/x-www-form-urlencoded",
                "application/x-www-form-urlencoded; charset=utf-8",
                "APPLICATION/X-WWW-FORM-URLENCODED",
            ],
        )
        fun `returns true for form-urlencoded content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe true
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/html",
                "multipart/form-data",
                "text/plain",
            ],
        )
        fun `returns false for non-form content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns false when content type is null`() {
            parser.canParse(null, "key=value".toByteArray()) shouldBe false
        }
    }

    @Nested
    inner class `parse form data` {

        @Test
        fun `parses simple key-value pair`() {
            val result = parser.parse("name=John")

            result shouldHaveSize 1
            result[0].key shouldBe "name"
            result[0].value shouldBe "John"
        }

        @Test
        fun `parses multiple key-value pairs`() {
            val result = parser.parse("name=John&age=30&city=NYC")

            result shouldHaveSize 3
            result[0].key shouldBe "name"
            result[0].value shouldBe "John"
            result[1].key shouldBe "age"
            result[1].value shouldBe "30"
            result[2].key shouldBe "city"
            result[2].value shouldBe "NYC"
        }

        @Test
        fun `decodes URL-encoded values`() {
            val result = parser.parse("greeting=hello+world&path=%2Fhome%2Fuser")

            result shouldHaveSize 2
            result[0].key shouldBe "greeting"
            result[0].value shouldBe "hello world"
            result[1].key shouldBe "path"
            result[1].value shouldBe "/home/user"
        }

        @Test
        fun `handles key with no value`() {
            val result = parser.parse("key")

            result shouldHaveSize 1
            result[0].key shouldBe "key"
            result[0].value shouldBe ""
        }

        @Test
        fun `handles key with empty value`() {
            val result = parser.parse("key=")

            result shouldHaveSize 1
            result[0].key shouldBe "key"
            result[0].value shouldBe ""
        }

        @Test
        fun `splits on first equals only`() {
            val result = parser.parse("equation=a=b+c")

            result shouldHaveSize 1
            result[0].key shouldBe "equation"
            result[0].value shouldBe "a=b c"
        }

        @Test
        fun `returns empty list for blank input`() {
            parser.parse("").shouldBeEmpty()
            parser.parse("   ").shouldBeEmpty()
        }

        @Test
        fun `filters blank segments between ampersands`() {
            val result = parser.parse("a=1&&b=2")

            result shouldHaveSize 2
            result[0].key shouldBe "a"
            result[1].key shouldBe "b"
        }
    }

    @Nested
    inner class `parse body` {

        @Test
        fun `returns empty parsed body for empty byte array`() {
            val result = parser.parse(ByteArray(0))

            result.formatted shouldBe ""
            result.contentType shouldBe ContentType.FORM_DATA
            result.isValid shouldBe true
        }

        @Test
        fun `formats key-value pairs one per line`() {
            val body = "name=John&age=30".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "name=John"
            result.formatted shouldContain "age=30"
            result.contentType shouldBe ContentType.FORM_DATA
            result.isValid shouldBe true
        }

        @Test
        fun `metadata contains param count`() {
            val body = "a=1&b=2&c=3".toByteArray()
            val result = parser.parse(body)

            result.metadata["paramCount"] shouldBe "3"
        }

        @Test
        fun `isValid is false when no params are parsed`() {
            val body = "&&&".toByteArray()
            val result = parser.parse(body)

            result.isValid shouldBe false
        }
    }
}
