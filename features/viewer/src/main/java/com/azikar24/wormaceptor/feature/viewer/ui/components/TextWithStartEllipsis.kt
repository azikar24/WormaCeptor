package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import java.text.BreakIterator

private const val Ellipsis = "..."

/**
 * A single-line text that shows the ellipsis at the start when it overflows.
 * Useful for paths/URLs where the end matters more than the beginning.
 *
 * Example: "/api/v1/users/12345/profile" becomes "...users/12345/profile" if space is limited.
 *
 * Measures and draws in one layout pass (no subcomposition), since it sits in every
 * transaction row and runs for each row scrolled into view.
 */
@Composable
fun TextWithStartEllipsis(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    val textMeasurer = rememberTextMeasurer()
    val resolvedColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
    val mergedStyle = style.merge(TextStyle(color = resolvedColor, fontSize = fontSize, fontWeight = fontWeight))
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Layout(
        modifier = modifier
            .semantics { this.text = AnnotatedString(text) }
            .drawBehind { layoutResult?.let { drawText(it) } },
    ) { _, constraints ->
        val result = measureStartEllipsized(textMeasurer, text, mergedStyle, constraints)
        layoutResult = result
        layout(
            width = result.size.width.coerceIn(constraints.minWidth, constraints.maxWidth),
            height = result.size.height.coerceIn(constraints.minHeight, constraints.maxHeight),
        ) {}
    }
}

private fun measureStartEllipsized(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    constraints: Constraints,
): TextLayoutResult {
    val full = textMeasurer.measure(text, style, maxLines = 1, softWrap = false)
    if (!constraints.hasBoundedWidth || full.size.width <= constraints.maxWidth) return full

    val ellipsisWidth = textMeasurer.measure(Ellipsis, style, maxLines = 1, softWrap = false).size.width
    val available = constraints.maxWidth - ellipsisWidth
    if (available <= 0) return textMeasurer.measure(Ellipsis, style, maxLines = 1, softWrap = false)

    // Offset-from-position lookups assume LTR; a width search over suffixes works for any direction.
    val start = smallestFittingStart(graphemeStarts(text)) { offset ->
        val width = textMeasurer.measure(Ellipsis + text.substring(offset), style, maxLines = 1, softWrap = false)
            .size.width
        width <= constraints.maxWidth
    }
    return textMeasurer.measure(Ellipsis + text.substring(start), style, maxLines = 1, softWrap = false)
}

/** Grapheme cluster start offsets of [text], followed by `text.length`, so a cut never splits a character. */
internal fun graphemeStarts(text: String): IntArray {
    val iterator = BreakIterator.getCharacterInstance()
    iterator.setText(text)
    return generateSequence(iterator.first()) { iterator.next().takeIf { it != BreakIterator.DONE } }
        .toList()
        .toIntArray()
}

/**
 * Binary search for the smallest offset in [starts] whose suffix [fits]. Suffix width shrinks as the
 * start moves right, so the predicate is monotonic. The last entry (empty suffix) is assumed to fit.
 */
internal fun smallestFittingStart(
    starts: IntArray,
    fits: (Int) -> Boolean,
): Int {
    var low = 0
    var high = starts.lastIndex
    while (low < high) {
        val mid = low + high ushr 1
        if (fits(starts[mid])) high = mid else low = mid + 1
    }
    return starts[low]
}
