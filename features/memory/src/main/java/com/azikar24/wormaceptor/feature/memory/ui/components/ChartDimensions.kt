package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset

@Immutable
internal data class ChartDimensions(
    val padding: Float,
    val chartWidth: Float,
    val chartHeight: Float,
    val pointCount: Int,
) {
    fun toOffset(
        index: Int,
        value: Long,
        maxValue: Long,
    ): Offset {
        val x = padding + chartWidth / (pointCount - 1) * index
        val y = padding + chartHeight - value.toFloat() / maxValue * chartHeight
        return Offset(x, y)
    }

    companion object {
        const val BytesPerMb = 1_048_576L
        const val GridLineCount = 4
    }
}
