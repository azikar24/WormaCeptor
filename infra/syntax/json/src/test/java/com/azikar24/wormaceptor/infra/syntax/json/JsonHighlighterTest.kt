package com.azikar24.wormaceptor.infra.syntax.json

import com.azikar24.wormaceptor.domain.contracts.TokenType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class JsonHighlighterTest {

    private val highlighter = JsonHighlighter()

    @Test
    fun `language should be json`() {
        highlighter.language shouldBe "json"
    }

    @Nested
    inner class PropertyKeys {

        @Test
        fun `should tokenize property key without colon`() {
            val code = """{"name": "value"}"""
            val tokens = highlighter.tokenize(code)

            val propertyToken = tokens.first { it.type == TokenType.PROPERTY }
            // The property token should cover "name" (with quotes) but not the colon
            code.substring(propertyToken.start, propertyToken.end) shouldBe "\"name\""
        }

        @Test
        fun `should tokenize multiple property keys`() {
            val code = """{"first": 1, "second": 2}"""
            val tokens = highlighter.tokenize(code)

            val propertyTokens = tokens.filter { it.type == TokenType.PROPERTY }
            propertyTokens shouldHaveSize 2
            code.substring(propertyTokens[0].start, propertyTokens[0].end) shouldBe "\"first\""
            code.substring(propertyTokens[1].start, propertyTokens[1].end) shouldBe "\"second\""
        }
    }

    @Nested
    inner class StringValues {

        @Test
        fun `should tokenize string value`() {
            val code = """{"key": "hello world"}"""
            val tokens = highlighter.tokenize(code)

            val stringTokens = tokens.filter { it.type == TokenType.STRING }
            stringTokens shouldHaveSize 1
            code.substring(stringTokens[0].start, stringTokens[0].end) shouldBe "\"hello world\""
        }

        @Test
        fun `should tokenize escaped quotes in strings`() {
            val code = """{"key": "say \"hi\""}"""
            val tokens = highlighter.tokenize(code)

            val stringTokens = tokens.filter { it.type == TokenType.STRING }
            stringTokens shouldHaveSize 1
            code.substring(stringTokens[0].start, stringTokens[0].end) shouldBe "\"say \\\"hi\\\"\""
        }
    }

    @Nested
    inner class Numbers {

        @Test
        fun `should tokenize integer numbers`() {
            val code = """{"count": 42}"""
            val tokens = highlighter.tokenize(code)

            val numberTokens = tokens.filter { it.type == TokenType.NUMBER }
            numberTokens shouldHaveSize 1
            code.substring(numberTokens[0].start, numberTokens[0].end) shouldBe "42"
        }

        @Test
        fun `should tokenize float numbers`() {
            val code = """{"price": 19.99}"""
            val tokens = highlighter.tokenize(code)

            val numberTokens = tokens.filter { it.type == TokenType.NUMBER }
            numberTokens shouldHaveSize 1
            code.substring(numberTokens[0].start, numberTokens[0].end) shouldBe "19.99"
        }

        @Test
        fun `should tokenize negative numbers`() {
            val code = """{"temp": -5}"""
            val tokens = highlighter.tokenize(code)

            val numberTokens = tokens.filter { it.type == TokenType.NUMBER }
            numberTokens shouldHaveSize 1
            code.substring(numberTokens[0].start, numberTokens[0].end) shouldBe "-5"
        }

        @Test
        fun `should tokenize scientific notation`() {
            val code = """{"value": 1.5e10}"""
            val tokens = highlighter.tokenize(code)

            val numberTokens = tokens.filter { it.type == TokenType.NUMBER }
            numberTokens shouldHaveSize 1
            code.substring(numberTokens[0].start, numberTokens[0].end) shouldBe "1.5e10"
        }
    }

    @Nested
    inner class BooleansAndNull {

        @Test
        fun `should tokenize true`() {
            val code = """{"active": true}"""
            val tokens = highlighter.tokenize(code)

            val boolTokens = tokens.filter { it.type == TokenType.BOOLEAN }
            boolTokens shouldHaveSize 1
            code.substring(boolTokens[0].start, boolTokens[0].end) shouldBe "true"
        }

        @Test
        fun `should tokenize false`() {
            val code = """{"active": false}"""
            val tokens = highlighter.tokenize(code)

            val boolTokens = tokens.filter { it.type == TokenType.BOOLEAN }
            boolTokens shouldHaveSize 1
            code.substring(boolTokens[0].start, boolTokens[0].end) shouldBe "false"
        }

        @Test
        fun `should tokenize null`() {
            val code = """{"value": null}"""
            val tokens = highlighter.tokenize(code)

            val boolTokens = tokens.filter { it.type == TokenType.BOOLEAN }
            boolTokens shouldHaveSize 1
            code.substring(boolTokens[0].start, boolTokens[0].end) shouldBe "null"
        }
    }

    @Nested
    inner class Punctuation {

        @Test
        fun `should tokenize punctuation characters`() {
            val code = """{"a": [1, 2]}"""
            val tokens = highlighter.tokenize(code)

            val punctTokens = tokens.filter { it.type == TokenType.PUNCTUATION }
            // The colon after property key "a": is consumed by the PROPERTY pattern's usedRange,
            // so only structural punctuation outside property patterns is tokenized
            val punctChars = punctTokens.map { code.substring(it.start, it.end) }
            punctChars shouldBe listOf("{", "[", ",", "]", "}")
        }
    }

    @Nested
    inner class TokenOrdering {

        @Test
        fun `tokens should be sorted by start position`() {
            val code = """{"name": "Alice", "age": 30, "active": true}"""
            val tokens = highlighter.tokenize(code)

            val starts = tokens.map { it.start }
            starts shouldBe starts.sorted()
        }

        @Test
        fun `tokens should not overlap`() {
            val code = """{"name": "Alice", "age": 30, "active": true, "data": null}"""
            val tokens = highlighter.tokenize(code)

            for (i in 0 until tokens.size - 1) {
                val current = tokens[i]
                val next = tokens[i + 1]
                // current.end should be <= next.start (no overlap)
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
        fun `complex nested JSON should tokenize without errors`() {
            val code = """
                {
                    "users": [
                        {
                            "name": "Alice",
                            "age": 30,
                            "active": true,
                            "address": null
                        },
                        {
                            "name": "Bob",
                            "scores": [1.5, 2e3, -7]
                        }
                    ]
                }
            """.trimIndent()
            val tokens = highlighter.tokenize(code)

            // Verify no overlapping tokens
            for (i in 0 until tokens.size - 1) {
                (tokens[i].end <= tokens[i + 1].start) shouldBe true
            }

            // Verify all expected token types are present
            val types = tokens.map { it.type }.toSet()
            types shouldBe setOf(
                TokenType.PROPERTY,
                TokenType.STRING,
                TokenType.NUMBER,
                TokenType.BOOLEAN,
                TokenType.PUNCTUATION,
            )
        }
    }
}
