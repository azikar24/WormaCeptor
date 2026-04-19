package com.azikar24.wormaceptor.core.ui.components.section

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Horizontally scrollable row.
 *
 * When [bleed] is true (default), the row extends its scroll viewport beyond the parent's
 * horizontal padding by [contentPadding] on each side, so scrolling appears edge-to-edge.
 * Requires the parent to have horizontal padding matching [contentPadding].
 *
 * When [bleed] is false, the row stays within the parent's content area and [contentPadding]
 * acts as literal, always-visible inner padding.
 */
@Composable
fun WormaCeptorScrollableRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    bleed: Boolean = true,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val startPadding = contentPadding.calculateStartPadding(layoutDirection)
    val endPadding = contentPadding.calculateEndPadding(layoutDirection)
    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()

    val bleedModifier = if (bleed) {
        Modifier.escapeHorizontalPadding(startPadding, endPadding)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .padding(top = topPadding, bottom = bottomPadding)
            .then(bleedModifier)
            .horizontalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.width(startPadding))
        Row(horizontalArrangement = horizontalArrangement) {
            content()
        }
        Spacer(Modifier.width(endPadding))
    }
}

private fun Modifier.escapeHorizontalPadding(
    start: Dp,
    end: Dp,
) = layout { measurable, constraints ->
    val startPx = start.roundToPx()
    val endPx = end.roundToPx()
    val expandedConstraints = constraints.copy(
        maxWidth = constraints.maxWidth + startPx + endPx,
    )
    val placeable = measurable.measure(expandedConstraints)
    val layoutWidth = constraints.maxWidth.coerceAtLeast(0)
    layout(layoutWidth, placeable.height) {
        placeable.place(-startPx, 0)
    }
}
