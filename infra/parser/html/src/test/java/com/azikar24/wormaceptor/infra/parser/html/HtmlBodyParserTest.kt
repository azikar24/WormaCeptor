package com.azikar24.wormaceptor.infra.parser.html

import com.azikar24.wormaceptor.domain.contracts.ContentType
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class HtmlBodyParserTest {

    private val parser = HtmlBodyParser()

    @Nested
    inner class `supportedContentTypes` {

        @Test
        fun `contains text-html and application-xhtml+xml`() {
            parser.supportedContentTypes shouldBe listOf(
                "text/html",
                "application/xhtml+xml",
            )
        }
    }

    @Nested
    inner class `priority` {

        @Test
        fun `is 230`() {
            parser.priority shouldBe 230
        }
    }

    @Nested
    inner class `canParse` {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "text/html",
                "application/xhtml+xml",
            ],
        )
        fun `returns true for supported HTML content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe true
        }

        @Test
        fun `returns true for content type with charset parameter`() {
            parser.canParse("text/html; charset=utf-8", ByteArray(0)) shouldBe true
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/xml",
                "text/plain",
                "application/xml",
            ],
        )
        fun `returns false for non-HTML content types`(contentType: String) {
            parser.canParse(contentType, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns true when body starts with doctype`() {
            parser.canParse(null, "<!DOCTYPE html><html></html>".toByteArray()) shouldBe true
        }

        @Test
        fun `returns true when body starts with html tag`() {
            parser.canParse(null, "<html><body>Hello</body></html>".toByteArray()) shouldBe true
        }

        @Test
        fun `returns true when body contains html open and close tags`() {
            parser.canParse(null, "<div><html><p>test</p></html></div>".toByteArray()) shouldBe true
        }

        @Test
        fun `returns false for empty body and null content type`() {
            parser.canParse(null, ByteArray(0)) shouldBe false
        }

        @Test
        fun `returns false for non-HTML body and null content type`() {
            parser.canParse(null, "just plain text".toByteArray()) shouldBe false
        }

        @Test
        fun `is case insensitive for body inspection`() {
            parser.canParse(null, "<!DOCTYPE HTML>".toByteArray()) shouldBe true
        }
    }

    @Nested
    inner class `formatHtml` {

        @Test
        fun `formats HTML comment`() {
            val body = "<!-- comment -->".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<!-- comment -->"
            result.isValid shouldBe true
        }

        @Test
        fun `formats DOCTYPE declaration`() {
            val body = "<!DOCTYPE html>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldStartWith "<!DOCTYPE html>"
            result.isValid shouldBe true
        }

        @Test
        fun `formats opening and closing tags with indentation`() {
            val body = "<div><p>text</p></div>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<div>"
            result.formatted shouldContain "  <p>"
            result.formatted shouldContain "</p>"
            result.formatted shouldContain "</div>"
            result.isValid shouldBe true
        }

        @Test
        fun `formats void elements without increasing indent`() {
            val body = "<div><br><hr><img src=\"test.png\"></div>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<div>"
            result.formatted shouldContain "<br>"
            result.formatted shouldContain "<hr>"
            result.formatted shouldContain "</div>"
            result.isValid shouldBe true
        }

        @Test
        fun `preserves preformatted tag content`() {
            val body = "<pre>  line1\n  line2</pre>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<pre>"
            result.formatted shouldContain "  line1"
            result.formatted shouldContain "  line2"
            result.formatted shouldContain "</pre>"
            result.isValid shouldBe true
        }

        @Test
        fun `preserves script tag content`() {
            val body = "<script>var x = 1;</script>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<script>"
            result.formatted shouldContain "var x = 1;"
            result.formatted shouldContain "</script>"
        }

        @Test
        fun `preserves style tag content`() {
            val body = "<style>body { color: red; }</style>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<style>"
            result.formatted shouldContain "body { color: red; }"
            result.formatted shouldContain "</style>"
        }

        @Test
        fun `trims text content outside preformatted tags`() {
            val body = "<div>   hello world   </div>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "hello world"
            result.isValid shouldBe true
        }

        @Test
        fun `formats self-closing tags with slash`() {
            val body = "<div><input type=\"text\" /></div>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<input type=\"text\" />"
            result.isValid shouldBe true
        }

        @Test
        fun `handles deeply nested structure`() {
            val body = "<html><body><div><p><span>deep</span></p></div></body></html>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<html>"
            result.formatted shouldContain "  <body>"
            result.formatted shouldContain "    <div>"
            result.formatted shouldContain "      <p>"
            result.formatted shouldContain "        <span>"
            result.isValid shouldBe true
        }

        @Test
        fun `handles unclosed comment gracefully`() {
            val body = "<!-- unclosed".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<!-- unclosed"
            result.isValid shouldBe true
        }

        @Test
        fun `handles unclosed tag gracefully`() {
            val body = "<div".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<div"
            result.isValid shouldBe true
        }

        @Test
        fun `handles tag attributes with quotes`() {
            val body = """<a href="http://example.com" class="link">Click</a>""".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain """<a href="http://example.com" class="link">"""
            result.formatted shouldContain "Click"
            result.isValid shouldBe true
        }

        @Test
        fun `handles complete HTML document`() {
            val html = """<!DOCTYPE html><html><head><title>Test</title></head><body><p>Hello</p></body></html>"""
            val result = parser.parse(html.toByteArray())

            result.formatted shouldContain "<!DOCTYPE html>"
            result.formatted shouldContain "<html>"
            result.formatted shouldContain "<head>"
            result.formatted shouldContain "<title>"
            result.formatted shouldContain "Test"
            result.formatted shouldContain "</title>"
            result.formatted shouldContain "</head>"
            result.formatted shouldContain "<body>"
            result.formatted shouldContain "<p>"
            result.formatted shouldContain "Hello"
            result.isValid shouldBe true
        }

        @Test
        fun `returns empty parsed body for empty byte array`() {
            val result = parser.parse(ByteArray(0))

            result.formatted shouldBe ""
            result.contentType shouldBe ContentType.HTML
            result.isValid shouldBe true
        }

        @Test
        fun `handles empty non-void tags`() {
            val body = "<div></div>".toByteArray()
            val result = parser.parse(body)

            result.formatted shouldContain "<div>"
            result.formatted shouldContain "</div>"
            result.isValid shouldBe true
        }
    }

    @Nested
    inner class `extractMetadata` {

        @Test
        fun `extracts title`() {
            val html = "<html><head><title>My Page</title></head></html>"
            val result = parser.parse(html.toByteArray())

            result.metadata["title"] shouldBe "My Page"
        }

        @Test
        fun `extracts meta description with name first`() {
            val html = """<html><head><meta name="description" content="A great page"></head></html>"""
            val result = parser.parse(html.toByteArray())

            result.metadata["description"] shouldBe "A great page"
        }

        @Test
        fun `extracts meta description with content first`() {
            val html = """<html><head><meta content="Alternate order" name="description"></head></html>"""
            val result = parser.parse(html.toByteArray())

            result.metadata["description"] shouldBe "Alternate order"
        }

        @Test
        fun `extracts charset`() {
            val html = """<html><head><meta charset="utf-8"></head></html>"""
            val result = parser.parse(html.toByteArray())

            result.metadata["charset"] shouldBe "utf-8"
        }

        @Test
        fun `extracts viewport`() {
            val html = """<html><head>""" +
                """<meta name="viewport" content="width=device-width, initial-scale=1">""" +
                """</head></html>"""
            val result = parser.parse(html.toByteArray())

            result.metadata["viewport"] shouldBe "width=device-width, initial-scale=1"
        }

        @Test
        fun `extracts doctype`() {
            val html = "<!DOCTYPE html><html></html>"
            val result = parser.parse(html.toByteArray())

            result.metadata["doctype"] shouldBe "html"
        }

        @Test
        fun `handles HTML with no metadata`() {
            val html = "<html><body><p>Hello</p></body></html>"
            val result = parser.parse(html.toByteArray())

            result.metadata shouldNotContainKey "title"
            result.metadata shouldNotContainKey "description"
            result.metadata shouldNotContainKey "charset"
        }

        @Test
        fun `extracts all metadata from complete document`() {
            val html = buildString {
                append("<!DOCTYPE html>")
                append("<html>")
                append("<head>")
                append("<meta charset=\"utf-8\">")
                append("<meta name=\"viewport\" content=\"width=device-width\">")
                append("<meta name=\"description\" content=\"Test page\">")
                append("<title>Test Title</title>")
                append("</head>")
                append("<body></body>")
                append("</html>")
            }
            val result = parser.parse(html.toByteArray())

            result.metadata["title"] shouldBe "Test Title"
            result.metadata["description"] shouldBe "Test page"
            result.metadata["charset"] shouldBe "utf-8"
            result.metadata["viewport"] shouldBe "width=device-width"
            result.metadata["doctype"] shouldBe "html"
        }
    }

    @Nested
    inner class `contentType` {

        @Test
        fun `defaultContentType is HTML`() {
            val result = parser.parse("<html></html>".toByteArray())

            result.contentType shouldBe ContentType.HTML
        }
    }

    @Nested
    inner class `custom indent` {

        @Test
        fun `uses custom indent string`() {
            val customParser = HtmlBodyParser(indentString = "    ")
            val body = "<div><p>text</p></div>".toByteArray()
            val result = customParser.parse(body)

            result.formatted shouldContain "    <p>"
        }
    }
}
