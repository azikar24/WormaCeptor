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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.engine.MetricStatus
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayState
import com.azikar24.wormaceptor.core.engine.R
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
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
            .clip(RoundedCornerShape(WormaCeptorTokens.Spacing.xl))
            .background(WormaCeptorTokens.Colors.Overlay.background)
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
            .padding(horizontal = WormaCeptorTokens.Spacing.md, vertical = WormaCeptorTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        if (state.cpuEnabled) {
            MetricDisplay(
                label = stringResource(R.string.overlay_metric_cpu),
                value = state.cpuPercent,
                suffix = "%",
                status = MetricStatus.fromCpuPercent(state.cpuPercent, state.cpuMonitorRunning),
            )
        }

        if (state.memoryEnabled) {
            MetricDisplay(
                label = stringResource(R.string.overlay_metric_mem),
                value = state.memoryPercent,
                suffix = "%",
                status = MetricStatus.fromMemoryPercent(state.memoryPercent, state.memoryMonitorRunning),
            )
        }

        if (state.fpsEnabled) {
            MetricDisplay(
                label = stringResource(R.string.overlay_metric_fps),
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
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        // Status indicator dot
        Canvas(modifier = Modifier.size(6.dp)) {
            drawCircle(color = color)
        }

        Text(
            text = label,
            style = WormaCeptorTokens.Typography.overlayLabel,
            color = WormaCeptorTokens.Colors.Overlay.textSecondary,
        )

        Text(
            text = formattedValue,
            style = WormaCeptorTokens.Typography.overlayValue,
            color = Color.White,
        )
    }
}

private fun MetricStatus.toColor(): Color = when (this) {
    MetricStatus.GOOD -> WormaCeptorTokens.Colors.Overlay.good
    MetricStatus.WARNING -> WormaCeptorTokens.Colors.Overlay.warning
    MetricStatus.CRITICAL -> WormaCeptorTokens.Colors.Overlay.critical
    MetricStatus.INACTIVE -> WormaCeptorTokens.Colors.Overlay.inactive
}
