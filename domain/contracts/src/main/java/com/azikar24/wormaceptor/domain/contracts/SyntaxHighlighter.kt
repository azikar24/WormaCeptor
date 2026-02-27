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
    /** Language keywords (function, if, else, return). */
    KEYWORD,

    /** Quoted string literals. */
    STRING,

    /** Numeric literals (integer and floating-point). */
    NUMBER,

    /** Line and block comments. */
    COMMENT,

    /** Object keys, JSON keys, or XML attribute names. */
    PROPERTY,

    /** HTML/XML element tag names. */
    TAG,

    /** Operators such as =, +, -, etc. */
    OPERATOR,

    /** Structural punctuation like braces, brackets, and colons. */
    PUNCTUATION,

    /** Boolean and null literals (true, false, null). */
    BOOLEAN,

    /** Type annotations and class references. */
    TYPE,

    /** Unstyled default text that does not match any other category. */
    DEFAULT,
}

/**
 * Represents a matched token in the source code.
 */
data class Token(
    /** Semantic category of this token for syntax coloring. */
    val type: TokenType,
    /** Inclusive start index in the source string. */
    val start: Int,
    /** Exclusive end index in the source string. */
    val end: Int,
)
