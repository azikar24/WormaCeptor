package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max

/**
 * A version-agnostic FlowRow that wraps children into multiple rows using the stable [Layout] composable.
 * Avoids binary compatibility issues with different versions of androidx.compose.foundation.
 */
@Suppress("LongMethod")
@Composable
fun WormaCeptorFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val horizontalSpacing = horizontalArrangement.spacing.roundToPx()
        val verticalSpacing = verticalArrangement.spacing.roundToPx()
        val result = buildRows(measurables, constraints, horizontalSpacing)

        val totalHeight = result.rowHeights.sum() + max(0, (result.rows.size - 1) * verticalSpacing)
        val layoutWidth = constraints.maxWidth
        val layoutHeight = totalHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

        layout(layoutWidth, layoutHeight) {
            placeRows(
                result.rows,
                result.rowHeights,
                layoutWidth,
                layoutHeight,
                horizontalArrangement,
                verticalArrangement,
            )
        }
    }
}

private data class FlowRowResult(
    val rows: List<List<Placeable>>,
    val rowHeights: List<Int>,
)

private fun buildRows(
    measurables: List<androidx.compose.ui.layout.Measurable>,
    constraints: androidx.compose.ui.unit.Constraints,
    horizontalSpacing: Int,
): FlowRowResult {
    val rows = mutableListOf<MutableList<Placeable>>()
    val rowHeights = mutableListOf<Int>()

    var currentRow = mutableListOf<Placeable>()
    var currentRowWidth = 0
    var currentRowHeight = 0

    measurables.forEach { measurable ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        val spacingToAdd = if (currentRow.isNotEmpty()) horizontalSpacing else 0

        if (currentRowWidth + spacingToAdd + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowHeights.add(currentRowHeight)
            currentRow = mutableListOf()
            currentRowWidth = 0
            currentRowHeight = 0
        }

        currentRow.add(placeable)
        currentRowWidth += (if (currentRow.size > 1) horizontalSpacing else 0) + placeable.width
        currentRowHeight = max(currentRowHeight, placeable.height)
    }

    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
        rowHeights.add(currentRowHeight)
    }

    return FlowRowResult(rows, rowHeights)
}

@Suppress("LongParameterList")
private fun androidx.compose.ui.layout.Placeable.PlacementScope.placeRows(
    rows: List<List<Placeable>>,
    rowHeights: List<Int>,
    layoutWidth: Int,
    layoutHeight: Int,
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical,
) {
    val verticalOffsets = IntArray(rows.size)
    with(verticalArrangement) {
        arrange(
            totalSize = layoutHeight,
            sizes = rowHeights.toIntArray(),
            outPositions = verticalOffsets,
        )
    }

    rows.forEachIndexed { rowIndex, row ->
        val rowItemWidths = IntArray(row.size) { row[it].width }
        val horizontalOffsets = IntArray(row.size)
        with(horizontalArrangement) {
            arrange(
                totalSize = layoutWidth,
                sizes = rowItemWidths,
                layoutDirection = LayoutDirection.Ltr,
                outPositions = horizontalOffsets,
            )
        }

        row.forEachIndexed { itemIndex, placeable ->
            placeable.placeRelative(
                x = horizontalOffsets[itemIndex],
                y = verticalOffsets[rowIndex],
            )
        }
    }
}
