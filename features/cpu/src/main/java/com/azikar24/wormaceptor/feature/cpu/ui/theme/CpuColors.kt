/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cpu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Colors for the CPU Monitoring feature.
 * Uses centralized colors from WormaCeptorColors.Cpu for chart colors.
 */
@Immutable
data class CpuColors(
    val cpuUsage: Color,
    val cpuUsageLight: Color,
    val coreColors: List<Color>,
    val normal: Color,
    val warning: Color,
    val critical: Color,
    val cardBackground: Color,
    val chartBackground: Color,
    val gridLines: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
    val gaugeBackground: Color,
    val gaugeTrack: Color,
) {
    fun statusColorForUsage(usagePercent: Float): Color = when {
        usagePercent >= 80f -> critical
        usagePercent >= 50f -> warning
        else -> normal
    }

    fun colorForCore(index: Int): Color = WormaCeptorColors.Cpu.forCore(index)
}

/**
 * Returns the appropriate CPU colors based on the current theme.
 */
@Composable
fun cpuColors(darkTheme: Boolean = isSystemInDarkTheme()): CpuColors {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    return CpuColors(
        cpuUsage = WormaCeptorColors.Cpu.Usage,
        cpuUsageLight = WormaCeptorColors.Cpu.UsageLight,
        coreColors = WormaCeptorColors.Cpu.CoreColors,
        normal = WormaCeptorColors.StatusGreen,
        warning = WormaCeptorColors.StatusAmber,
        critical = WormaCeptorColors.StatusRed,
        cardBackground = surfaceColor,
        chartBackground = surfaceVariant,
        gridLines = outline.copy(alpha = 0.3f),
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = 0.87f),
        gaugeBackground = surfaceVariant,
        gaugeTrack = outline.copy(alpha = 0.2f),
    )
}
