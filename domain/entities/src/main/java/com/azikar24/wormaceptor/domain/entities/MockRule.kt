package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

/**
 * Defines a mock rule that intercepts matching HTTP requests and returns a configured response.
 *
 * @property id Unique identifier for this rule.
 * @property name Human-readable display name for the rule.
 * @property enabled Whether this rule is active.
 * @property matcher Criteria for matching incoming requests.
 * @property response The mock response to return when a request matches.
 * @property delay Optional artificial delay before returning the response.
 * @property behavior Controls when and how the mock is applied.
 * @property priority Higher-priority rules are evaluated first. Higher value = higher priority.
 * @property createdAt Epoch millis when the rule was created.
 */
data class MockRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val enabled: Boolean = true,
    val matcher: RequestMatcher,
    val response: MockResponse,
    val delay: MockDelay = MockDelay.None,
    val behavior: MockBehavior = MockBehavior.Always,
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Criteria for matching an incoming HTTP request against a mock rule.
 *
 * @property urlPattern URL matching pattern. Exact match, prefix (ends with `*`), or regex (starts with `~`).
 * @property matchType The type of URL matching to perform.
 * @property method HTTP method to match, or null to match all methods.
 * @property headers Header name-value pairs that must all be present for a match.
 */
data class RequestMatcher(
    val urlPattern: String,
    val matchType: UrlMatchType = UrlMatchType.PREFIX,
    val method: String? = null,
    val headers: Map<String, String> = emptyMap(),
)

/** Strategy for matching a URL pattern against a request URL. */
enum class UrlMatchType {
    /** The request URL must exactly equal the pattern. */
    EXACT,

    /** The request URL must start with the pattern (excluding the trailing `*`). */
    PREFIX,

    /** The pattern is interpreted as a regular expression. */
    REGEX,
}

/**
 * The HTTP response returned when a mock rule matches.
 *
 * @property statusCode HTTP status code (e.g. 200, 404, 500).
 * @property statusMessage HTTP reason phrase (e.g. "OK", "Not Found").
 * @property headers Response headers to include.
 * @property body Response body content, or null for an empty body.
 * @property contentType MIME type for the response body.
 */
data class MockResponse(
    val statusCode: Int = 200,
    val statusMessage: String = "OK",
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val contentType: String = "application/json",
)

/** Artificial delay applied before returning a mocked response. */
sealed class MockDelay {
    /** No delay. */
    data object None : MockDelay()

    /** Fixed delay of [ms] milliseconds. */
    data class Fixed(val ms: Long) : MockDelay()

    /** Random delay between [minMs] and [maxMs] milliseconds. */
    data class Range(val minMs: Long, val maxMs: Long) : MockDelay()
}

/** Controls when and how a mock rule is applied across multiple requests. */
sealed class MockBehavior {
    /** Always return the mock response for every matching request. */
    data object Always : MockBehavior()

    /** Only mock every [n]th matching request; pass through the rest. */
    data class NthRequest(val n: Int) : MockBehavior()

    /** Mock the first [count] matching requests, then pass through. */
    data class FirstN(val count: Int) : MockBehavior()

    /** Cycle through a list of [responses] for successive matching requests. */
    data class Sequential(val responses: List<MockResponse>) : MockBehavior()

    /** Match the request but pass it through to the network (useful for debugging matchers). */
    data object Passthrough : MockBehavior()
}
