package com.azikar24.wormaceptor.domain.contracts

import java.net.URI

/**
 * Utility for parsing URLs and extracting base URL and path components.
 *
 * Base URL detection priority:
 * 1. Look for /api/vX pattern (e.g., /api/v1, /api/v2)
 * 2. Look for standalone /vX pattern (e.g., /v1, /v2)
 * 3. Fall back to just the host
 */
object UrlParser {

    // Matches /api/v followed by version number
    private val API_VERSION_PATTERN = Regex("/api/v(\\d+)")

    // Matches standalone /v followed by version number
    private val VERSION_PATTERN = Regex("/v(\\d+)")

    /**
     * Result of URL parsing containing base URL and relative path.
     */
    data class ParsedUrl(
        val baseUrl: String,
        val path: String
    )

    /**
     * Parses a URL and extracts the base URL and relative path.
     *
     * Examples:
     * - "https://api.example.com/api/v1/users/123" -> baseUrl: "api.example.com/api/v1", path: "/users/123"
     * - "https://api.example.com/v2/products" -> baseUrl: "api.example.com/v2", path: "/products"
     * - "https://api.example.com/users/123" -> baseUrl: "api.example.com", path: "/users/123"
     *
     * @param url The full URL to parse
     * @return ParsedUrl with baseUrl and path components
     */
    fun parse(url: String): ParsedUrl {
        return try {
            val uri = URI(url)
            val host = uri.host ?: return ParsedUrl(url, "")
            val fullPath = uri.path ?: ""

            // Try to find /api/vX pattern first
            val apiVersionMatch = API_VERSION_PATTERN.find(fullPath)
            if (apiVersionMatch != null) {
                val versionPath = fullPath.substring(0, apiVersionMatch.range.last + 1)
                val relativePath = fullPath.substring(apiVersionMatch.range.last + 1).ifEmpty { "/" }
                return ParsedUrl(
                    baseUrl = "$host$versionPath",
                    path = relativePath
                )
            }

            // Try to find standalone /vX pattern
            val versionMatch = VERSION_PATTERN.find(fullPath)
            if (versionMatch != null) {
                val versionPath = fullPath.substring(0, versionMatch.range.last + 1)
                val relativePath = fullPath.substring(versionMatch.range.last + 1).ifEmpty { "/" }
                return ParsedUrl(
                    baseUrl = "$host$versionPath",
                    path = relativePath
                )
            }

            // Fall back to just host
            ParsedUrl(
                baseUrl = host,
                path = fullPath.ifEmpty { "/" }
            )
        } catch (e: Exception) {
            ParsedUrl(url, "")
        }
    }

    /**
     * Extracts just the base URL from a full URL.
     *
     * @param url The full URL to parse
     * @return The base URL (host + api version path if present)
     */
    fun extractBaseUrl(url: String): String = parse(url).baseUrl

    /**
     * Extracts the relative path from a full URL.
     *
     * @param url The full URL to parse
     * @return The path relative to the base URL
     */
    fun extractPath(url: String): String = parse(url).path
}
