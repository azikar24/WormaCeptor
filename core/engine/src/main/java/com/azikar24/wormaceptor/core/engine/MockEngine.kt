package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.MockBehavior
import com.azikar24.wormaceptor.domain.entities.MockDelay
import com.azikar24.wormaceptor.domain.entities.MockResponse
import com.azikar24.wormaceptor.domain.entities.MockRule
import com.azikar24.wormaceptor.domain.entities.UrlMatchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * Engine that evaluates mock rules against incoming HTTP requests and builds OkHttp responses.
 *
 * Thread-safe: rules and hit counters use concurrent data structures.
 */
class MockEngine {

    private val _mockingEnabled = MutableStateFlow(true)

    /** Whether mocking is globally enabled. */
    val mockingEnabled: StateFlow<Boolean> = _mockingEnabled.asStateFlow()

    private val _rules = MutableStateFlow<List<MockRule>>(emptyList())

    /** The current list of mock rules. */
    val rules: StateFlow<List<MockRule>> = _rules.asStateFlow()

    /** Tracks how many times each rule has been matched (rule id -> count). */
    private val hitCounters = ConcurrentHashMap<String, AtomicInteger>()

    /** Enables or disables all mocking globally. */
    fun setMockingEnabled(enabled: Boolean) {
        _mockingEnabled.value = enabled
    }

    /** Replaces all rules with the given list. */
    fun setRules(rules: List<MockRule>) {
        _rules.value = rules
    }

    /**
     * Finds the highest-priority enabled rule that matches the given request parameters.
     *
     * @param url The full request URL.
     * @param method The HTTP method (GET, POST, etc.).
     * @param headers The request headers.
     * @return The matching [MockRule], or null if no rule matches or mocking is disabled.
     */
    fun findMatchingRule(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
    ): MockRule? {
        if (!_mockingEnabled.value) return null

        val enabledRules = _rules.value
            .filter { it.enabled }
            .sortedByDescending { it.priority }

        for (rule in enabledRules) {
            if (matchesUrl(url, rule.matcher.urlPattern, rule.matcher.matchType) &&
                matchesMethod(method, rule.matcher.method) &&
                matchesHeaders(headers, rule.matcher.headers)
            ) {
                if (shouldApply(rule)) {
                    return rule
                }
            }
        }
        return null
    }

    /**
     * Resolves the effective [MockResponse] for the given rule, accounting for
     * [MockBehavior.Sequential] cycling.
     *
     * @param rule The matched mock rule.
     * @return The [MockResponse] to use, or null if the behavior is [MockBehavior.Passthrough].
     */
    fun resolveResponse(rule: MockRule): MockResponse? {
        return when (val behavior = rule.behavior) {
            is MockBehavior.Always,
            is MockBehavior.NthRequest,
            is MockBehavior.FirstN,
            -> rule.response

            is MockBehavior.Sequential -> {
                val responses = behavior.responses
                if (responses.isEmpty()) return rule.response
                val counter = hitCounters.getOrPut(rule.id) { AtomicInteger(0) }
                // The counter was already incremented in shouldApply, so use (count - 1)
                val index = (counter.get() - 1).mod(responses.size)
                responses[index]
            }

            is MockBehavior.Passthrough -> null
        }
    }

    /**
     * Computes the delay in milliseconds for the given rule.
     */
    fun computeDelayMs(rule: MockRule): Long {
        return when (val delay = rule.delay) {
            is MockDelay.None -> 0L
            is MockDelay.Fixed -> delay.ms
            is MockDelay.Range -> {
                if (delay.minMs >= delay.maxMs) {
                    delay.minMs
                } else {
                    Random.nextLong(delay.minMs, delay.maxMs + 1)
                }
            }
        }
    }

    /**
     * Builds an OkHttp [Response] from a [MockResponse] and the original [Request].
     *
     * @param mockResponse The mock response definition.
     * @param request The original OkHttp request.
     * @return A fully constructed OkHttp [Response].
     */
    fun buildOkHttpResponse(
        mockResponse: MockResponse,
        request: Request,
    ): Response {
        val mediaType = mockResponse.contentType.toMediaTypeOrNull()
        val body = (mockResponse.body ?: "").toResponseBody(mediaType)

        val headersBuilder = Headers.Builder()
        for ((name, value) in mockResponse.headers) {
            headersBuilder.add(name, value)
        }
        headersBuilder.set("Content-Type", mockResponse.contentType)

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(mockResponse.statusCode)
            .message(mockResponse.statusMessage)
            .headers(headersBuilder.build())
            .body(body)
            .build()
    }

    /** Resets all hit counters. */
    fun resetCounters() {
        hitCounters.clear()
    }

    private fun matchesUrl(
        url: String,
        pattern: String,
        matchType: UrlMatchType,
    ): Boolean {
        return when (matchType) {
            UrlMatchType.EXACT -> url == pattern
            UrlMatchType.PREFIX -> {
                val prefix = pattern.trimEnd('*')
                url.startsWith(prefix)
            }
            UrlMatchType.REGEX -> {
                val regexPattern = pattern.trimStart('~').trim()
                try {
                    Regex(regexPattern).containsMatchIn(url)
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    private fun matchesMethod(
        requestMethod: String,
        ruleMethod: String?,
    ): Boolean {
        if (ruleMethod.isNullOrBlank()) return true
        return requestMethod.equals(ruleMethod, ignoreCase = true)
    }

    private fun matchesHeaders(
        requestHeaders: Map<String, List<String>>,
        ruleHeaders: Map<String, String>,
    ): Boolean {
        for ((name, expectedValue) in ruleHeaders) {
            val actualValues = requestHeaders.entries
                .firstOrNull { it.key.equals(name, ignoreCase = true) }
                ?.value
                ?: return false
            if (actualValues.none { it.equals(expectedValue, ignoreCase = true) }) {
                return false
            }
        }
        return true
    }

    /**
     * Determines whether a rule should be applied for the current request, based on its behavior.
     * Increments the hit counter as a side effect.
     */
    private fun shouldApply(rule: MockRule): Boolean {
        val counter = hitCounters.getOrPut(rule.id) { AtomicInteger(0) }
        val count = counter.incrementAndGet()

        return when (val behavior = rule.behavior) {
            is MockBehavior.Always -> true
            is MockBehavior.NthRequest -> count % behavior.n == 0
            is MockBehavior.FirstN -> count <= behavior.count
            is MockBehavior.Sequential -> true
            is MockBehavior.Passthrough -> true
        }
    }
}
