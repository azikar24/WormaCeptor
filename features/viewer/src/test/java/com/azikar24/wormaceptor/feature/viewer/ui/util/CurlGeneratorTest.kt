package com.azikar24.wormaceptor.feature.viewer.ui.util

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurlGeneratorTest {

    @Nested
    inner class BasicGeneration {

        @Test
        fun `should generate simple GET request`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://api.example.com/users",
                headers = emptyMap(),
            )

            result shouldBe "curl -X GET 'https://api.example.com/users'"
        }

        @Test
        fun `should generate POST request with body`() {
            val result = CurlGenerator.generate(
                method = "POST",
                url = "https://api.example.com/users",
                headers = mapOf("Content-Type" to listOf("application/json")),
                body = """{"name":"John"}""",
            )

            result shouldContain "curl -X POST"
            result shouldContain "-H 'Content-Type: application/json'"
            result shouldContain """-d '{"name":"John"}'"""
        }

        @Test
        fun `should generate PUT request with body`() {
            val result = CurlGenerator.generate(
                method = "PUT",
                url = "https://api.example.com/users/1",
                headers = emptyMap(),
                body = """{"name":"Jane"}""",
            )

            result shouldContain "curl -X PUT"
            result shouldContain """-d '{"name":"Jane"}'"""
        }

        @Test
        fun `should generate PATCH request with body`() {
            val result = CurlGenerator.generate(
                method = "PATCH",
                url = "https://api.example.com/users/1",
                headers = emptyMap(),
                body = """{"active":true}""",
            )

            result shouldContain "-d"
        }

        @Test
        fun `should generate DELETE request with body`() {
            val result = CurlGenerator.generate(
                method = "DELETE",
                url = "https://api.example.com/users/1",
                headers = emptyMap(),
                body = """{"confirm":true}""",
            )

            result shouldContain "-d"
        }
    }

    @Nested
    inner class BodyHandling {

        @Test
        fun `should NOT include body for GET even if provided`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://api.example.com/search",
                headers = emptyMap(),
                body = "should-not-appear",
            )

            result shouldNotContain "-d"
            result shouldNotContain "should-not-appear"
        }

        @Test
        fun `should NOT include body for HEAD even if provided`() {
            val result = CurlGenerator.generate(
                method = "HEAD",
                url = "https://api.example.com/health",
                headers = emptyMap(),
                body = "should-not-appear",
            )

            result shouldNotContain "-d"
        }

        @Test
        fun `should NOT include body for OPTIONS even if provided`() {
            val result = CurlGenerator.generate(
                method = "OPTIONS",
                url = "https://api.example.com/cors",
                headers = emptyMap(),
                body = "nope",
            )

            result shouldNotContain "-d"
        }

        @Test
        fun `should handle null body for POST`() {
            val result = CurlGenerator.generate(
                method = "POST",
                url = "https://api.example.com/ping",
                headers = emptyMap(),
                body = null,
            )

            result shouldNotContain "-d"
        }

        @Test
        fun `should handle empty body for POST`() {
            val result = CurlGenerator.generate(
                method = "POST",
                url = "https://api.example.com/ping",
                headers = emptyMap(),
                body = "",
            )

            result shouldNotContain "-d"
        }
    }

    @Nested
    inner class HeaderHandling {

        @Test
        fun `should include single header`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com",
                headers = mapOf("Authorization" to listOf("Bearer tok123")),
            )

            result shouldContain "-H 'Authorization: Bearer tok123'"
        }

        @Test
        fun `should include multiple headers`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com",
                headers = mapOf(
                    "Accept" to listOf("application/json"),
                    "Authorization" to listOf("Bearer tok123"),
                ),
            )

            result shouldContain "-H 'Accept: application/json'"
            result shouldContain "-H 'Authorization: Bearer tok123'"
        }

        @Test
        fun `should expand multi-value headers into separate -H flags`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com",
                headers = mapOf("Accept" to listOf("text/html", "application/json")),
            )

            result shouldContain "-H 'Accept: text/html'"
            result shouldContain "-H 'Accept: application/json'"
        }

        @Test
        fun `should handle empty headers map`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com",
                headers = emptyMap(),
            )

            result shouldNotContain "-H"
        }
    }

    @Nested
    inner class ShellEscaping {

        @Test
        fun `should escape single quotes in header values`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com",
                headers = mapOf("Cookie" to listOf("name='value'")),
            )

            result shouldContain "-H 'Cookie: name='\\''value'\\'''"
        }

        @Test
        fun `should escape single quotes in URL`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com/search?q=it's",
                headers = emptyMap(),
            )

            result shouldContain "'https://example.com/search?q=it'\\''s'"
        }

        @Test
        fun `should escape single quotes in body`() {
            val result = CurlGenerator.generate(
                method = "POST",
                url = "https://example.com",
                headers = emptyMap(),
                body = """{"message":"it's working"}""",
            )

            result shouldContain "-d '{\"message\":\"it'\\''s working\"}'"
        }

        @Test
        fun `should safely handle dollar signs in URL (no variable expansion)`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://example.com/\$PATH",
                headers = emptyMap(),
            )

            // Single quotes prevent variable expansion
            result shouldContain "'https://example.com/\$PATH'"
        }

        @Test
        fun `should safely handle backticks in body`() {
            val result = CurlGenerator.generate(
                method = "POST",
                url = "https://example.com",
                headers = emptyMap(),
                body = "`whoami`",
            )

            // Single quotes prevent command substitution
            result shouldContain "-d '`whoami`'"
        }
    }

    @Nested
    inner class MethodSanitization {

        @Test
        fun `should sanitize method with special characters`() {
            val result = CurlGenerator.generate(
                method = "GET;rm -rf /",
                url = "https://example.com",
                headers = emptyMap(),
            )

            result shouldStartWith "curl -X GETrmrf"
            result shouldNotContain ";"
            result shouldNotContain " -rf"
        }

        @Test
        fun `should sanitize method with shell metacharacters`() {
            val result = CurlGenerator.generate(
                method = "POST\$(whoami)",
                url = "https://example.com",
                headers = emptyMap(),
            )

            result shouldStartWith "curl -X POSTwhoami"
            result shouldNotContain "\$"
            result shouldNotContain "("
        }

        @Test
        fun `should preserve normal method names`() {
            CurlGenerator.sanitizeMethod("GET") shouldBe "GET"
            CurlGenerator.sanitizeMethod("POST") shouldBe "POST"
            CurlGenerator.sanitizeMethod("PUT") shouldBe "PUT"
            CurlGenerator.sanitizeMethod("PATCH") shouldBe "PATCH"
            CurlGenerator.sanitizeMethod("DELETE") shouldBe "DELETE"
            CurlGenerator.sanitizeMethod("HEAD") shouldBe "HEAD"
            CurlGenerator.sanitizeMethod("OPTIONS") shouldBe "OPTIONS"
        }

        @Test
        fun `should fall back to GET for all-numeric method`() {
            CurlGenerator.sanitizeMethod("123") shouldBe "GET"
        }

        @Test
        fun `should fall back to GET for empty method`() {
            CurlGenerator.sanitizeMethod("") shouldBe "GET"
        }
    }

    @Nested
    inner class ShellQuote {

        @Test
        fun `should wrap simple string in single quotes`() {
            CurlGenerator.shellQuote("hello") shouldBe "'hello'"
        }

        @Test
        fun `should escape single quotes within string`() {
            CurlGenerator.shellQuote("it's") shouldBe "'it'\\''s'"
        }

        @Test
        fun `should handle multiple single quotes`() {
            CurlGenerator.shellQuote("a'b'c") shouldBe "'a'\\''b'\\''c'"
        }

        @Test
        fun `should handle empty string`() {
            CurlGenerator.shellQuote("") shouldBe "''"
        }

        @Test
        fun `should preserve double quotes without escaping`() {
            CurlGenerator.shellQuote("""he said "hi"""") shouldBe """'he said "hi"'"""
        }
    }

    @Nested
    inner class FullCommandIntegration {

        @Test
        fun `should generate complete POST with all components`() {
            val result = CurlGenerator.generate(
                method = "POST",
                url = "https://api.example.com/v2/users",
                headers = mapOf(
                    "Content-Type" to listOf("application/json"),
                    "Authorization" to listOf("Bearer eyJ0eXAi..."),
                    "Accept" to listOf("application/json"),
                ),
                body = """{"name":"John Doe","email":"john@example.com"}""",
            )

            result shouldStartWith "curl -X POST 'https://api.example.com/v2/users'"
            result shouldContain "-H 'Content-Type: application/json'"
            result shouldContain "-H 'Authorization: Bearer eyJ0eXAi...'"
            result shouldContain "-H 'Accept: application/json'"
            result shouldContain """-d '{"name":"John Doe","email":"john@example.com"}'"""
        }

        @Test
        fun `should generate URL with query parameters`() {
            val result = CurlGenerator.generate(
                method = "GET",
                url = "https://api.example.com/search?q=hello+world&page=1&limit=20",
                headers = emptyMap(),
            )

            result shouldBe "curl -X GET 'https://api.example.com/search?q=hello+world&page=1&limit=20'"
        }
    }
}
