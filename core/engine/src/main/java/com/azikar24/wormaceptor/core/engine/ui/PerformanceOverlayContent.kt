/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.engine.MetricStatus
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayState

/**
 * Performance overlay content composable.
 *
 * Displays a compact badge showing FPS, Memory, and CPU metrics.
 * Expands on tap to show mini sparkline charts for each metric.
 * Supports drag gestures for repositioning.
 */
@Composable
fun PerformanceOverlayContent(
    state: PerformanceOverlayState,
    callbacks: PerformanceOverlayEngine.OverlayCallbacks,
    modifier: Modifier = Modifier,
) {
    var isLongPressing by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (state.isDragging) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "drag_scale",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { callbacks.onToggleExpanded() },
                    onLongPress = {
                        isLongPressing = true
                        callbacks.onDragStart()
                    },
                )
            }
            .pointerInput(state.isDragging) {
                if (state.isDragging) {
                    detectDragGestures(
                        onDragEnd = {
                            isLongPressing = false
                            callbacks.onDragEnd()
                        },
                        onDragCancel = {
                            isLongPressing = false
                            callbacks.onDragEnd()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            callbacks.onDrag(dragAmount)
                        },
                    )
                }
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC1E1E1E), // Semi-transparent dark
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(8.dp),
        ) {
            if (state.isExpanded) {
                ExpandedContent(
                    state = state,
                    onMetricClick = { metricType ->
                        when (metricType) {
                            "fps" -> callbacks.onOpenFpsDetail()
                            "memory" -> callbacks.onOpenMemoryDetail()
                            "cpu" -> callbacks.onOpenCpuDetail()
                        }
                    },
                    onOpenWormaCeptor = callbacks.onOpenWormaCeptor,
                )
            } else {
                CollapsedContent(state = state)
            }
        }
    }
}

@Composable
private fun CollapsedContent(state: PerformanceOverlayState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // CPU
        MetricBadge(
            icon = Icons.Default.Speed,
            value = "${state.cpuPercent}%",
            status = MetricStatus.fromCpuPercent(state.cpuPercent),
        )

        Divider()

        // Memory
        MetricBadge(
            icon = Icons.Default.Memory,
            value = "${state.memoryPercent}%",
            status = MetricStatus.fromMemoryPercent(state.memoryPercent),
        )

        Divider()

        // FPS
        MetricBadge(
            icon = null,
            label = "FPS",
            value = "${state.fpsValue}",
            status = MetricStatus.fromFps(state.fpsValue),
        )
    }
}

@Composable
private fun MetricBadge(
    icon: ImageVector?,
    value: String,
    status: MetricStatus,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val color = status.toColor()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color,
            )
        } else if (label != null) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.8f),
            )
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color,
        )
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(16.dp)
            .background(Color.White.copy(alpha = 0.2f)),
    )
}

@Composable
private fun ExpandedContent(
    state: PerformanceOverlayState,
    onMetricClick: (String) -> Unit,
    onOpenWormaCeptor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(140.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // CPU Row
        MetricRow(
            icon = Icons.Default.Speed,
            label = "CPU",
            value = "${state.cpuPercent}%",
            history = state.cpuHistory,
            status = MetricStatus.fromCpuPercent(state.cpuPercent),
            onClick = { onMetricClick("cpu") },
        )

        // Memory Row
        MetricRow(
            icon = Icons.Default.Memory,
            label = "MEM",
            value = "${state.memoryPercent}%",
            history = state.memoryHistory,
            status = MetricStatus.fromMemoryPercent(state.memoryPercent),
            onClick = { onMetricClick("memory") },
        )

        // FPS Row
        MetricRow(
            icon = null,
            label = "FPS",
            value = "${state.fpsValue}",
            history = state.fpsHistory,
            status = MetricStatus.fromFps(state.fpsValue),
            maxValue = 70f,
            onClick = { onMetricClick("fps") },
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Open WormaCeptor button
        Surface(
            onClick = onOpenWormaCeptor,
            shape = RoundedCornerShape(8.dp),
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Open",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open WormaCeptor",
                    modifier = Modifier.size(12.dp),
                    tint = Color.White.copy(alpha = 0.9f),
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: ImageVector?,
    label: String,
    value: String,
    history: List<Float>,
    status: MetricStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxValue: Float = 100f,
) {
    val color = status.toColor()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon and value
            Row(
                modifier = Modifier.width(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = color,
                    )
                } else {
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = color.copy(alpha = 0.8f),
                    )
                }
                Text(
                    text = value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = color,
                )
            }

            // Mini sparkline
            MiniSparkline(
                data = history,
                color = color,
                maxValue = maxValue,
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp),
            )
        }
    }
}

@Composable
private fun MiniSparkline(data: List<Float>, color: Color, maxValue: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 2f

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)

        val path = Path()

        data.forEachIndexed { index, value ->
            val x = padding + index * xStep
            val normalizedValue = (value / maxValue).coerceIn(0f, 1f)
            val y = padding + chartHeight * (1 - normalizedValue)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Draw line
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.5f),
        )

        // Draw current value dot
        if (data.isNotEmpty()) {
            val lastValue = data.last()
            val lastX = padding + (data.size - 1) * xStep
            val normalizedLast = (lastValue / maxValue).coerceIn(0f, 1f)
            val lastY = padding + chartHeight * (1 - normalizedLast)

            drawCircle(
                color = color,
                radius = 3f,
                center = Offset(lastX, lastY),
            )
        }
    }
}

/**
 * Extension function to convert MetricStatus to a color.
 */
private fun MetricStatus.toColor(): Color = when (this) {
    MetricStatus.GOOD -> Color(0xFF4CAF50) // Green
    MetricStatus.WARNING -> Color(0xFFFF9800) // Orange
    MetricStatus.CRITICAL -> Color(0xFFF44336) // Red
}
