package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.max

@Composable
internal fun FpsChartCard(
    history: ImmutableList<FpsInfo>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WormaCeptorTokens.Spacing.lg),
        ) {
            Text(
                text = stringResource(R.string.fps_over_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.fps_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = WormaCeptorTokens.Alpha.BOLD,
                        ),
                    )
                }
            } else {
                val latestFps = history.lastOrNull()?.currentFps?.toInt() ?: 0
                val chartDescription = stringResource(R.string.fps_chart_content_description, latestFps)
                FpsChart(
                    data = history,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = chartDescription
                        },
                )
            }
        }
    }
}

@Composable
private fun FpsChart(
    data: ImmutableList<FpsInfo>,
    modifier: Modifier = Modifier,
) {
    val lineColor = WormaCeptorTokens.Colors.Fps.chartLine()
    val fillColor = WormaCeptorTokens.Colors.Fps.chartLine().copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
    val goodThresholdColor = WormaCeptorTokens.Colors.Fps.good.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
    val warningThresholdColor = WormaCeptorTokens.Colors.Fps.warning.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)

    // Calculate min/max for scaling
    val maxFps = remember(data) {
        max(data.maxOfOrNull { it.currentFps } ?: 60f, 65f)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 8f

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw horizontal grid lines and threshold indicators
        val gridLines = listOf(0f, 30f, 55f, maxFps)
        gridLines.forEach { fps ->
            val y = padding + chartHeight * (1 - fps / maxFps)

            // Draw threshold color bands
            when (fps) {
                55f -> {
                    drawLine(
                        color = goodThresholdColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 2f,
                    )
                }
                30f -> {
                    drawLine(
                        color = warningThresholdColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 2f,
                    )
                }
                else -> {
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 1f,
                    )
                }
            }
        }

        if (data.size < 2) return@Canvas

        // Create path for the line
        val linePath = Path()
        val fillPath = Path()

        val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)

        data.forEachIndexed { index, info ->
            val x = padding + index * xStep
            val y = padding + chartHeight * (1 - info.currentFps / maxFps)

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, padding + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Complete fill path
        fillPath.lineTo(padding + (data.size - 1) * xStep, padding + chartHeight)
        fillPath.close()

        // Draw fill
        drawPath(
            path = fillPath,
            color = fillColor,
        )

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3f),
        )
    }
}

@Preview(name = "FpsChartCard - Light")
@Composable
private fun FpsChartCardPreview() {
    WormaCeptorTheme {
        FpsChartCard(
            history = persistentListOf(
                FpsInfo(
                    currentFps = 58f,
                    averageFps = 58f,
                    minFps = 58f,
                    maxFps = 58f,
                    droppedFrames = 0,
                    jankFrames = 0,
                    timestamp = 1L,
                ),
                FpsInfo(
                    currentFps = 55f,
                    averageFps = 56f,
                    minFps = 55f,
                    maxFps = 58f,
                    droppedFrames = 1,
                    jankFrames = 0,
                    timestamp = 2L,
                ),
                FpsInfo(
                    currentFps = 42f,
                    averageFps = 51f,
                    minFps = 42f,
                    maxFps = 58f,
                    droppedFrames = 3,
                    jankFrames = 1,
                    timestamp = 3L,
                ),
                FpsInfo(
                    currentFps = 30f,
                    averageFps = 46f,
                    minFps = 30f,
                    maxFps = 58f,
                    droppedFrames = 5,
                    jankFrames = 2,
                    timestamp = 4L,
                ),
                FpsInfo(
                    currentFps = 50f,
                    averageFps = 47f,
                    minFps = 30f,
                    maxFps = 58f,
                    droppedFrames = 5,
                    jankFrames = 2,
                    timestamp = 5L,
                ),
                FpsInfo(
                    currentFps = 60f,
                    averageFps = 49f,
                    minFps = 30f,
                    maxFps = 60f,
                    droppedFrames = 5,
                    jankFrames = 2,
                    timestamp = 6L,
                ),
            ),
            modifier = Modifier.height(200.dp),
        )
    }
}

@Preview(name = "FpsChartCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FpsChartCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        FpsChartCard(
            history = persistentListOf(),
            modifier = Modifier.height(200.dp),
        )
    }
}
