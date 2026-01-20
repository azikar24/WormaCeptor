/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.infra.syntax.json

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.azikar24.wormaceptor.domain.contracts.SyntaxColors
import com.azikar24.wormaceptor.domain.contracts.SyntaxHighlighter
import com.azikar24.wormaceptor.domain.contracts.Token
import com.azikar24.wormaceptor.domain.contracts.TokenType

/**
 * Syntax highlighter for JSON content.
 *
 * Highlights:
 * - Property keys (purple)
 * - String values (green)
 * - Numbers (blue)
 * - Booleans and null (keyword color)
 * - Punctuation (subtle)
 */
class JsonHighlighter : SyntaxHighlighter {

    override val language: String = "json"

    // Regex patterns for JSON tokens
    // Order matters: property keys must be matched before general strings
    private val patterns = listOf(
        // Property key: "key":
        TokenPattern(""""(?:[^"\\]|\\.)*"\s*:""".toRegex(), TokenType.PROPERTY),
        // String value: "value"
        TokenPattern(""""(?:[^"\\]|\\.)*"""".toRegex(), TokenType.STRING),
        // Numbers (including scientific notation)
        TokenPattern("""-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?""".toRegex(), TokenType.NUMBER),
        // Booleans and null
        TokenPattern("""\b(?:true|false|null)\b""".toRegex(), TokenType.BOOLEAN),
        // Punctuation
        TokenPattern("""[{}\[\],:]""".toRegex(), TokenType.PUNCTUATION),
    )

    override fun highlight(code: String, colors: SyntaxColors): AnnotatedString {
        val tokens = tokenize(code)
        return buildHighlightedString(code, tokens, colors)
    }

    /**
     * Tokenizes the input code into a list of tokens.
     */
    private fun tokenize(code: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val usedRanges = mutableSetOf<IntRange>()

        for (pattern in patterns) {
            pattern.regex.findAll(code).forEach { match ->
                val range = match.range
                // Check if this range overlaps with any already tokenized range
                val overlaps = usedRanges.any { existing ->
                    range.first <= existing.last && range.last >= existing.first
                }

                if (!overlaps) {
                    // For property keys, we want to highlight just the key part (including quotes)
                    // but not the colon
                    val tokenRange = if (pattern.type == TokenType.PROPERTY) {
                        val keyEnd = match.value.indexOfLast { it == '"' }
                        if (keyEnd > 0) {
                            IntRange(range.first, range.first + keyEnd)
                        } else {
                            range
                        }
                    } else {
                        range
                    }

                    tokens.add(Token(pattern.type, tokenRange.first, tokenRange.last + 1))
                    usedRanges.add(range)
                }
            }
        }

        return tokens.sortedBy { it.start }
    }

    /**
     * Builds an AnnotatedString from the code and tokens.
     */
    private fun buildHighlightedString(code: String, tokens: List<Token>, colors: SyntaxColors): AnnotatedString {
        return buildAnnotatedString {
            var lastEnd = 0

            for (token in tokens) {
                // Append unhighlighted text before this token
                if (token.start > lastEnd) {
                    withStyle(SpanStyle(color = colors.default)) {
                        append(code.substring(lastEnd, token.start))
                    }
                }

                // Append highlighted token
                withStyle(SpanStyle(color = colors.forType(token.type))) {
                    append(code.substring(token.start, minOf(token.end, code.length)))
                }

                lastEnd = token.end
            }

            // Append remaining text after last token
            if (lastEnd < code.length) {
                withStyle(SpanStyle(color = colors.default)) {
                    append(code.substring(lastEnd))
                }
            }
        }
    }

    /**
     * Internal class to pair regex patterns with token types.
     */
    private data class TokenPattern(
        val regex: Regex,
        val type: TokenType,
    )
}
