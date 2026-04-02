package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.body.ContentTypeChip

/**
 * Data class holding information about a single search match.
 */
internal data class MatchInfo(
    val globalPosition: Int,
    val lineIndex: Int,
)

/** Maximum body size (in chars) for which syntax highlighting is attempted. */
internal const val MAX_SYNTAX_HIGHLIGHT_SIZE = 200_000

@Composable
internal fun PrettyRawToggle(
    isPretty: Boolean,
    onToggle: () -> Unit,
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val radius = WormaCeptorDesignSystem.CornerRadius.xs
    val shape = RoundedCornerShape(radius)

    val borderColor by animateColorAsState(
        targetValue = activeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE),
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
        label = "segment_border",
    )

    Row(
        modifier = Modifier
            .clip(shape)
            .border(WormaCeptorDesignSystem.BorderWidth.thin, borderColor, shape),
    ) {
        SegmentOption(
            text = stringResource(R.string.viewer_body_pretty),
            isSelected = isPretty,
            activeColor = activeColor,
            onClick = { if (!isPretty) onToggle() },
        )
        SegmentOption(
            text = stringResource(R.string.viewer_body_raw),
            isSelected = !isPretty,
            activeColor = activeColor,
            onClick = { if (isPretty) onToggle() },
        )
    }
}

@Composable
private fun SegmentOption(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            activeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.LIGHT)
        } else {
            Color.Transparent
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
        label = "segment_bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.NORMAL),
        label = "segment_text",
    )

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        ),
        color = textColor,
        modifier = Modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
    )
}

/**
 * Responsive row of body controls that wraps on small screens.
 * Uses FlowRow to allow badges to flow to multiple lines when space is constrained.
 *
 * Layout priority (from most to least important):
 * 1. Pretty/Raw toggle - primary interaction for viewing mode
 * 2. Zoom button - quick access to fullscreen view
 * 3. Content type chip - informational, can wrap to next row
 */
@Composable
internal fun BodyControlsRow(
    contentType: ContentType,
    isPrettyMode: Boolean,
    onPrettyModeToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Content type chip - informational, can wrap first
        ContentTypeChip(contentType = contentType)
        // Pretty/Raw toggle - most important, always visible
        PrettyRawToggle(
            isPretty = isPrettyMode,
            onToggle = onPrettyModeToggle,
        )
    }
}

/**
 * Find all match positions using indexOf - faster than regex for simple substring matching.
 * O(n) where n = text length.
 */
internal fun findMatchRanges(
    text: String,
    query: String,
): List<IntRange> {
    if (query.isEmpty()) return emptyList()
    val matches = ArrayList<IntRange>(64) // Pre-size for typical case
    var index = 0
    while (true) {
        index = text.indexOf(query, index, ignoreCase = true)
        if (index < 0) break
        matches.add(index until index + query.length)
        index++
    }
    return matches
}

/**
 * Build base AnnotatedString with ALL matches highlighted (yellow).
 * This is cached and reused - only rebuilt when text/query changes.
 * Current match is NOT highlighted here - it uses an overlay for true O(1).
 */
internal fun buildBaseHighlightedText(
    text: String,
    matchRanges: List<IntRange>,
    syntaxHighlighted: AnnotatedString? = null,
): AnnotatedString {
    val base = syntaxHighlighted ?: AnnotatedString(text)
    if (matchRanges.isEmpty()) return base

    return buildAnnotatedString {
        append(base)
        val defaultStyle = SpanStyle(
            background = WormaCeptorColors.Accent.Highlight.copy(alpha = WormaCeptorDesignSystem.Alpha.STRONG),
        )
        matchRanges.forEach { range ->
            addStyle(defaultStyle, range.first, range.last + 1)
        }
    }
}

/**
 * True O(1) highlighted text component.
 * Uses overlay for current match instead of rebuilding AnnotatedString.
 */
@Composable
internal fun HighlightedBodyText(
    text: String,
    query: String,
    currentMatchGlobalPos: Int?,
    modifier: Modifier = Modifier,
    syntaxHighlighted: AnnotatedString? = null,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    // Level 1: Cache match ranges - only when text/query changes
    val matchRanges = remember(text, query) {
        findMatchRanges(text, query)
    }

    // Level 2: Cache base text with ALL matches in yellow - only when text/query changes
    val baseHighlighted = remember(text, matchRanges, syntaxHighlighted) {
        buildBaseHighlightedText(text, matchRanges, syntaxHighlighted)
    }

    // Track layout for overlay positioning
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Current match range for overlay - O(1) lookup
    val currentMatchRange = remember(currentMatchGlobalPos, matchRanges) {
        if (currentMatchGlobalPos == null) {
            null
        } else {
            matchRanges.find { it.first == currentMatchGlobalPos }
        }
    }

    // Current match PATH (not bounds) - handles wrapped text correctly
    val currentMatchPath = remember(textLayoutResult, currentMatchRange) {
        val layout = textLayoutResult ?: return@remember null
        val range = currentMatchRange ?: return@remember null

        try {
            layout.getPathForRange(range.first, range.last + 1)
        } catch (_: Exception) {
            null
        }
    }

    val highlightColor = WormaCeptorColors.StatusBlue.copy(alpha = WormaCeptorDesignSystem.Alpha.HEAVY)

    Box(modifier = modifier) {
        // Current match overlay using Canvas to draw the actual path shape
        // This correctly handles text that wraps across multiple lines
        currentMatchPath?.let { path ->
            Canvas(
                modifier = Modifier.matchParentSize(),
            ) {
                drawPath(path, highlightColor)
            }
        }

        // Base text with yellow highlights (cached, never changes on "next")
        Text(
            text = baseHighlighted,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            onTextLayout = {
                textLayoutResult = it
                onTextLayout(it)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrettyRawTogglePrettyPreview() {
    WormaCeptorTheme {
        PrettyRawToggle(
            isPretty = true,
            onToggle = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrettyRawToggleRawPreview() {
    WormaCeptorTheme {
        PrettyRawToggle(
            isPretty = false,
            onToggle = {},
        )
    }
}
