package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.SyntaxHighlighter

/**
 * Registry for managing syntax highlighters.
 * Allows registration and lookup of highlighters by language or content type.
 */
interface HighlighterRegistry {
    /**
     * Registers a syntax highlighter.
     *
     * @param highlighter The highlighter to register.
     */
    fun register(highlighter: SyntaxHighlighter)

    /**
     * Gets a highlighter for the specified language.
     *
     * @param language The language identifier (e.g., "json", "xml").
     * @return The highlighter for the language, or null if none registered.
     */
    fun getHighlighter(language: String): SyntaxHighlighter?

    /**
     * Gets a highlighter based on a content type string.
     *
     * @param contentType The MIME content type (e.g., "application/json", "text/xml").
     * @return The highlighter for the content type, or null if none registered.
     */
    fun getHighlighterForContentType(contentType: String): SyntaxHighlighter?

    /**
     * Returns all registered highlighters.
     */
    fun getAllHighlighters(): List<SyntaxHighlighter>

    /**
     * Returns all supported language identifiers.
     */
    fun getSupportedLanguages(): Set<String>
}

/**
 * Default implementation of HighlighterRegistry.
 */
class DefaultHighlighterRegistry : HighlighterRegistry {

    private val highlighters = mutableMapOf<String, SyntaxHighlighter>()

    // Mapping from content type patterns to language identifiers
    private val contentTypeToLanguage = mapOf(
        "application/json" to "json",
        "text/json" to "json",
        "application/xml" to "xml",
        "text/xml" to "xml",
        "text/html" to "html",
        "application/xhtml+xml" to "html",
        "text/css" to "css",
        "application/javascript" to "javascript",
        "text/javascript" to "javascript",
        "application/x-javascript" to "javascript",
    )

    override fun register(highlighter: SyntaxHighlighter) {
        highlighters[highlighter.language.lowercase()] = highlighter
    }

    override fun getHighlighter(language: String): SyntaxHighlighter? {
        return highlighters[language.lowercase()]
    }

    override fun getHighlighterForContentType(contentType: String): SyntaxHighlighter? {
        // Extract base content type (remove charset and other parameters)
        val baseContentType = contentType.split(";").firstOrNull()?.trim()?.lowercase() ?: return null

        // Look up language for content type
        val language = contentTypeToLanguage[baseContentType]
            ?: detectLanguageFromContentType(baseContentType)
            ?: return null

        return getHighlighter(language)
    }

    override fun getAllHighlighters(): List<SyntaxHighlighter> {
        return highlighters.values.toList()
    }

    override fun getSupportedLanguages(): Set<String> {
        return highlighters.keys.toSet()
    }

    /**
     * Attempts to detect language from content type using pattern matching.
     */
    private fun detectLanguageFromContentType(contentType: String): String? {
        return when {
            contentType.contains("json") -> "json"
            contentType.contains("xml") -> "xml"
            contentType.contains("html") -> "html"
            contentType.contains("javascript") || contentType.contains("ecmascript") -> "javascript"
            contentType.contains("css") -> "css"
            else -> null
        }
    }
}
