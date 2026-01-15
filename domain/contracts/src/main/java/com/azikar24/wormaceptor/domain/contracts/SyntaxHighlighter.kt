/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.contracts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

/**
 * Contract for syntax highlighting implementations.
 * Each highlighter is responsible for tokenizing and coloring code for a specific language.
 */
interface SyntaxHighlighter {
    /**
     * The language identifier this highlighter supports (e.g., "json", "xml", "html").
     */
    val language: String

    /**
     * Highlights the given code string and returns an AnnotatedString with color spans.
     *
     * @param code The source code to highlight.
     * @param colors The color scheme to use for highlighting.
     * @return An AnnotatedString with appropriate color spans applied.
     */
    fun highlight(code: String, colors: SyntaxColors): AnnotatedString
}

/**
 * Token types for consistent syntax coloring across all highlighters.
 */
enum class TokenType {
    KEYWORD,        // Language keywords (function, if, else)
    STRING,         // "string literals"
    NUMBER,         // 123, 45.67
    COMMENT,        // // comments, /* block */
    PROPERTY,       // JSON keys, XML attributes
    TAG,            // HTML/XML tags
    OPERATOR,       // =, +, -, etc.
    PUNCTUATION,    // {, }, [, ], etc.
    BOOLEAN,        // true, false, null
    TYPE,           // Type annotations
    DEFAULT         // Default text
}

/**
 * Represents a matched token in the source code.
 */
data class Token(
    val type: TokenType,
    val start: Int,
    val end: Int
)

/**
 * Color scheme for syntax highlighting.
 * Implementations should provide both light and dark theme variants.
 */
interface SyntaxColors {
    val keyword: Color
    val string: Color
    val number: Color
    val comment: Color
    val property: Color
    val tag: Color
    val operator: Color
    val punctuation: Color
    val boolean: Color
    val typeAnnotation: Color
    val default: Color

    /**
     * Returns the color for the given token type.
     */
    fun forType(tokenType: TokenType): Color = when (tokenType) {
        TokenType.KEYWORD -> keyword
        TokenType.STRING -> string
        TokenType.NUMBER -> number
        TokenType.COMMENT -> comment
        TokenType.PROPERTY -> property
        TokenType.TAG -> tag
        TokenType.OPERATOR -> operator
        TokenType.PUNCTUATION -> punctuation
        TokenType.BOOLEAN -> boolean
        TokenType.TYPE -> typeAnnotation
        TokenType.DEFAULT -> default
    }
}

/**
 * Light theme syntax colors - inspired by IntelliJ IDEA light theme.
 */
object LightSyntaxColors : SyntaxColors {
    override val keyword = Color(0xFF0033B3)      // Blue
    override val string = Color(0xFF067D17)       // Green
    override val number = Color(0xFF1750EB)       // Blue
    override val comment = Color(0xFF8C8C8C)      // Gray
    override val property = Color(0xFF871094)     // Purple
    override val tag = Color(0xFF0033B3)          // Blue
    override val operator = Color(0xFF000000)     // Black
    override val punctuation = Color(0xFF000000)  // Black
    override val boolean = Color(0xFF0033B3)      // Blue
    override val typeAnnotation = Color(0xFF008080) // Teal
    override val default = Color(0xFF000000)      // Black
}

/**
 * Dark theme syntax colors - inspired by IntelliJ IDEA dark theme.
 */
object DarkSyntaxColors : SyntaxColors {
    override val keyword = Color(0xFFCC7832)      // Orange
    override val string = Color(0xFF6A8759)       // Green
    override val number = Color(0xFF6897BB)       // Blue
    override val comment = Color(0xFF808080)      // Gray
    override val property = Color(0xFF9876AA)     // Purple
    override val tag = Color(0xFFE8BF6A)          // Yellow
    override val operator = Color(0xFFA9B7C6)     // Light gray
    override val punctuation = Color(0xFFA9B7C6)  // Light gray
    override val boolean = Color(0xFFCC7832)      // Orange
    override val typeAnnotation = Color(0xFF6897BB) // Blue
    override val default = Color(0xFFA9B7C6)      // Light gray
}
