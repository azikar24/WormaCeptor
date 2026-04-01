package com.azikar24.wormaceptor.domain.entities.mock

/** Strategy for matching a URL pattern against a request URL. */
enum class UrlMatchType {
    /** The request URL must exactly equal the pattern. */
    EXACT,

    /** The request URL must start with the pattern (excluding the trailing `*`). */
    PREFIX,

    /** The pattern is interpreted as a regular expression. */
    REGEX,
}
