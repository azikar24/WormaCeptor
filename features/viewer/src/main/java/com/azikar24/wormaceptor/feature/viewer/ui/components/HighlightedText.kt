/*
 * Copyright AziKar24 2025.
 */

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
import com.azikar24.wormaceptor.feature.viewer.ui.theme.ComposeSyntaxColors
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.syntaxColors

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
    colors: ComposeSyntaxColors = syntaxColors()
) {
    val displayText = if (enableHighlighting && highlightedText != null) {
        // Apply search highlighting on top of syntax highlighting
        applySearchHighlighting(
            text = highlightedText,
            searchQuery = searchQuery,
            currentMatchIndex = currentSearchMatchIndex,
            colors = colors
        )
    } else {
        // Plain text with search highlighting only
        applySearchHighlighting(
            text = AnnotatedString(text),
            searchQuery = searchQuery,
            currentMatchIndex = currentSearchMatchIndex,
            colors = colors
        )
    }

    if (showLineNumbers) {
        HighlightedTextWithLineNumbers(
            text = text,
            displayText = displayText,
            style = style,
            colors = colors,
            onTextLayout = onTextLayout,
            modifier = modifier
        )
    } else {
        SelectionContainer {
            Text(
                text = displayText,
                style = style,
                modifier = modifier.fillMaxWidth(),
                onTextLayout = { onTextLayout?.invoke(it) }
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
    modifier: Modifier = Modifier
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
            .background(colors.codeBackground, WormaCeptorDesignSystem.Shapes.chip)
    ) {
        // Line numbers column
        Column(
            modifier = Modifier
                .width(lineNumberWidth)
                .background(colors.lineNumberBackground)
                .padding(
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                    horizontal = WormaCeptorDesignSystem.Spacing.sm
                ),
            horizontalAlignment = Alignment.End
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    style = style.copy(
                        color = colors.lineNumberText,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(end = WormaCeptorDesignSystem.Spacing.xs)
                )
            }
        }

        // Vertical divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .background(colors.lineNumberText.copy(alpha = 0.2f))
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
                    bottom = WormaCeptorDesignSystem.Spacing.sm
                )
        ) {
            SelectionContainer {
                Text(
                    text = displayText,
                    style = style,
                    onTextLayout = { onTextLayout?.invoke(it) }
                )
            }
        }
    }
}

/**
 * Applies search highlighting on top of existing AnnotatedString.
 * Search matches are shown with a highlight background,
 * and the current match is shown with a distinct color.
 */
private fun applySearchHighlighting(
    text: AnnotatedString,
    searchQuery: String,
    currentMatchIndex: Int?,
    colors: ComposeSyntaxColors
): AnnotatedString {
    if (searchQuery.isEmpty()) return text

    val plainText = text.text
    val matches = mutableListOf<IntRange>()

    var startIndex = 0
    while (true) {
        val index = plainText.indexOf(searchQuery, startIndex, ignoreCase = true)
        if (index < 0) break
        matches.add(index until (index + searchQuery.length))
        startIndex = index + 1
    }

    if (matches.isEmpty()) return text

    return buildAnnotatedString {
        // First, append all the original spans
        append(text)

        // Then overlay search highlights
        matches.forEachIndexed { matchIndex, range ->
            val isCurrent = matchIndex == currentMatchIndex
            addStyle(
                style = SpanStyle(
                    background = if (isCurrent) {
                        colors.searchHighlightCurrent
                    } else {
                        colors.searchHighlight
                    },
                    color = colors.searchHighlightText,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                ),
                start = range.first,
                end = range.last + 1
            )
        }
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
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    val colors = syntaxColors()
    val highlightedText = remember(text, searchQuery, currentMatchIndex, colors) {
        applySearchHighlighting(
            text = AnnotatedString(text),
            searchQuery = searchQuery,
            currentMatchIndex = currentMatchIndex,
            colors = colors
        )
    }

    SelectionContainer {
        Text(
            text = highlightedText,
            style = style,
            modifier = modifier,
            onTextLayout = { onTextLayout?.invoke(it) }
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
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
) {
    val colors = syntaxColors()
    Text(
        text = text,
        style = style.copy(color = colors.default),
        modifier = modifier
            .background(
                colors.codeBackground,
                WormaCeptorDesignSystem.Shapes.chip
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                vertical = WormaCeptorDesignSystem.Spacing.xxs
            )
    )
}
