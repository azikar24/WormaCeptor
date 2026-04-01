package com.azikar24.wormaceptor.api

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RedactionConfigTest {

    @Nested
    inner class `redactHeader` {

        @Test
        fun `stores header name in lowercase`() {
            val config = RedactionConfig()
            config.redactHeader("Authorization")

            config.headersToRedact shouldContainExactly setOf("authorization")
        }

        @Test
        fun `case-insensitive matching treats mixed case as same header`() {
            val config = RedactionConfig()
            config.redactHeader("Authorization")
            config.redactHeader("AUTHORIZATION")
            config.redactHeader("authorization")

            config.headersToRedact.size shouldBe 1
            config.headersToRedact.first() shouldBe "authorization"
        }

        @Test
        fun `supports multiple distinct headers`() {
            val config = RedactionConfig()
            config.redactHeader("Authorization")
            config.redactHeader("Cookie")
            config.redactHeader("X-Api-Key")

            config.headersToRedact shouldBe setOf("authorization", "cookie", "x-api-key")
        }

        @Test
        fun `returns the same instance for chaining`() {
            val config = RedactionConfig()
            val result = config.redactHeader("Authorization")

            result shouldBeSameInstanceAs config
        }
    }

    @Nested
    inner class `redactJsonValue` {

        @Test
        fun `replaces quoted string JSON value`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")

            val input = """{"password":"secret123","user":"john"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"password":"********","user":"john"}"""
        }

        @Test
        fun `replaces value with whitespace around colon`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")

            val input = """{"password" : "my secret"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"password" : "********"}"""
        }

        @Test
        fun `replaces non-string value like null`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")

            val input = """{"password":null,"user":"john"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"password":"********","user":"john"}"""
        }

        @Test
        fun `replaces numeric value`() {
            val config = RedactionConfig()
            config.redactJsonValue("pin")

            val input = """{"pin":1234,"name":"test"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"pin":"********","name":"test"}"""
        }

        @Test
        fun `replaces boolean value`() {
            val config = RedactionConfig()
            config.redactJsonValue("secret_flag")

            val input = """{"secret_flag":true}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"secret_flag":"********"}"""
        }

        @Test
        fun `is case-insensitive for key matching`() {
            val config = RedactionConfig()
            config.redactJsonValue("Password")

            val input = """{"password":"secret"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"password":"********"}"""
        }

        @Test
        fun `handles escaped quotes inside string values`() {
            val config = RedactionConfig()
            config.redactJsonValue("token")

            val input = """{"token":"abc\"def","other":"val"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"token":"********","other":"val"}"""
        }

        @Test
        fun `does not affect unrelated keys`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")

            val input = """{"username":"admin","email":"admin@test.com"}"""
            val result = config.applyRedactions(input)

            result shouldBe input
        }

        @Test
        fun `returns the same instance for chaining`() {
            val config = RedactionConfig()
            val result = config.redactJsonValue("password")

            result shouldBeSameInstanceAs config
        }
    }

    @Nested
    inner class `redactXmlValue` {

        @Test
        fun `replaces XML element content`() {
            val config = RedactionConfig()
            config.redactXmlValue("password")

            val input = "<user><password>secret123</password><name>John</name></user>"
            val result = config.applyRedactions(input)

            result shouldBe "<user><password>********</password><name>John</name></user>"
        }

        @Test
        fun `is case-insensitive for tag matching`() {
            val config = RedactionConfig()
            config.redactXmlValue("password")

            val input = "<Password>secret123</Password>"
            val result = config.applyRedactions(input)

            result shouldBe "<Password>********</Password>"
        }

        @Test
        fun `preserves original tag case in output`() {
            val config = RedactionConfig()
            config.redactXmlValue("Token")

            val input = "<TOKEN>abc123</TOKEN>"
            val result = config.applyRedactions(input)

            result shouldBe "<TOKEN>********</TOKEN>"
        }

        @Test
        fun `handles multiline content`() {
            val config = RedactionConfig()
            config.redactXmlValue("secret")

            val input = "<secret>\n  multi\n  line\n</secret>"
            val result = config.applyRedactions(input)

            result shouldBe "<secret>********</secret>"
        }

        @Test
        fun `does not affect unrelated tags`() {
            val config = RedactionConfig()
            config.redactXmlValue("password")

            val input = "<username>admin</username><email>admin@test.com</email>"
            val result = config.applyRedactions(input)

            result shouldBe input
        }

        @Test
        fun `returns the same instance for chaining`() {
            val config = RedactionConfig()
            val result = config.redactXmlValue("token")

            result shouldBeSameInstanceAs config
        }
    }

    @Nested
    inner class `redactBody` {

        @Test
        fun `replaces regex match with replacement text`() {
            val config = RedactionConfig()
            config.redactBody("api_key=\\w+")

            val input = "https://example.com?api_key=abc123&other=value"
            val result = config.applyRedactions(input)

            result shouldBe "https://example.com?********&other=value"
        }

        @Test
        fun `replaces all occurrences of pattern`() {
            val config = RedactionConfig()
            config.redactBody("Bearer [A-Za-z0-9]+")

            val input = "Token: Bearer abc123, Also: Bearer xyz789"
            val result = config.applyRedactions(input)

            result shouldBe "Token: ********, Also: ********"
        }

        @Test
        fun `is case-insensitive`() {
            val config = RedactionConfig()
            config.redactBody("secret_\\w+")

            val input = "value=SECRET_TOKEN here"
            val result = config.applyRedactions(input)

            result shouldBe "value=******** here"
        }

        @Test
        fun `does not match when pattern is absent`() {
            val config = RedactionConfig()
            config.redactBody("ssn=\\d+-\\d+-\\d+")

            val input = "name=John&age=30"
            val result = config.applyRedactions(input)

            result shouldBe input
        }

        @Test
        fun `returns the same instance for chaining`() {
            val config = RedactionConfig()
            val result = config.redactBody("pattern")

            result shouldBeSameInstanceAs config
        }
    }

    @Nested
    inner class `replacement` {

        @Test
        fun `default replacement text is eight asterisks`() {
            val config = RedactionConfig()

            config.replacementText shouldBe "********"
        }

        @Test
        fun `changes replacement text for subsequent redactions`() {
            val config = RedactionConfig()
            config.replacement("[REDACTED]")
            config.redactJsonValue("password")

            val input = """{"password":"secret"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"password":"[REDACTED]"}"""
        }

        @Test
        fun `returns the same instance for chaining`() {
            val config = RedactionConfig()
            val result = config.replacement("[HIDDEN]")

            result shouldBeSameInstanceAs config
        }
    }

    @Nested
    inner class `chaining` {

        @Test
        fun `all methods return the same config instance`() {
            val config = RedactionConfig()

            val chained = config
                .redactHeader("Authorization")
                .redactHeader("Cookie")
                .redactJsonValue("password")
                .redactXmlValue("token")
                .redactBody("secret=\\w+")
                .replacement("[REDACTED]")

            chained shouldBeSameInstanceAs config
        }

        @Test
        fun `chained config accumulates all rules`() {
            val config = RedactionConfig()
                .redactHeader("Authorization")
                .redactHeader("Cookie")
                .redactJsonValue("password")
                .redactBody("ssn=\\d+")

            config.headersToRedact shouldBe setOf("authorization", "cookie")
            config.bodyRedactions.size shouldBe 2
        }
    }

    @Nested
    inner class `applyRedactions` {

        @Test
        fun `applies multiple JSON redactions in order`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")
            config.redactJsonValue("api_key")

            val input = """{"password":"s3cret","api_key":"key123","name":"test"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"password":"********","api_key":"********","name":"test"}"""
        }

        @Test
        fun `applies mixed JSON and XML redactions`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")
            config.redactXmlValue("token")

            val jsonInput = """{"password":"secret"}"""
            val xmlInput = "<auth><token>abc</token></auth>"

            config.applyRedactions(jsonInput) shouldBe """{"password":"********"}"""
            config.applyRedactions(xmlInput) shouldBe "<auth><token>********</token></auth>"
        }

        @Test
        fun `applies body pattern and JSON value redaction together`() {
            val config = RedactionConfig()
            config.redactBody("Bearer [A-Za-z0-9]+")
            config.redactJsonValue("secret")

            val input = """{"auth":"Bearer abc123","secret":"hide_me"}"""
            val result = config.applyRedactions(input)

            result shouldBe """{"auth":"********","secret":"********"}"""
        }

        @Test
        fun `uses custom replacement text across all redaction types`() {
            val config = RedactionConfig()
            config.replacement("[X]")
            config.redactJsonValue("pass")
            config.redactXmlValue("token")
            config.redactBody("key=\\w+")

            config.applyRedactions("""{"pass":"a"}""") shouldBe """{"pass":"[X]"}"""
            config.applyRedactions("<token>b</token>") shouldBe "<token>[X]</token>"
            config.applyRedactions("key=abc") shouldBe "[X]"
        }

        @Test
        fun `returns original text when no redactions are configured`() {
            val config = RedactionConfig()

            val input = """{"password":"secret","token":"abc"}"""
            config.applyRedactions(input) shouldBe input
        }

        @Test
        fun `returns original text when no patterns match`() {
            val config = RedactionConfig()
            config.redactJsonValue("nonexistent_key")

            val input = """{"username":"admin"}"""
            config.applyRedactions(input) shouldBe input
        }

        @Test
        fun `handles empty string input`() {
            val config = RedactionConfig()
            config.redactJsonValue("password")

            config.applyRedactions("") shouldBe ""
        }
    }
}
