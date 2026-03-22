package com.azikar24.wormaceptor.infra.parser.xml

import com.azikar24.wormaceptor.domain.contracts.ContentType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class XmlBodyParserTest {

    private val parser = XmlBodyParser()

    @Nested
    inner class `supportedContentTypes` {

        @Test
        fun `contains all expected XML MIME types`() {
            parser.supportedContentTypes shouldBe listOf(
                "application/xml",
                "text/xml",
                "application/xhtml+xml",
                "application/soap+xml",
                "application/rss+xml",
                "application/atom+xml",
            )
        }
    }

    @Nested
    inner class `priority` {

        @Test
        fun `is 200`() {
            parser.priority shouldBe 200
        }
    }

    @Nested
    inner class `canParse` {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/xml",
                "text/xml",
                "application/xhtml+xml",
                "application/soap+xml",
                "application/rss+xml",
                "application/atom+xml",
            ],
        )
        fun `returns true for supported XML content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for content type with charset parameter`() {
            parser.canParse("application/xml; charset=utf-8", ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for custom +xml suffix`() {
            parser.canParse("application/custom+xml", ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for uppercase content type`() {
            parser.canParse("APPLICATION/XML", ByteArray(0)) shouldBe true
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/html",
                "text/plain",
                "multipart/form-data",
            ],
        )
        fun `returns false for non-XML content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns false when content type is null`() {
            parser.canParse(null, "<root/>".toByteArray()) shouldBe false
        }
    }

    @Nested
    inner class `format` {

        @Test
        fun `formats processing instruction`() {
            val result = parser.format("""<?xml version="1.0"?>""")

            result shouldHaveSize 1
            result[0] shouldBe """<?xml version="1.0"?>"""
        }

        @Test
        fun `formats comment`() {
            val result = parser.format("<!-- This is a comment -->")

            result shouldHaveSize 1
            result[0] shouldBe "<!-- This is a comment -->"
        }

        @Test
        fun `formats CDATA section`() {
            val result = parser.format("<![CDATA[some raw content]]>")

            result shouldHaveSize 1
            result[0] shouldBe "<![CDATA[some raw content]]>"
        }

        @Test
        fun `formats DOCTYPE declaration`() {
            val result = parser.format("<!DOCTYPE html>")

            result shouldHaveSize 1
            result[0] shouldBe "<!DOCTYPE html>"
        }

        @Test
        fun `formats self-closing tag without indentation change`() {
            val result = parser.format("<root><br/></root>")

            result shouldHaveSize 3
            result[0] shouldBe "<root>"
            result[1] shouldBe "  <br/>"
            result[2] shouldBe "</root>"
        }

        @Test
        fun `indents nested tags`() {
            val result = parser.format("<root><child><grandchild/></child></root>")

            result shouldHaveSize 5
            result[0] shouldBe "<root>"
            result[1] shouldBe "  <child>"
            result[2] shouldBe "    <grandchild/>"
            result[3] shouldBe "  </child>"
            result[4] shouldBe "</root>"
        }

        @Test
        fun `preserves text content between tags`() {
            val result = parser.format("<name>John</name>")

            result shouldHaveSize 3
            result[0] shouldBe "<name>"
            result[1] shouldBe "  John"
            result[2] shouldBe "</name>"
        }

        @Test
        fun `handles multiple sibling elements`() {
            val result = parser.format("<root><a/><b/><c/></root>")

            result shouldHaveSize 5
            result[0] shouldBe "<root>"
            result[1] shouldBe "  <a/>"
            result[2] shouldBe "  <b/>"
            result[3] shouldBe "  <c/>"
            result[4] shouldBe "</root>"
        }

        @Test
        fun `returns empty list for empty input`() {
            parser.format("").shouldBeEmpty()
        }

        @Test
        fun `returns empty list for whitespace-only input`() {
            parser.format("   ").shouldBeEmpty()
        }

        @Test
        fun `handles unclosed comment gracefully`() {
            val result = parser.format("<!-- unclosed comment >")

            result shouldHaveSize 1
            result[0] shouldContain "<!-- unclosed comment >"
        }

        @Test
        fun `handles unclosed CDATA gracefully`() {
            val result = parser.format("<![CDATA[unclosed cdata>")

            result shouldHaveSize 1
            result[0] shouldContain "<![CDATA[unclosed cdata>"
        }

        @Test
        fun `handles unclosed tag gracefully`() {
            val result = parser.format("<root")

            result shouldHaveSize 1
            result[0] shouldContain "<"
        }

        @Test
        fun `handles complex XML document`() {
            val xml = """<?xml version="1.0"?><root><item id="1"><name>Test</name></item></root>"""
            val result = parser.format(xml)

            result[0] shouldBe """<?xml version="1.0"?>"""
            result[1] shouldBe "<root>"
            result[2] shouldBe """  <item id="1">"""
            result[3] shouldBe "    <name>"
            result[4] shouldBe "      Test"
            result[5] shouldBe "    </name>"
            result[6] shouldBe "  </item>"
            result[7] shouldBe "</root>"
        }
    }

    @Nested
    inner class `parse body` {

        @Test
        fun `returns empty parsed body for empty byte array`() {
            val result = parser.parse(ByteArray(0))

            result.formatted shouldBe ""
            result.contentType shouldBe ContentType.XML
            result.isValid shouldBe true
        }

        @Test
        fun `formats XML body and returns valid parsed body`() {
            val body = "<root><child/></root>".toByteArray()
            val result = parser.parse(body)

            result.contentType shouldBe ContentType.XML
            result.isValid shouldBe true
            result.formatted shouldContain "<root>"
            result.formatted shouldContain "  <child/>"
            result.formatted shouldContain "</root>"
        }
    }
}
