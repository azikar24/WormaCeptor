package com.azikar24.wormaceptor.domain.contracts

/**
 * Contract for syntax highlighting implementations.
 * Each highlighter is responsible for tokenizing code for a specific language.
 */
interface SyntaxHighlighter {
    /**
     * The language identifier this highlighter supports (e.g., "json", "xml", "html").
     */
    val language: String

    /**
     * Tokenizes the given code string into a list of categorized tokens.
     *
     * @param code The source code to tokenize.
     * @return A list of tokens with type and position information.
     */
    fun tokenize(code: String): List<Token>
}

/**
 * Token types for consistent syntax coloring across all highlighters.
 */
enum class TokenType {
    KEYWORD, // Language keywords (function, if, else)
    STRING, // "string literals"
    NUMBER, // 123, 45.67
    COMMENT, // // comments, /* block */
    PROPERTY, // JSON keys, XML attributes
    TAG, // HTML/XML tags
    OPERATOR, // =, +, -, etc.
    PUNCTUATION, // {, }, [, ], etc.
    BOOLEAN, // true, false, null
    TYPE, // Type annotations
    DEFAULT, // Default text
}

/**
 * Represents a matched token in the source code.
 */
data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int,
)
