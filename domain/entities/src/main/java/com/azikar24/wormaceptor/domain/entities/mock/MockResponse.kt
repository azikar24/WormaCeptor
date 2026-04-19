package com.azikar24.wormaceptor.domain.entities.mock

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
