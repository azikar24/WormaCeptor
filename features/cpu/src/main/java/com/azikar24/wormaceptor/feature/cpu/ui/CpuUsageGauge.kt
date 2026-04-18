package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.CpuMeasurementSource
import com.azikar24.wormaceptor.feature.cpu.R
import java.text.DecimalFormat
import kotlin.math.roundToInt

private const val CpuCriticalThreshold = 80f
private const val CpuWarningThreshold = 50f
private const val CpuPercentDivisor = 100f
private const val GaugeStartAngle = 135f
private const val GaugeSweepAngle = 270f
private val CpuPercentFormatter = DecimalFormat("#,##0.0")

@Composable
internal fun CpuUsageGaugeCard(
    currentCpu: CpuInfo,
    isWarning: Boolean,
    modifier: Modifier = Modifier,
) {
    val statusColor = when {
        currentCpu.overallUsagePercent >= CpuCriticalThreshold -> WormaCeptorTokens.Colors.Status.red
        currentCpu.overallUsagePercent >= CpuWarningThreshold -> WormaCeptorTokens.Colors.Status.amber
        else -> WormaCeptorTokens.Colors.Status.green
    }
    // Animated sweep angle for the gauge
    val animatedProgress by animateFloatAsState(
        targetValue = currentCpu.overallUsagePercent / CpuPercentDivisor,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.VERY_SLOW),
        label = "gauge_progress",
    )

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardExtraLarge,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            GaugeHeader(
                currentCpu = currentCpu,
                isWarning = isWarning,
                statusColor = statusColor,
            )

            GaugeCircle(
                animatedProgress = animatedProgress,
                statusColor = statusColor,
                formattedPercent = "${CpuPercentFormatter.format(currentCpu.overallUsagePercent)}%",
                coreCount = currentCpu.coreCount,
            )
        }
    }
}

@Composable
private fun GaugeHeader(
    currentCpu: CpuInfo,
    isWarning: Boolean,
    statusColor: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = stringResource(R.string.cpu_title),
                tint = statusColor,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.xl),
            )
            Column {
                Text(
                    text = stringResource(R.string.cpu_overall_usage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = when (currentCpu.measurementSource) {
                        CpuMeasurementSource.SYSTEM -> stringResource(R.string.cpu_measurement_system)
                        CpuMeasurementSource.PROCESS -> stringResource(R.string.cpu_measurement_process)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (isWarning) {
            CpuWarningBadge()
        }
    }
}

@Composable
private fun CpuWarningBadge() {
    Surface(
        shape = WormaCeptorTokens.Shapes.card,
        color = WormaCeptorTokens.Colors.Status.red.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(R.string.cpu_warning_high),
                tint = WormaCeptorTokens.Colors.Status.red,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.lg),
            )
            Text(
                text = stringResource(R.string.cpu_high),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = WormaCeptorTokens.Colors.Status.red,
            )
        }
    }
}

@Composable
private fun GaugeCircle(
    animatedProgress: Float,
    statusColor: Color,
    formattedPercent: String,
    coreCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WormaCeptorTokens.ComponentSize.gaugeContainerHeight),
        contentAlignment = Alignment.Center,
    ) {
        val cpuPercentage = (animatedProgress * CpuPercentDivisor).roundToInt()
        val cpuUsageDescription = stringResource(
            id = R.string.cpu_usage_content_description,
            cpuPercentage,
        )

        CpuGauge(
            progress = animatedProgress,
            statusColor = statusColor,
            modifier = Modifier
                .size(WormaCeptorTokens.ComponentSize.gaugeSize)
                .semantics {
                    contentDescription = cpuUsageDescription
                },
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formattedPercent,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = statusColor,
            )
            Text(
                text = stringResource(R.string.cpu_core_count, coreCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CpuGauge(
    progress: Float,
    statusColor: Color,
    modifier: Modifier = Modifier,
) {
    val arcBackground = MaterialTheme.colorScheme.outline.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)

    Canvas(modifier = modifier) {
        val strokeWidth = WormaCeptorTokens.ComponentSize.gaugeStrokeWidth.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw background arc
        drawArc(
            color = arcBackground,
            startAngle = GaugeStartAngle,
            sweepAngle = GaugeSweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Draw progress arc
        drawArc(
            color = statusColor,
            startAngle = GaugeStartAngle,
            sweepAngle = GaugeSweepAngle * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CpuUsageGaugeCardNormalPreview() {
    WormaCeptorTheme {
        CpuUsageGaugeCard(
            currentCpu = CpuInfo(
                timestamp = System.currentTimeMillis(),
                overallUsagePercent = 25f,
                perCoreUsage = listOf(20f, 30f, 22f, 28f),
                coreCount = 4,
                cpuFrequencyMHz = 2400L,
                cpuTemperature = 38f,
            ),
            isWarning = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CpuUsageGaugeCardWarningPreview() {
    WormaCeptorTheme {
        CpuUsageGaugeCard(
            currentCpu = CpuInfo(
                timestamp = System.currentTimeMillis(),
                overallUsagePercent = 85f,
                perCoreUsage = listOf(80f, 90f, 82f, 88f),
                coreCount = 4,
                cpuFrequencyMHz = 2400L,
                cpuTemperature = 72f,
                measurementSource = CpuMeasurementSource.PROCESS,
            ),
            isWarning = true,
        )
    }
}
