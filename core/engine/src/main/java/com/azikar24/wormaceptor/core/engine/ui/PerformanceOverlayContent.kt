package com.azikar24.wormaceptor.core.engine.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.engine.MetricStatus
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayState

// Colors
private val PillBackground = Color(0xFF1C1C1E)
private val GoodColor = Color(0xFF32D74B)
private val WarningColor = Color(0xFFFF9F0A)
private val CriticalColor = Color(0xFFFF453A)
private val InactiveColor = Color(0xFF8E8E93)
private val TextSecondary = Color(0xFF8E8E93)

/**
 * iOS Dynamic Island-style performance overlay.
 *
 * Displays a compact floating pill showing performance metrics.
 * Drag to reposition the pill anywhere on screen.
 */
@Composable
fun PerformanceOverlayContent(
    state: PerformanceOverlayState,
    callbacks: PerformanceOverlayEngine.OverlayCallbacks,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(PillBackground)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { callbacks.onDragStart() },
                    onDragEnd = { callbacks.onDragEnd() },
                    onDragCancel = { callbacks.onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        callbacks.onDrag(dragAmount)
                    },
                )
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.cpuEnabled) {
            MetricDisplay(
                label = "CPU",
                value = state.cpuPercent,
                suffix = "%",
                status = MetricStatus.fromCpuPercent(state.cpuPercent, state.cpuMonitorRunning),
            )
        }

        if (state.memoryEnabled) {
            MetricDisplay(
                label = "MEM",
                value = state.memoryPercent,
                suffix = "%",
                status = MetricStatus.fromMemoryPercent(state.memoryPercent, state.memoryMonitorRunning),
            )
        }

        if (state.fpsEnabled) {
            MetricDisplay(
                label = "FPS",
                value = state.fpsValue,
                suffix = "",
                status = MetricStatus.fromFps(state.fpsValue, state.fpsMonitorRunning),
            )
        }
    }
}

@Composable
private fun MetricDisplay(
    label: String,
    value: Int,
    suffix: String,
    status: MetricStatus,
    modifier: Modifier = Modifier,
) {
    val color = status.toColor()

    // Format value off the composition path
    val formattedValue = remember(value, suffix) {
        if (suffix.isNotEmpty()) "$value$suffix" else value.toString()
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Status indicator dot
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = color)
        }

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            letterSpacing = 0.5.sp,
        )

        Text(
            text = formattedValue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = Color.White,
        )
    }
}

private fun MetricStatus.toColor(): Color = when (this) {
    MetricStatus.GOOD -> GoodColor
    MetricStatus.WARNING -> WarningColor
    MetricStatus.CRITICAL -> CriticalColor
    MetricStatus.INACTIVE -> InactiveColor
}
