package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.ComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.syntaxColors

/**
 * A composable that displays text with optional syntax highlighting,
 * line numbers, and search highlighting support.
 *
 * Features:
 * - Syntax highlighting for code (when provided as pre-highlighted AnnotatedString)
 * - Line numbers with subtle styling
 * - Search match highlighting layered on top of syntax colors
 * - Support for both light and dark themes
 * - Toggle for enabling/disabling highlighting
 * - Horizontal scrolling for long lines
 */
@Composable
fun HighlightedText(
    text: String,
    modifier: Modifier = Modifier,
    highlightedText: AnnotatedString? = null,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    showLineNumbers: Boolean = true,
    enableHighlighting: Boolean = true,
    searchQuery: String = "",
    currentSearchMatchIndex: Int? = null,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    colors: ComposeSyntaxColors = syntaxColors(),
) {
    // Level 1: Memoize the base text (with or without syntax highlighting)
    val baseText = remember(text, highlightedText, enableHighlighting) {
        if (enableHighlighting && highlightedText != null) highlightedText else AnnotatedString(text)
    }

    // Level 2: Memoize match positions - only recompute when text or query changes
    val matches = remember(text, searchQuery) {
        if (searchQuery.isEmpty()) emptyList() else findAllMatches(text, searchQuery)
    }

    // Level 3: Cache base text with ALL matches highlighted (yellow)
    val baseWithAllMatches = remember(baseText, matches, colors) {
        applyAllMatchesHighlight(baseText, matches, colors)
    }

    // Level 4: Apply current match highlight - O(1) operation
    val displayText = remember(baseWithAllMatches, matches, currentSearchMatchIndex, colors) {
        applyCurrentMatchOnly(baseWithAllMatches, matches, currentSearchMatchIndex, colors)
    }

    if (showLineNumbers) {
        HighlightedTextWithLineNumbers(
            text = text,
            displayText = displayText,
            style = style,
            colors = colors,
            onTextLayout = onTextLayout,
            modifier = modifier,
        )
    } else {
        SelectionContainer {
            Text(
                text = displayText,
                style = style,
                modifier = modifier.fillMaxWidth(),
                onTextLayout = { onTextLayout?.invoke(it) },
            )
        }
    }
}

@Composable
private fun HighlightedTextWithLineNumbers(
    text: String,
    displayText: AnnotatedString,
    style: TextStyle,
    colors: ComposeSyntaxColors,
    onTextLayout: ((TextLayoutResult) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val lines = remember(text) { text.lines() }
    val lineCount = lines.size
    val lineNumberWidth = remember(lineCount) {
        // Calculate width based on number of digits
        (lineCount.toString().length * 10 + 16).dp
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.codeBackground, WormaCeptorDesignSystem.Shapes.chip),
    ) {
        // Line numbers column
        Column(
            modifier = Modifier
                .width(lineNumberWidth)
                .background(colors.lineNumberBackground)
                .padding(
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                ),
            horizontalAlignment = Alignment.End,
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    style = style.copy(
                        color = colors.lineNumberText,
                        fontWeight = FontWeight.Normal,
                    ),
                    modifier = Modifier.padding(end = WormaCeptorDesignSystem.Spacing.xs),
                )
            }
        }

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .background(colors.lineNumberText.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)),
        )

        // Code content with horizontal scroll
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(
                    start = WormaCeptorDesignSystem.Spacing.md,
                    end = WormaCeptorDesignSystem.Spacing.sm,
                    top = WormaCeptorDesignSystem.Spacing.sm,
                    bottom = WormaCeptorDesignSystem.Spacing.sm,
                ),
        ) {
            SelectionContainer {
                Text(
                    text = displayText,
                    style = style,
                    onTextLayout = { onTextLayout?.invoke(it) },
                )
            }
        }
    }
}

/**
 * Finds all match positions for a search query in text.
 * Uses indexOf for O(n) performance - faster than regex for simple substring matching.
 */
private fun findAllMatches(
    text: String,
    searchQuery: String,
): List<IntRange> {
    if (searchQuery.isEmpty()) return emptyList()

    val matches = ArrayList<IntRange>(64) // Pre-size for typical case
    var index = 0
    while (true) {
        index = text.indexOf(searchQuery, index, ignoreCase = true)
        if (index < 0) break
        matches.add(index until (index + searchQuery.length))
        index++
    }
    return matches
}

/**
 * Apply default highlight (yellow) to ALL matches.
 * This is cached and only rebuilt when text/query changes.
 */
private fun applyAllMatchesHighlight(
    text: AnnotatedString,
    matches: List<IntRange>,
    colors: ComposeSyntaxColors,
): AnnotatedString {
    if (matches.isEmpty()) return text

    return buildAnnotatedString {
        append(text)
        val defaultStyle = SpanStyle(
            background = colors.searchHighlight,
            color = colors.searchHighlightText,
        )
        matches.forEach { range ->
            addStyle(defaultStyle, range.first, range.last + 1)
        }
    }
}

/**
 * Apply current match highlight on top of base - O(1) operation.
 * Just adds one style span for the current match.
 */
private fun applyCurrentMatchOnly(
    base: AnnotatedString,
    matches: List<IntRange>,
    currentMatchIndex: Int?,
    colors: ComposeSyntaxColors,
): AnnotatedString {
    if (currentMatchIndex == null || matches.isEmpty()) return base
    val range = matches.getOrNull(currentMatchIndex) ?: return base

    return buildAnnotatedString {
        append(base)
        addStyle(
            SpanStyle(
                background = colors.searchHighlightCurrent,
                color = colors.searchHighlightText,
                fontWeight = FontWeight.Bold,
            ),
            range.first,
            range.last + 1,
        )
    }
}

/**
 * Simplified version for plain text with search highlighting.
 * Use this when you don't need syntax highlighting.
 */
@Composable
fun SearchHighlightedText(
    text: String,
    searchQuery: String,
    modifier: Modifier = Modifier,
    currentMatchIndex: Int? = null,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
) {
    val colors = syntaxColors()

    // Level 1: Cache match ranges
    val matches = remember(text, searchQuery) {
        if (searchQuery.isEmpty()) emptyList() else findAllMatches(text, searchQuery)
    }

    // Level 2: Cache base with all matches highlighted
    val baseText = remember(text) { AnnotatedString(text) }
    val baseWithMatches = remember(baseText, matches, colors) {
        applyAllMatchesHighlight(baseText, matches, colors)
    }

    // Level 3: Apply current match - O(1)
    val highlightedText = remember(baseWithMatches, matches, currentMatchIndex, colors) {
        applyCurrentMatchOnly(baseWithMatches, matches, currentMatchIndex, colors)
    }

    SelectionContainer {
        Text(
            text = highlightedText,
            style = style,
            modifier = modifier,
            onTextLayout = { onTextLayout?.invoke(it) },
        )
    }
}

/**
 * Inline code display with syntax-aware styling.
 * Useful for displaying small code snippets inline with text.
 */
@Composable
fun InlineCode(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
) {
    val colors = syntaxColors()
    Text(
        text = text,
        style = style.copy(color = colors.default),
        modifier = modifier
            .background(
                colors.codeBackground,
                WormaCeptorDesignSystem.Shapes.chip,
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
    )
}
