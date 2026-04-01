package com.azikar24.wormaceptor.domain.entities.mock

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
