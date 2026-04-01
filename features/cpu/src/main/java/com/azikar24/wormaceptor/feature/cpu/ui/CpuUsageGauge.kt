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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.CpuMeasurementSource
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.ui.theme.CpuColors
import java.text.DecimalFormat

@Composable
internal fun CpuUsageGaugeCard(
    currentCpu: CpuInfo,
    isWarning: Boolean,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    val statusColor = colors.statusColorForUsage(currentCpu.overallUsagePercent)
    val formatter = DecimalFormat("#,##0.0")

    // Animated sweep angle for the gauge
    val animatedProgress by animateFloatAsState(
        targetValue = currentCpu.overallUsagePercent / 100f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.VERY_SLOW),
        label = "gauge_progress",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = stringResource(R.string.cpu_title),
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.cpu_overall_usage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.labelPrimary,
                        )
                        Text(
                            text = when (currentCpu.measurementSource) {
                                CpuMeasurementSource.SYSTEM -> stringResource(R.string.cpu_measurement_system)
                                CpuMeasurementSource.PROCESS -> stringResource(R.string.cpu_measurement_process)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.labelSecondary,
                        )
                    }
                }

                // Warning indicator
                if (isWarning) {
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                        color = colors.critical.copy(alpha = WormaCeptorDesignSystem.Alpha.LIGHT),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xs,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.cpu_warning_high),
                                tint = colors.critical,
                                modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.lg),
                            )
                            Text(
                                text = stringResource(R.string.cpu_high),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.critical,
                            )
                        }
                    }
                }
            }

            // Circular gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                val cpuPercentage = (animatedProgress * 100).toInt()
                val cpuUsageDescription = stringResource(
                    id = R.string.cpu_usage_content_description,
                    cpuPercentage,
                )

                CpuGauge(
                    progress = animatedProgress,
                    statusColor = statusColor,
                    colors = colors,
                    modifier = Modifier
                        .size(160.dp)
                        .semantics {
                            contentDescription = cpuUsageDescription
                        },
                )

                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${formatter.format(currentCpu.overallUsagePercent)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                    Text(
                        text = stringResource(R.string.cpu_core_count, currentCpu.coreCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.labelSecondary,
                    )
                }
            }
        }
    }
}

@Composable
internal fun CpuGauge(
    progress: Float,
    statusColor: Color,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = WormaCeptorDesignSystem.Spacing.lg.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw background arc
        drawArc(
            color = colors.gaugeTrack,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Draw progress arc
        drawArc(
            color = statusColor,
            startAngle = 135f,
            sweepAngle = 270f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}
