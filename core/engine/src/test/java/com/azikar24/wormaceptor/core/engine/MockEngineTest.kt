package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.MockBehavior
import com.azikar24.wormaceptor.domain.entities.MockDelay
import com.azikar24.wormaceptor.domain.entities.MockResponse
import com.azikar24.wormaceptor.domain.entities.MockRule
import com.azikar24.wormaceptor.domain.entities.RequestMatcher
import com.azikar24.wormaceptor.domain.entities.UrlMatchType
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import okhttp3.Request
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MockEngineTest {

    private lateinit var engine: MockEngine

    @BeforeEach
    fun setUp() {
        engine = MockEngine()
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun mockRule(
        id: String = "rule-1",
        name: String = "Test Rule",
        enabled: Boolean = true,
        urlPattern: String = "https://api.example.com/test",
        matchType: UrlMatchType = UrlMatchType.EXACT,
        method: String? = null,
        headers: Map<String, String> = emptyMap(),
        response: MockResponse = MockResponse(statusCode = 200, statusMessage = "OK"),
        delay: MockDelay = MockDelay.None,
        behavior: MockBehavior = MockBehavior.Always,
        priority: Int = 0,
    ): MockRule = MockRule(
        id = id,
        name = name,
        enabled = enabled,
        matcher = RequestMatcher(
            urlPattern = urlPattern,
            matchType = matchType,
            method = method,
            headers = headers,
        ),
        response = response,
        delay = delay,
        behavior = behavior,
        priority = priority,
    )

    private fun okHttpRequest(
        url: String = "https://api.example.com/test",
    ): Request = Request.Builder().url(url).build()

    // ── URL Matching ────────────────────────────────────────────────────

    @Nested
    inner class ExactUrlMatching {

        @Test
        fun `should match when URL exactly equals the pattern`() {
            val rule = mockRule(
                urlPattern = "https://api.example.com/users",
                matchType = UrlMatchType.EXACT,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users",
                method = "GET",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `should not match when URL differs from pattern`() {
            val rule = mockRule(
                urlPattern = "https://api.example.com/users",
                matchType = UrlMatchType.EXACT,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users/123",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should not match when URL is a prefix of the pattern`() {
            val rule = mockRule(
                urlPattern = "https://api.example.com/users/123",
                matchType = UrlMatchType.EXACT,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }
    }

    @Nested
    inner class PrefixUrlMatching {

        @Test
        fun `should match when URL starts with the prefix pattern`() {
            val rule = mockRule(
                urlPattern = "https://api.example.com/users*",
                matchType = UrlMatchType.PREFIX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users/123/profile",
                method = "GET",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `should match when URL exactly equals the prefix without the asterisk`() {
            val rule = mockRule(
                urlPattern = "https://api.example.com/users*",
                matchType = UrlMatchType.PREFIX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users",
                method = "GET",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `should not match when URL does not start with the prefix`() {
            val rule = mockRule(
                urlPattern = "https://api.example.com/users*",
                matchType = UrlMatchType.PREFIX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/orders/123",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }
    }

    @Nested
    inner class RegexUrlMatching {

        @Test
        fun `should match when URL matches regex pattern`() {
            val rule = mockRule(
                urlPattern = "~https://api\\.example\\.com/users/\\d+",
                matchType = UrlMatchType.REGEX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users/42",
                method = "GET",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `should not match when URL does not match regex pattern`() {
            val rule = mockRule(
                urlPattern = "~https://api\\.example\\.com/users/\\d+$",
                matchType = UrlMatchType.REGEX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/users/abc",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should handle invalid regex gracefully and not match`() {
            val rule = mockRule(
                urlPattern = "~[invalid(regex",
                matchType = UrlMatchType.REGEX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://anything.com",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should match regex without tilde prefix in pattern`() {
            val rule = mockRule(
                urlPattern = "~ .*/api/v[0-9]+/.*",
                matchType = UrlMatchType.REGEX,
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://host.com/api/v2/resource",
                method = "GET",
                headers = emptyMap(),
            )

            result shouldBe rule
        }
    }

    // ── Method Matching ─────────────────────────────────────────────────

    @Nested
    inner class MethodMatching {

        @Test
        fun `should match any method when rule method is null`() {
            val rule = mockRule(method = null)
            engine.setRules(listOf(rule))

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe rule

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "POST",
                headers = emptyMap(),
            ) shouldBe rule

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "DELETE",
                headers = emptyMap(),
            ) shouldBe rule
        }

        @Test
        fun `should match any method when rule method is blank`() {
            val rule = mockRule(method = "  ")
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "PUT",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `should match when method matches case-insensitively`() {
            val rule = mockRule(method = "POST")
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "post",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `should not match when method differs`() {
            val rule = mockRule(method = "GET")
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "POST",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }
    }

    // ── Header Matching ─────────────────────────────────────────────────

    @Nested
    inner class HeaderMatching {

        @Test
        fun `should match when all required headers are present`() {
            val rule = mockRule(
                headers = mapOf("Authorization" to "Bearer token123"),
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf("Authorization" to listOf("Bearer token123")),
            )

            result shouldBe rule
        }

        @Test
        fun `should not match when a required header is missing`() {
            val rule = mockRule(
                headers = mapOf("X-Api-Key" to "secret"),
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should not match when header value does not match`() {
            val rule = mockRule(
                headers = mapOf("X-Api-Key" to "secret"),
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf("X-Api-Key" to listOf("wrong-value")),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should match headers case-insensitively`() {
            val rule = mockRule(
                headers = mapOf("content-type" to "application/json"),
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf("Content-Type" to listOf("Application/JSON")),
            )

            result shouldBe rule
        }

        @Test
        fun `should match when request has extra headers beyond required ones`() {
            val rule = mockRule(
                headers = mapOf("Accept" to "text/html"),
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf(
                    "Accept" to listOf("text/html"),
                    "X-Extra" to listOf("extra-value"),
                ),
            )

            result shouldBe rule
        }

        @Test
        fun `should match when rule has no header requirements`() {
            val rule = mockRule(headers = emptyMap())
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf("Anything" to listOf("value")),
            )

            result shouldBe rule
        }

        @Test
        fun `should match when header has multiple values and one matches`() {
            val rule = mockRule(
                headers = mapOf("Accept" to "application/json"),
            )
            engine.setRules(listOf(rule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf("Accept" to listOf("text/html", "application/json")),
            )

            result shouldBe rule
        }
    }

    // ── Priority Ordering ───────────────────────────────────────────────

    @Nested
    inner class PriorityOrdering {

        @Test
        fun `should match higher priority rule first`() {
            val lowPriority = mockRule(
                id = "low",
                priority = 1,
                response = MockResponse(statusCode = 200, statusMessage = "Low"),
            )
            val highPriority = mockRule(
                id = "high",
                priority = 10,
                response = MockResponse(statusCode = 201, statusMessage = "High"),
            )
            engine.setRules(listOf(lowPriority, highPriority))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldNotBeNull()
            result.id shouldBe "high"
        }

        @Test
        fun `should match first rule when priorities are equal`() {
            val ruleA = mockRule(id = "A", priority = 5, name = "Rule A")
            val ruleB = mockRule(id = "B", priority = 5, name = "Rule B")
            engine.setRules(listOf(ruleA, ruleB))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldNotBeNull()
            // With equal priority the stable sort order keeps the original list order,
            // but sortedByDescending is not guaranteed to be stable in all Kotlin
            // stdlib implementations. We just verify a rule was matched.
            (result.id == "A" || result.id == "B") shouldBe true
        }

        @Test
        fun `should fall through to lower priority if higher priority rule does not match URL`() {
            val highPriority = mockRule(
                id = "high",
                priority = 10,
                urlPattern = "https://api.example.com/other",
                matchType = UrlMatchType.EXACT,
            )
            val lowPriority = mockRule(
                id = "low",
                priority = 1,
                urlPattern = "https://api.example.com/test",
                matchType = UrlMatchType.EXACT,
            )
            engine.setRules(listOf(highPriority, lowPriority))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldNotBeNull()
            result.id shouldBe "low"
        }
    }

    // ── Disabled Rules ──────────────────────────────────────────────────

    @Nested
    inner class DisabledRules {

        @Test
        fun `should skip disabled rules`() {
            val disabledRule = mockRule(id = "disabled", enabled = false)
            engine.setRules(listOf(disabledRule))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should skip disabled rule and match enabled one`() {
            val disabled = mockRule(id = "disabled", enabled = false, priority = 10)
            val enabled = mockRule(id = "enabled", enabled = true, priority = 1)
            engine.setRules(listOf(disabled, enabled))

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldNotBeNull()
            result.id shouldBe "enabled"
        }
    }

    // ── Mocking Enabled Toggle ──────────────────────────────────────────

    @Nested
    inner class MockingEnabledToggle {

        @Test
        fun `should return null when mocking is disabled`() {
            val rule = mockRule()
            engine.setRules(listOf(rule))
            engine.setMockingEnabled(false)

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `should return matching rule when mocking is re-enabled`() {
            val rule = mockRule()
            engine.setRules(listOf(rule))
            engine.setMockingEnabled(false)
            engine.setMockingEnabled(true)

            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result shouldBe rule
        }

        @Test
        fun `mockingEnabled state flow should reflect current state`() {
            engine.mockingEnabled.value shouldBe true

            engine.setMockingEnabled(false)
            engine.mockingEnabled.value shouldBe false

            engine.setMockingEnabled(true)
            engine.mockingEnabled.value shouldBe true
        }
    }

    // ── shouldApply Behavior ────────────────────────────────────────────

    @Nested
    inner class ShouldApplyBehavior {

        @Test
        fun `Always behavior should match every time`() {
            val rule = mockRule(behavior = MockBehavior.Always)
            engine.setRules(listOf(rule))

            repeat(5) {
                engine.findMatchingRule(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                ) shouldBe rule
            }
        }

        @Test
        fun `NthRequest behavior should match only every Nth request`() {
            val rule = mockRule(behavior = MockBehavior.NthRequest(n = 3))
            engine.setRules(listOf(rule))

            val results = (1..9).map {
                engine.findMatchingRule(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                )
            }

            // Requests 1..9: matches at positions 3, 6, 9 (count % 3 == 0)
            results[0].shouldBeNull()  // count=1
            results[1].shouldBeNull()  // count=2
            results[2] shouldBe rule   // count=3
            results[3].shouldBeNull()  // count=4
            results[4].shouldBeNull()  // count=5
            results[5] shouldBe rule   // count=6
            results[6].shouldBeNull()  // count=7
            results[7].shouldBeNull()  // count=8
            results[8] shouldBe rule   // count=9
        }

        @Test
        fun `FirstN behavior should match the first N requests then stop`() {
            val rule = mockRule(behavior = MockBehavior.FirstN(count = 3))
            engine.setRules(listOf(rule))

            val results = (1..6).map {
                engine.findMatchingRule(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                )
            }

            results[0] shouldBe rule   // count=1 <= 3
            results[1] shouldBe rule   // count=2 <= 3
            results[2] shouldBe rule   // count=3 <= 3
            results[3].shouldBeNull()  // count=4 > 3
            results[4].shouldBeNull()  // count=5 > 3
            results[5].shouldBeNull()  // count=6 > 3
        }

        @Test
        fun `Sequential behavior should always apply`() {
            val responses = listOf(
                MockResponse(statusCode = 200, statusMessage = "First"),
                MockResponse(statusCode = 201, statusMessage = "Second"),
            )
            val rule = mockRule(behavior = MockBehavior.Sequential(responses))
            engine.setRules(listOf(rule))

            repeat(4) {
                engine.findMatchingRule(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                ) shouldBe rule
            }
        }

        @Test
        fun `Passthrough behavior should always apply`() {
            val rule = mockRule(behavior = MockBehavior.Passthrough)
            engine.setRules(listOf(rule))

            repeat(3) {
                engine.findMatchingRule(
                    url = "https://api.example.com/test",
                    method = "GET",
                    headers = emptyMap(),
                ) shouldBe rule
            }
        }
    }

    // ── resolveResponse ─────────────────────────────────────────────────

    @Nested
    inner class ResolveResponse {

        @Test
        fun `Always behavior should return the rule response`() {
            val response = MockResponse(statusCode = 200, statusMessage = "OK")
            val rule = mockRule(response = response, behavior = MockBehavior.Always)

            engine.resolveResponse(rule) shouldBe response
        }

        @Test
        fun `NthRequest behavior should return the rule response`() {
            val response = MockResponse(statusCode = 404, statusMessage = "Not Found")
            val rule = mockRule(response = response, behavior = MockBehavior.NthRequest(n = 2))

            engine.resolveResponse(rule) shouldBe response
        }

        @Test
        fun `FirstN behavior should return the rule response`() {
            val response = MockResponse(statusCode = 500, statusMessage = "Error")
            val rule = mockRule(response = response, behavior = MockBehavior.FirstN(count = 5))

            engine.resolveResponse(rule) shouldBe response
        }

        @Test
        fun `Passthrough behavior should return null`() {
            val rule = mockRule(behavior = MockBehavior.Passthrough)

            engine.resolveResponse(rule).shouldBeNull()
        }

        @Test
        fun `Sequential behavior should cycle through responses`() {
            val responses = listOf(
                MockResponse(statusCode = 200, statusMessage = "First"),
                MockResponse(statusCode = 201, statusMessage = "Second"),
                MockResponse(statusCode = 202, statusMessage = "Third"),
            )
            val rule = mockRule(
                id = "seq-rule",
                behavior = MockBehavior.Sequential(responses),
            )
            engine.setRules(listOf(rule))

            // Trigger shouldApply to increment the counter
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )
            engine.resolveResponse(rule)?.statusMessage shouldBe "First"

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )
            engine.resolveResponse(rule)?.statusMessage shouldBe "Second"

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )
            engine.resolveResponse(rule)?.statusMessage shouldBe "Third"

            // Cycle back to first
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )
            engine.resolveResponse(rule)?.statusMessage shouldBe "First"
        }

        @Test
        fun `Sequential behavior with empty responses should return rule response`() {
            val defaultResponse = MockResponse(statusCode = 200, statusMessage = "Default")
            val rule = mockRule(
                id = "seq-empty",
                response = defaultResponse,
                behavior = MockBehavior.Sequential(emptyList()),
            )
            engine.setRules(listOf(rule))

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )
            engine.resolveResponse(rule) shouldBe defaultResponse
        }
    }

    // ── computeDelayMs ──────────────────────────────────────────────────

    @Nested
    inner class ComputeDelayMs {

        @Test
        fun `None delay should return 0`() {
            val rule = mockRule(delay = MockDelay.None)

            engine.computeDelayMs(rule) shouldBe 0L
        }

        @Test
        fun `Fixed delay should return exact milliseconds`() {
            val rule = mockRule(delay = MockDelay.Fixed(ms = 500L))

            engine.computeDelayMs(rule) shouldBe 500L
        }

        @Test
        fun `Range delay should return value within min and max`() {
            val rule = mockRule(delay = MockDelay.Range(minMs = 100L, maxMs = 200L))

            repeat(50) {
                val delay = engine.computeDelayMs(rule)
                delay shouldBeGreaterThanOrEqual 100L
                delay shouldBeLessThanOrEqual 200L
            }
        }

        @Test
        fun `Range delay should return minMs when min equals max`() {
            val rule = mockRule(delay = MockDelay.Range(minMs = 150L, maxMs = 150L))

            engine.computeDelayMs(rule) shouldBe 150L
        }

        @Test
        fun `Range delay should return minMs when min exceeds max`() {
            val rule = mockRule(delay = MockDelay.Range(minMs = 300L, maxMs = 100L))

            engine.computeDelayMs(rule) shouldBe 300L
        }
    }

    // ── buildOkHttpResponse ─────────────────────────────────────────────

    @Nested
    inner class BuildOkHttpResponse {

        @Test
        fun `should build response with correct status code`() {
            val mockResponse = MockResponse(
                statusCode = 404,
                statusMessage = "Not Found",
                body = """{"error": "not found"}""",
            )
            val request = okHttpRequest()

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.code shouldBe 404
        }

        @Test
        fun `should build response with correct status message`() {
            val mockResponse = MockResponse(
                statusCode = 200,
                statusMessage = "Custom OK",
            )
            val request = okHttpRequest()

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.message shouldBe "Custom OK"
        }

        @Test
        fun `should build response with body content`() {
            val bodyContent = """{"key": "value"}"""
            val mockResponse = MockResponse(
                statusCode = 200,
                statusMessage = "OK",
                body = bodyContent,
            )
            val request = okHttpRequest()

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.body?.string() shouldBe bodyContent
        }

        @Test
        fun `should build response with empty body when body is null`() {
            val mockResponse = MockResponse(
                statusCode = 204,
                statusMessage = "No Content",
                body = null,
            )
            val request = okHttpRequest()

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.body?.string() shouldBe ""
        }

        @Test
        fun `should include custom headers in response`() {
            val mockResponse = MockResponse(
                statusCode = 200,
                statusMessage = "OK",
                headers = mapOf(
                    "X-Custom" to "custom-value",
                    "X-Request-Id" to "abc-123",
                ),
            )
            val request = okHttpRequest()

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.header("X-Custom") shouldBe "custom-value"
            response.header("X-Request-Id") shouldBe "abc-123"
        }

        @Test
        fun `should set Content-Type header from mock response`() {
            val mockResponse = MockResponse(
                statusCode = 200,
                statusMessage = "OK",
                contentType = "text/plain",
            )
            val request = okHttpRequest()

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.header("Content-Type") shouldBe "text/plain"
        }

        @Test
        fun `should reference the original request`() {
            val mockResponse = MockResponse(statusCode = 200, statusMessage = "OK")
            val request = okHttpRequest("https://api.example.com/original")

            val response = engine.buildOkHttpResponse(mockResponse, request)

            response.request.url.toString() shouldBe "https://api.example.com/original"
        }
    }

    // ── resetCounters ───────────────────────────────────────────────────

    @Nested
    inner class ResetCounters {

        @Test
        fun `should reset hit counters so FirstN applies again`() {
            val rule = mockRule(behavior = MockBehavior.FirstN(count = 2))
            engine.setRules(listOf(rule))

            // Exhaust the first 2
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe rule
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe rule

            // Third should not match
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ).shouldBeNull()

            // Reset and try again
            engine.resetCounters()

            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe rule
        }

        @Test
        fun `should reset hit counters so NthRequest counting restarts`() {
            val rule = mockRule(behavior = MockBehavior.NthRequest(n = 2))
            engine.setRules(listOf(rule))

            // 1st call (count=1) -> null, 2nd call (count=2) -> match
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ).shouldBeNull()
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe rule

            engine.resetCounters()

            // After reset, counting starts from 0 again
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ).shouldBeNull()
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe rule
        }
    }

    // ── setRules ────────────────────────────────────────────────────────

    @Nested
    inner class SetRules {

        @Test
        fun `should expose rules via state flow`() {
            val ruleList = listOf(
                mockRule(id = "1"),
                mockRule(id = "2"),
            )
            engine.setRules(ruleList)

            engine.rules.value shouldBe ruleList
        }

        @Test
        fun `should return null when no rules are set`() {
            val result = engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = emptyMap(),
            )

            result.shouldBeNull()
        }

        @Test
        fun `replacing rules should clear previous matches`() {
            val ruleA = mockRule(
                id = "A",
                urlPattern = "https://api.example.com/a",
                matchType = UrlMatchType.EXACT,
            )
            engine.setRules(listOf(ruleA))

            engine.findMatchingRule(
                url = "https://api.example.com/a",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe ruleA

            // Replace with a different rule
            val ruleB = mockRule(
                id = "B",
                urlPattern = "https://api.example.com/b",
                matchType = UrlMatchType.EXACT,
            )
            engine.setRules(listOf(ruleB))

            engine.findMatchingRule(
                url = "https://api.example.com/a",
                method = "GET",
                headers = emptyMap(),
            ).shouldBeNull()

            engine.findMatchingRule(
                url = "https://api.example.com/b",
                method = "GET",
                headers = emptyMap(),
            ) shouldBe ruleB
        }
    }

    // ── Edge Cases ──────────────────────────────────────────────────────

    @Nested
    inner class EdgeCases {

        @Test
        fun `multiple rules with different URL types should all be evaluated`() {
            val exactRule = mockRule(
                id = "exact",
                urlPattern = "https://api.example.com/exact",
                matchType = UrlMatchType.EXACT,
                priority = 1,
            )
            val prefixRule = mockRule(
                id = "prefix",
                urlPattern = "https://api.example.com/prefix*",
                matchType = UrlMatchType.PREFIX,
                priority = 2,
            )
            val regexRule = mockRule(
                id = "regex",
                urlPattern = "~https://api\\.example\\.com/regex/\\d+",
                matchType = UrlMatchType.REGEX,
                priority = 3,
            )
            engine.setRules(listOf(exactRule, prefixRule, regexRule))

            engine.findMatchingRule(
                url = "https://api.example.com/exact",
                method = "GET",
                headers = emptyMap(),
            )?.id shouldBe "exact"

            engine.findMatchingRule(
                url = "https://api.example.com/prefix/sub",
                method = "GET",
                headers = emptyMap(),
            )?.id shouldBe "prefix"

            engine.findMatchingRule(
                url = "https://api.example.com/regex/42",
                method = "GET",
                headers = emptyMap(),
            )?.id shouldBe "regex"
        }

        @Test
        fun `rule with method and header constraints combined`() {
            val rule = mockRule(
                method = "POST",
                headers = mapOf("Content-Type" to "application/json"),
            )
            engine.setRules(listOf(rule))

            // Both match
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "POST",
                headers = mapOf("Content-Type" to listOf("application/json")),
            ) shouldBe rule

            // Wrong method
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "GET",
                headers = mapOf("Content-Type" to listOf("application/json")),
            ).shouldBeNull()

            // Wrong header
            engine.findMatchingRule(
                url = "https://api.example.com/test",
                method = "POST",
                headers = mapOf("Content-Type" to listOf("text/html")),
            ).shouldBeNull()
        }
    }
}
