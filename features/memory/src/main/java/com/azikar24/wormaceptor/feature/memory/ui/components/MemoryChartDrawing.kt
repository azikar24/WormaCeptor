package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import kotlinx.collections.immutable.ImmutableList

internal fun DrawScope.drawMemoryGridLines(
    gridColor: Color,
    dimensions: ChartDimensions,
) {
    val endX = size.width - dimensions.padding
    for (i in 0..ChartDimensions.GridLineCount) {
        val y = dimensions.padding + dimensions.chartHeight / ChartDimensions.GridLineCount * i
        drawLine(
            color = gridColor,
            start = Offset(dimensions.padding, y),
            end = Offset(endX, y),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

internal inline fun DrawScope.drawLinePath(
    history: ImmutableList<MemoryInfo>,
    maxMemory: Long,
    color: Color,
    dimensions: ChartDimensions,
    valueSelector: (MemoryInfo) -> Long,
) {
    val path = Path()
    history.forEachIndexed { index, info ->
        val offset = dimensions.toOffset(index, valueSelector(info), maxMemory)
        if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
    )
}

internal inline fun DrawScope.drawAreaFill(
    history: ImmutableList<MemoryInfo>,
    maxMemory: Long,
    color: Color,
    dimensions: ChartDimensions,
    valueSelector: (MemoryInfo) -> Long,
) {
    val areaPath = Path()
    val baseline = dimensions.padding + dimensions.chartHeight
    history.forEachIndexed { index, info ->
        val offset = dimensions.toOffset(index, valueSelector(info), maxMemory)
        if (index == 0) {
            areaPath.moveTo(offset.x, baseline)
            areaPath.lineTo(offset.x, offset.y)
        } else {
            areaPath.lineTo(offset.x, offset.y)
        }
    }
    areaPath.lineTo(dimensions.padding + dimensions.chartWidth, baseline)
    areaPath.close()

    drawPath(
        path = areaPath,
        color = color.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
    )
}
