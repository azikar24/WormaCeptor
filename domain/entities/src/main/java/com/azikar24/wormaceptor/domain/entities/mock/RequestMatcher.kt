package com.azikar24.wormaceptor.domain.entities.mock

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
