/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.infra.syntax.xml

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.azikar24.wormaceptor.domain.contracts.SyntaxColors
import com.azikar24.wormaceptor.domain.contracts.SyntaxHighlighter
import com.azikar24.wormaceptor.domain.contracts.Token
import com.azikar24.wormaceptor.domain.contracts.TokenType

/**
 * Syntax highlighter for XML and HTML content.
 *
 * Highlights:
 * - Tag names (tag color)
 * - Attribute names (property color)
 * - Attribute values (string color)
 * - Comments (comment color)
 * - DOCTYPE and CDATA (keyword color)
 * - Tag brackets and punctuation (punctuation color)
 */
class XmlHighlighter : SyntaxHighlighter {

    override val language: String = "xml"

    override fun highlight(code: String, colors: SyntaxColors): AnnotatedString {
        val tokens = tokenize(code)
        return buildHighlightedString(code, tokens, colors)
    }

    /**
     * Tokenizes the input XML/HTML code.
     *
     * Uses a simple state machine approach to handle the nested nature of XML.
     */
    private fun tokenize(code: String): List<Token> {
        val tokens = mutableListOf<Token>()

        // Match comments: <!-- ... -->
        val commentRegex = """<!--[\s\S]*?-->""".toRegex()
        commentRegex.findAll(code).forEach { match ->
            tokens.add(Token(TokenType.COMMENT, match.range.first, match.range.last + 1))
        }

        // Match CDATA sections: <![CDATA[ ... ]]>
        val cdataRegex = """<!\[CDATA\[[\s\S]*?\]\]>""".toRegex()
        cdataRegex.findAll(code).forEach { match ->
            tokens.add(Token(TokenType.KEYWORD, match.range.first, match.range.last + 1))
        }

        // Match DOCTYPE: <!DOCTYPE ... >
        val doctypeRegex = """<!DOCTYPE[^>]*>""".toRegex(RegexOption.IGNORE_CASE)
        doctypeRegex.findAll(code).forEach { match ->
            tokens.add(Token(TokenType.KEYWORD, match.range.first, match.range.last + 1))
        }

        // Match XML declaration: <?xml ... ?>
        val xmlDeclRegex = """<\?xml[^?]*\?>""".toRegex(RegexOption.IGNORE_CASE)
        xmlDeclRegex.findAll(code).forEach { match ->
            tokens.add(Token(TokenType.KEYWORD, match.range.first, match.range.last + 1))
        }

        // Match processing instructions: <?name ... ?>
        val procInstrRegex = """<\?[a-zA-Z][a-zA-Z0-9]*[^?]*\?>""".toRegex()
        procInstrRegex.findAll(code).forEach { match ->
            // Skip if already matched by xmlDeclRegex
            val overlaps = tokens.any { token ->
                match.range.first < token.end && match.range.last >= token.start
            }
            if (!overlaps) {
                tokens.add(Token(TokenType.KEYWORD, match.range.first, match.range.last + 1))
            }
        }

        // Match tags and their contents
        val tagRegex = """</?([a-zA-Z_][a-zA-Z0-9_:.-]*)([^>]*)>""".toRegex()
        tagRegex.findAll(code).forEach { match ->
            val fullRange = match.range

            // Skip if this overlaps with a comment, CDATA, DOCTYPE, or declaration
            val overlaps = tokens.any { token ->
                fullRange.first < token.end && fullRange.last >= token.start
            }
            if (overlaps) return@forEach

            // Opening bracket < or </
            val bracketEnd = if (code.getOrNull(fullRange.first + 1) == '/') {
                fullRange.first + 2
            } else {
                fullRange.first + 1
            }
            tokens.add(Token(TokenType.PUNCTUATION, fullRange.first, bracketEnd))

            // Tag name
            val tagNameGroup = match.groups[1]
            if (tagNameGroup != null) {
                tokens.add(Token(TokenType.TAG, tagNameGroup.range.first, tagNameGroup.range.last + 1))
            }

            // Attributes within the tag
            val attributesPart = match.groups[2]
            if (attributesPart != null && attributesPart.value.isNotBlank()) {
                tokenizeAttributes(attributesPart.value, attributesPart.range.first, tokens)
            }

            // Closing bracket > or />
            val closingStart = if (code.getOrNull(fullRange.last - 1) == '/') {
                fullRange.last - 1
            } else {
                fullRange.last
            }
            tokens.add(Token(TokenType.PUNCTUATION, closingStart, fullRange.last + 1))
        }

        // Match entity references: &amp; &lt; etc.
        val entityRegex = """&[a-zA-Z]+;|&#\d+;|&#x[0-9a-fA-F]+;""".toRegex()
        entityRegex.findAll(code).forEach { match ->
            val overlaps = tokens.any { token ->
                match.range.first < token.end && match.range.last >= token.start
            }
            if (!overlaps) {
                tokens.add(Token(TokenType.KEYWORD, match.range.first, match.range.last + 1))
            }
        }

        return tokens.sortedBy { it.start }
    }

    /**
     * Tokenizes attributes within a tag.
     *
     * @param attrString The attribute string (everything between tag name and >)
     * @param baseOffset The starting position of attrString in the original code
     * @param tokens The token list to add to
     */
    private fun tokenizeAttributes(attrString: String, baseOffset: Int, tokens: MutableList<Token>) {
        // Match attribute: name="value" or name='value' or name=value or just name
        val attrRegex = """([a-zA-Z_:][a-zA-Z0-9_:.-]*)\s*(?:=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+)))?""".toRegex()

        attrRegex.findAll(attrString).forEach { match ->
            // Attribute name
            val nameGroup = match.groups[1]
            if (nameGroup != null) {
                val nameStart = baseOffset + nameGroup.range.first
                val nameEnd = baseOffset + nameGroup.range.last + 1
                tokens.add(Token(TokenType.PROPERTY, nameStart, nameEnd))
            }

            // Attribute value (with quotes)
            // Check which value group matched: [2] = double quotes, [3] = single quotes, [4] = no quotes
            val valueGroup = match.groups[2] ?: match.groups[3] ?: match.groups[4]
            if (valueGroup != null) {
                // Find the quote characters in the original match
                val valueInMatch = match.value
                val eqIndex = valueInMatch.indexOf('=')
                if (eqIndex >= 0) {
                    val afterEq = valueInMatch.substring(eqIndex + 1).trimStart()
                    val quoteChar = afterEq.firstOrNull()

                    if (quoteChar == '"' || quoteChar == '\'') {
                        // Include quotes in the highlighted value
                        val quoteStart = match.range.first + valueInMatch.indexOf(quoteChar, eqIndex)
                        val quoteEnd = match.range.first + valueInMatch.lastIndexOf(quoteChar) + 1
                        tokens.add(Token(TokenType.STRING, baseOffset + quoteStart, baseOffset + quoteEnd))
                    } else {
                        // Unquoted value
                        val valueStart = baseOffset + valueGroup.range.first
                        val valueEnd = baseOffset + valueGroup.range.last + 1
                        tokens.add(Token(TokenType.STRING, valueStart, valueEnd))
                    }
                }
            }
        }
    }

    /**
     * Builds an AnnotatedString from the code and tokens.
     */
    private fun buildHighlightedString(
        code: String,
        tokens: List<Token>,
        colors: SyntaxColors
    ): AnnotatedString {
        return buildAnnotatedString {
            var lastEnd = 0

            // Remove overlapping tokens, keeping the first one (which is usually more specific)
            val filteredTokens = mutableListOf<Token>()
            for (token in tokens.sortedBy { it.start }) {
                val overlaps = filteredTokens.any { existing ->
                    token.start < existing.end && token.end > existing.start
                }
                if (!overlaps) {
                    filteredTokens.add(token)
                }
            }

            for (token in filteredTokens.sortedBy { it.start }) {
                // Append unhighlighted text before this token
                if (token.start > lastEnd) {
                    withStyle(SpanStyle(color = colors.default)) {
                        append(code.substring(lastEnd, token.start))
                    }
                }

                // Append highlighted token
                val safeEnd = minOf(token.end, code.length)
                if (token.start < safeEnd) {
                    withStyle(SpanStyle(color = colors.forType(token.type))) {
                        append(code.substring(token.start, safeEnd))
                    }
                }

                lastEnd = maxOf(lastEnd, safeEnd)
            }

            // Append remaining text after last token
            if (lastEnd < code.length) {
                withStyle(SpanStyle(color = colors.default)) {
                    append(code.substring(lastEnd))
                }
            }
        }
    }
}
