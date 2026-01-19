package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit

/**
 * A Text composable that shows ellipsis at the start when text overflows.
 * This is useful for displaying paths/URLs where the end is more important than the beginning.
 *
 * Example: "/api/v1/users/12345/profile" becomes "...users/12345/profile" if space is limited.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
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
    val density = LocalDensity.current

    val mergedStyle = style.merge(
        TextStyle(
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    )

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = constraints.maxWidth

        val displayText = remember(text, maxWidthPx, mergedStyle) {
            if (maxWidthPx == Constraints.Infinity || maxWidthPx <= 0) {
                text
            } else {
                calculateStartEllipsisText(text, maxWidthPx, mergedStyle, textMeasurer)
            }
        }

        Text(
            text = displayText,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            style = style,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            softWrap = false
        )
    }
}

private fun calculateStartEllipsisText(
    text: String,
    maxWidthPx: Int,
    style: TextStyle,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
): String {
    val fullTextWidth = textMeasurer.measure(text, style).size.width

    if (fullTextWidth <= maxWidthPx) {
        return text
    }

    val ellipsis = "..."
    val ellipsisWidth = textMeasurer.measure(ellipsis, style).size.width
    val availableWidth = maxWidthPx - ellipsisWidth

    if (availableWidth <= 0) {
        return ellipsis
    }

    // Find the longest suffix that fits using binary search
    var low = 0
    var high = text.length

    while (low < high) {
        val mid = (low + high) / 2
        val suffix = text.substring(mid)
        val suffixWidth = textMeasurer.measure(suffix, style).size.width

        if (suffixWidth <= availableWidth) {
            // This suffix fits, try to include more (move left)
            high = mid
        } else {
            // This suffix doesn't fit, need less text (move right)
            low = mid + 1
        }
    }

    return if (low >= text.length) {
        ellipsis
    } else {
        ellipsis + text.substring(low)
    }
}
