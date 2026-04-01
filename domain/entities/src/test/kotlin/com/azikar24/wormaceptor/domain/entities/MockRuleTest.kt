package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MockRuleTest {

    private fun defaultMatcher() = RequestMatcher(urlPattern = "https://api.example.com/*")

    private fun defaultResponse() = MockResponse()

    private fun defaultRule() = MockRule(
        name = "Test Rule",
        matcher = defaultMatcher(),
        response = defaultResponse(),
    )

    @Nested
    inner class MockRuleConstruction {

        @Test
        fun `constructs with minimal required fields`() {
            val rule = defaultRule()

            rule.name shouldBe "Test Rule"
            rule.matcher.urlPattern shouldBe "https://api.example.com/*"
        }

        @Test
        fun `constructs with all fields`() {
            val rule = MockRule(
                id = "custom-id",
                name = "Full Rule",
                enabled = false,
                matcher = RequestMatcher(
                    urlPattern = "/api/users",
                    matchType = UrlMatchType.EXACT,
                    method = "POST",
                    headers = mapOf("Authorization" to "Bearer token"),
                ),
                response = MockResponse(
                    statusCode = 201,
                    statusMessage = "Created",
                    headers = mapOf("Location" to "/api/users/1"),
                    body = """{"id": 1}""",
                    contentType = "application/json",
                ),
                delay = MockDelay.Fixed(ms = 500L),
                behavior = MockBehavior.FirstN(count = 3),
                priority = 10,
                createdAt = 1_700_000_000_000L,
            )

            rule.id shouldBe "custom-id"
            rule.name shouldBe "Full Rule"
            rule.enabled shouldBe false
            rule.priority shouldBe 10
            rule.createdAt shouldBe 1_700_000_000_000L
        }
    }

    @Nested
    inner class MockRuleDefaults {

        @Test
        fun `id defaults to a non-blank UUID string`() {
            val rule = defaultRule()

            rule.id.shouldNotBeBlank()
        }

        @Test
        fun `two rules get different default ids`() {
            val rule1 = defaultRule()
            val rule2 = defaultRule()

            rule1.id shouldNotBe rule2.id
        }

        @Test
        fun `enabled defaults to true`() {
            val rule = defaultRule()

            rule.enabled shouldBe true
        }

        @Test
        fun `delay defaults to None`() {
            val rule = defaultRule()

            rule.delay shouldBe MockDelay.None
        }

        @Test
        fun `behavior defaults to Always`() {
            val rule = defaultRule()

            rule.behavior shouldBe MockBehavior.Always
        }

        @Test
        fun `priority defaults to 0`() {
            val rule = defaultRule()

            rule.priority shouldBe 0
        }

        @Test
        fun `createdAt defaults to current time`() {
            val before = System.currentTimeMillis()
            val rule = defaultRule()
            val after = System.currentTimeMillis()

            (rule.createdAt in before..after) shouldBe true
        }
    }

    @Nested
    inner class MockRuleCopy {

        @Test
        fun `copy can disable a rule`() {
            val original = defaultRule()
            val disabled = original.copy(enabled = false)

            original.enabled shouldBe true
            disabled.enabled shouldBe false
            disabled.id shouldBe original.id
        }

        @Test
        fun `copy can change priority`() {
            val original = defaultRule()
            val updated = original.copy(priority = 5)

            updated.priority shouldBe 5
            updated.name shouldBe original.name
        }
    }

    @Nested
    inner class RequestMatcherConstruction {

        @Test
        fun `minimal construction with only urlPattern`() {
            val matcher = RequestMatcher(urlPattern = "/api/*")

            matcher.urlPattern shouldBe "/api/*"
        }

        @Test
        fun `full construction`() {
            val matcher = RequestMatcher(
                urlPattern = "~^/api/v[0-9]+/users$",
                matchType = UrlMatchType.REGEX,
                method = "DELETE",
                headers = mapOf("X-Custom" to "value"),
            )

            matcher.urlPattern shouldBe "~^/api/v[0-9]+/users$"
            matcher.matchType shouldBe UrlMatchType.REGEX
            matcher.method shouldBe "DELETE"
            matcher.headers shouldBe mapOf("X-Custom" to "value")
        }
    }

    @Nested
    inner class RequestMatcherDefaults {

        @Test
        fun `matchType defaults to PREFIX`() {
            val matcher = RequestMatcher(urlPattern = "/api/*")

            matcher.matchType shouldBe UrlMatchType.PREFIX
        }

        @Test
        fun `method defaults to null`() {
            val matcher = RequestMatcher(urlPattern = "/api/*")

            matcher.method shouldBe null
        }

        @Test
        fun `headers defaults to empty map`() {
            val matcher = RequestMatcher(urlPattern = "/api/*")

            matcher.headers shouldBe emptyMap()
        }
    }

    @Nested
    inner class UrlMatchTypeEnum {

        @Test
        fun `should have exactly three values`() {
            UrlMatchType.entries.size shouldBe 3
        }

        @Test
        fun `should contain EXACT, PREFIX, and REGEX`() {
            UrlMatchType.entries.map { it.name } shouldContainExactly listOf(
                "EXACT",
                "PREFIX",
                "REGEX",
            )
        }
    }

    @Nested
    inner class MockResponseConstruction {

        @Test
        fun `constructs with defaults`() {
            val response = MockResponse()

            response.statusCode shouldBe 200
            response.statusMessage shouldBe "OK"
            response.headers shouldBe emptyMap()
            response.body shouldBe null
            response.contentType shouldBe "application/json"
        }

        @Test
        fun `constructs with custom values`() {
            val response = MockResponse(
                statusCode = 404,
                statusMessage = "Not Found",
                headers = mapOf("X-Error" to "resource-missing"),
                body = """{"error": "not found"}""",
                contentType = "application/problem+json",
            )

            response.statusCode shouldBe 404
            response.statusMessage shouldBe "Not Found"
            response.headers shouldBe mapOf("X-Error" to "resource-missing")
            response.body shouldBe """{"error": "not found"}"""
            response.contentType shouldBe "application/problem+json"
        }
    }

    @Nested
    inner class MockDelaySealedClass {

        @Test
        fun `None is a singleton`() {
            MockDelay.None shouldBe MockDelay.None
        }

        @Test
        fun `Fixed stores ms value`() {
            val delay = MockDelay.Fixed(ms = 500L)

            delay.ms shouldBe 500L
        }

        @Test
        fun `Fixed equality works`() {
            MockDelay.Fixed(ms = 100L) shouldBe MockDelay.Fixed(ms = 100L)
            MockDelay.Fixed(ms = 100L) shouldNotBe MockDelay.Fixed(ms = 200L)
        }

        @Test
        fun `Range stores min and max`() {
            val delay = MockDelay.Range(minMs = 100L, maxMs = 500L)

            delay.minMs shouldBe 100L
            delay.maxMs shouldBe 500L
        }

        @Test
        fun `Range equality works`() {
            MockDelay.Range(100L, 500L) shouldBe MockDelay.Range(100L, 500L)
            MockDelay.Range(100L, 500L) shouldNotBe MockDelay.Range(100L, 600L)
        }

        @Test
        fun `all subtypes are instances of MockDelay`() {
            val none: MockDelay = MockDelay.None
            val fixed: MockDelay = MockDelay.Fixed(ms = 0)
            val range: MockDelay = MockDelay.Range(minMs = 0, maxMs = 1)

            none.shouldBeInstanceOf<MockDelay>()
            fixed.shouldBeInstanceOf<MockDelay>()
            range.shouldBeInstanceOf<MockDelay>()
        }

        @Test
        fun `exhaustive when covers all subtypes`() {
            val delays = listOf<MockDelay>(
                MockDelay.None,
                MockDelay.Fixed(100),
                MockDelay.Range(100, 200),
            )

            val descriptions = delays.map { delay ->
                when (delay) {
                    is MockDelay.None -> "none"
                    is MockDelay.Fixed -> "fixed:${delay.ms}"
                    is MockDelay.Range -> "range:${delay.minMs}-${delay.maxMs}"
                }
            }

            descriptions shouldBe listOf("none", "fixed:100", "range:100-200")
        }
    }

    @Nested
    inner class MockBehaviorSealedClass {

        @Test
        fun `Always is a singleton`() {
            MockBehavior.Always shouldBe MockBehavior.Always
        }

        @Test
        fun `Passthrough is a singleton`() {
            MockBehavior.Passthrough shouldBe MockBehavior.Passthrough
        }

        @Test
        fun `NthRequest stores n`() {
            val behavior = MockBehavior.NthRequest(n = 3)

            behavior.n shouldBe 3
        }

        @Test
        fun `FirstN stores count`() {
            val behavior = MockBehavior.FirstN(count = 5)

            behavior.count shouldBe 5
        }

        @Test
        fun `Sequential stores responses list`() {
            val responses = listOf(
                MockResponse(statusCode = 200),
                MockResponse(statusCode = 500),
            )
            val behavior = MockBehavior.Sequential(responses = responses)

            behavior.responses.size shouldBe 2
            behavior.responses[0].statusCode shouldBe 200
            behavior.responses[1].statusCode shouldBe 500
        }

        @Test
        fun `Sequential equality works`() {
            val responses = listOf(MockResponse(statusCode = 200))
            MockBehavior.Sequential(responses) shouldBe MockBehavior.Sequential(responses)
        }

        @Test
        fun `exhaustive when covers all subtypes`() {
            val behaviors = listOf<MockBehavior>(
                MockBehavior.Always,
                MockBehavior.NthRequest(2),
                MockBehavior.FirstN(3),
                MockBehavior.Sequential(listOf(MockResponse())),
                MockBehavior.Passthrough,
            )

            val descriptions = behaviors.map { behavior ->
                when (behavior) {
                    is MockBehavior.Always -> "always"
                    is MockBehavior.NthRequest -> "nth:${behavior.n}"
                    is MockBehavior.FirstN -> "firstN:${behavior.count}"
                    is MockBehavior.Sequential -> "sequential:${behavior.responses.size}"
                    is MockBehavior.Passthrough -> "passthrough"
                }
            }

            descriptions shouldBe listOf("always", "nth:2", "firstN:3", "sequential:1", "passthrough")
        }
    }
}
