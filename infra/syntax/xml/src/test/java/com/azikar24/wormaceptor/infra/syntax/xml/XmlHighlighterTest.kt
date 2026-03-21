package com.azikar24.wormaceptor.infra.syntax.xml

import com.azikar24.wormaceptor.domain.contracts.TokenType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class XmlHighlighterTest {

    private val highlighter = XmlHighlighter()

    @Test
    fun `language should be xml`() {
        highlighter.language shouldBe "xml"
    }

    @Nested
    inner class Comments {

        @Test
        fun `should tokenize XML comment`() {
            val code = "<!-- this is a comment -->"
            val tokens = highlighter.tokenize(code)

            val commentTokens = tokens.filter { it.type == TokenType.COMMENT }
            commentTokens shouldHaveSize 1
            code.substring(commentTokens[0].start, commentTokens[0].end) shouldBe "<!-- this is a comment -->"
        }

        @Test
        fun `should tokenize multiline comment`() {
            val code = """<!-- line1
            line2 -->
            """.trimIndent()
            val tokens = highlighter.tokenize(code)

            val commentTokens = tokens.filter { it.type == TokenType.COMMENT }
            commentTokens shouldHaveSize 1
            code.substring(commentTokens[0].start, commentTokens[0].end) shouldBe code
        }
    }

    @Nested
    inner class CData {

        @Test
        fun `should tokenize CDATA section as KEYWORD`() {
            val code = "<![CDATA[some raw data]]>"
            val tokens = highlighter.tokenize(code)

            val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
            keywordTokens shouldHaveSize 1
            code.substring(keywordTokens[0].start, keywordTokens[0].end) shouldBe code
        }
    }

    @Nested
    inner class DocType {

        @Test
        fun `should tokenize DOCTYPE as KEYWORD`() {
            val code = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0//EN">"""
            val tokens = highlighter.tokenize(code)

            val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
            keywordTokens shouldHaveSize 1
            code.substring(keywordTokens[0].start, keywordTokens[0].end) shouldBe code
        }
    }

    @Nested
    inner class ProcessingInstructions {

        @Test
        fun `should tokenize XML declaration as KEYWORD`() {
            val code = """<?xml version="1.0" encoding="UTF-8"?>"""
            val tokens = highlighter.tokenize(code)

            val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
            keywordTokens shouldHaveSize 1
            code.substring(keywordTokens[0].start, keywordTokens[0].end) shouldBe code
        }

        @Test
        fun `should tokenize non-xml processing instruction as KEYWORD`() {
            val code = """<?php echo "hello" ?>"""
            val tokens = highlighter.tokenize(code)

            val keywordTokens = tokens.filter { it.type == TokenType.KEYWORD }
            keywordTokens shouldHaveSize 1
            code.substring(keywordTokens[0].start, keywordTokens[0].end) shouldBe code
        }
    }

    @Nested
    inner class TagNames {

        @Test
        fun `should tokenize opening tag name`() {
            val code = "<person>"
            val tokens = highlighter.tokenize(code)

            val tagTokens = tokens.filter { it.type == TokenType.TAG }
            tagTokens shouldHaveSize 1
            code.substring(tagTokens[0].start, tagTokens[0].end) shouldBe "person"
        }

        @Test
        fun `should tokenize closing tag name`() {
            val code = "</person>"
            val tokens = highlighter.tokenize(code)

            val tagTokens = tokens.filter { it.type == TokenType.TAG }
            tagTokens shouldHaveSize 1
            code.substring(tagTokens[0].start, tagTokens[0].end) shouldBe "person"
        }

        @Test
        fun `should tokenize self-closing tag`() {
            val code = "<br/>"
            val tokens = highlighter.tokenize(code)

            val tagTokens = tokens.filter { it.type == TokenType.TAG }
            tagTokens shouldHaveSize 1
            code.substring(tagTokens[0].start, tagTokens[0].end) shouldBe "br"
        }

        @Test
        fun `should tokenize namespaced tag`() {
            val code = "<ns:element>"
            val tokens = highlighter.tokenize(code)

            val tagTokens = tokens.filter { it.type == TokenType.TAG }
            tagTokens shouldHaveSize 1
            code.substring(tagTokens[0].start, tagTokens[0].end) shouldBe "ns:element"
        }
    }

    @Nested
    inner class Attributes {

        @Test
        fun `should tokenize attribute name as PROPERTY`() {
            val code = """<div class="main">"""
            val tokens = highlighter.tokenize(code)

            val propertyTokens = tokens.filter { it.type == TokenType.PROPERTY }
            propertyTokens shouldHaveSize 1
            code.substring(propertyTokens[0].start, propertyTokens[0].end) shouldBe "class"
        }

        @Test
        fun `should tokenize double-quoted attribute value as STRING`() {
            val code = """<div class="main">"""
            val tokens = highlighter.tokenize(code)

            val stringTokens = tokens.filter { it.type == TokenType.STRING }
            stringTokens shouldHaveSize 1
            code.substring(stringTokens[0].start, stringTokens[0].end) shouldBe "\"main\""
        }

        @Test
        fun `should tokenize single-quoted attribute value as STRING`() {
            val code = "<div class='main'>"
            val tokens = highlighter.tokenize(code)

            val stringTokens = tokens.filter { it.type == TokenType.STRING }
            stringTokens shouldHaveSize 1
            code.substring(stringTokens[0].start, stringTokens[0].end) shouldBe "'main'"
        }

        @Test
        fun `should tokenize unquoted attribute value as STRING`() {
            val code = "<input type=text>"
            val tokens = highlighter.tokenize(code)

            val stringTokens = tokens.filter { it.type == TokenType.STRING }
            stringTokens shouldHaveSize 1
            code.substring(stringTokens[0].start, stringTokens[0].end) shouldBe "text"
        }

        @Test
        fun `should tokenize multiple attributes`() {
            val code = """<input type="text" name="field" value="123">"""
            val tokens = highlighter.tokenize(code)

            val propertyTokens = tokens.filter { it.type == TokenType.PROPERTY }
            propertyTokens shouldHaveSize 3
            val attrNames = propertyTokens.map { code.substring(it.start, it.end) }
            attrNames shouldBe listOf("type", "name", "value")
        }
    }

    @Nested
    inner class BracketPunctuation {

        @Test
        fun `should tokenize opening bracket as PUNCTUATION`() {
            val code = "<div>"
            val tokens = highlighter.tokenize(code)

            val punctTokens = tokens.filter { it.type == TokenType.PUNCTUATION }
            // < and >
            punctTokens shouldHaveSize 2
            code.substring(punctTokens[0].start, punctTokens[0].end) shouldBe "<"
            code.substring(punctTokens[1].start, punctTokens[1].end) shouldBe ">"
        }

        @Test
        fun `should tokenize closing tag bracket as PUNCTUATION`() {
            val code = "</div>"
            val tokens = highlighter.tokenize(code)

            val punctTokens = tokens.filter { it.type == TokenType.PUNCTUATION }
            punctTokens shouldHaveSize 2
            code.substring(punctTokens[0].start, punctTokens[0].end) shouldBe "</"
            code.substring(punctTokens[1].start, punctTokens[1].end) shouldBe ">"
        }

        @Test
        fun `should tokenize self-closing bracket as PUNCTUATION`() {
            val code = "<br/>"
            val tokens = highlighter.tokenize(code)

            val punctTokens = tokens.filter { it.type == TokenType.PUNCTUATION }
            punctTokens shouldHaveSize 2
            code.substring(punctTokens[0].start, punctTokens[0].end) shouldBe "<"
            code.substring(punctTokens[1].start, punctTokens[1].end) shouldBe "/>"
        }
    }

    @Nested
    inner class EntityReferences {

        @Test
        fun `should tokenize named entity reference as KEYWORD`() {
            val code = "<p>A &amp; B</p>"
            val tokens = highlighter.tokenize(code)

            val entityTokens = tokens.filter { it.type == TokenType.KEYWORD }
            entityTokens shouldHaveSize 1
            code.substring(entityTokens[0].start, entityTokens[0].end) shouldBe "&amp;"
        }

        @Test
        fun `should tokenize numeric entity reference`() {
            val code = "<p>&#65;</p>"
            val tokens = highlighter.tokenize(code)

            val entityTokens = tokens.filter { it.type == TokenType.KEYWORD }
            entityTokens shouldHaveSize 1
            code.substring(entityTokens[0].start, entityTokens[0].end) shouldBe "&#65;"
        }

        @Test
        fun `should tokenize hex entity reference`() {
            val code = "<p>&#x41;</p>"
            val tokens = highlighter.tokenize(code)

            val entityTokens = tokens.filter { it.type == TokenType.KEYWORD }
            entityTokens shouldHaveSize 1
            code.substring(entityTokens[0].start, entityTokens[0].end) shouldBe "&#x41;"
        }
    }

    @Nested
    inner class TokenOrdering {

        @Test
        fun `tokens should be sorted by start position`() {
            val code = """<root attr="val"><!-- comment --><child/></root>"""
            val tokens = highlighter.tokenize(code)

            val starts = tokens.map { it.start }
            starts shouldBe starts.sorted()
        }

        @Test
        fun `tokens should not overlap`() {
            val code = """<root attr="val"><!-- comment --><child/></root>"""
            val tokens = highlighter.tokenize(code)

            for (i in 0 until tokens.size - 1) {
                val current = tokens[i]
                val next = tokens[i + 1]
                (current.end <= next.start) shouldBe true
            }
        }
    }

    @Nested
    inner class EdgeCases {

        @Test
        fun `empty string should return no tokens`() {
            val tokens = highlighter.tokenize("")
            tokens.shouldBeEmpty()
        }

        @Test
        fun `nested tags should tokenize correctly`() {
            val code = "<root><parent><child>text</child></parent></root>"
            val tokens = highlighter.tokenize(code)

            val tagTokens = tokens.filter { it.type == TokenType.TAG }
            val tagNames = tagTokens.map { code.substring(it.start, it.end) }
            tagNames shouldBe listOf("root", "parent", "child", "child", "parent", "root")
        }

        @Test
        fun `tags inside comments should not be tokenized as tags`() {
            val code = "<!-- <hidden> --><visible>"
            val tokens = highlighter.tokenize(code)

            val tagTokens = tokens.filter { it.type == TokenType.TAG }
            tagTokens shouldHaveSize 1
            code.substring(tagTokens[0].start, tagTokens[0].end) shouldBe "visible"
        }

        @Test
        fun `complex XML document should produce valid token set`() {
            val code = """
                <?xml version="1.0"?>
                <!DOCTYPE root>
                <root xmlns:ns="http://example.com">
                    <!-- comment -->
                    <ns:child attr="value">text &amp; more</ns:child>
                    <![CDATA[raw <data>]]>
                    <self-closing/>
                </root>
            """.trimIndent()
            val tokens = highlighter.tokenize(code)

            // Verify token types present
            val types = tokens.map { it.type }.toSet()
            types shouldContain TokenType.KEYWORD // xml decl, DOCTYPE, CDATA, &amp;
            types shouldContain TokenType.COMMENT
            types shouldContain TokenType.TAG
            types shouldContain TokenType.PROPERTY
            types shouldContain TokenType.STRING
            types shouldContain TokenType.PUNCTUATION

            // Verify no overlaps
            for (i in 0 until tokens.size - 1) {
                (tokens[i].end <= tokens[i + 1].start) shouldBe true
            }
        }
    }
}
