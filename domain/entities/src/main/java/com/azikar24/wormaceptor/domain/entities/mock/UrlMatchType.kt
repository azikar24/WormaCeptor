package com.azikar24.wormaceptor.domain.entities.mock

/** Strategy for matching a URL pattern against a request URL. */
enum class UrlMatchType {
    EXACT,
    PREFIX,
    REGEX,
}
