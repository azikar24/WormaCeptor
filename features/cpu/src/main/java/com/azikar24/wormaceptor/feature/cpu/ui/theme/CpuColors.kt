package com.azikar24.wormaceptor.feature.cpu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Colors for the CPU Monitoring feature.
 * Uses centralized colors from WormaCeptorColors.Cpu for chart colors.
 *
 * @property cpuUsage Primary color for overall CPU usage indicators.
 * @property cpuUsageLight Lighter variant of the CPU usage color for fills and backgrounds.
 * @property coreColors Distinct colors for individual CPU cores in charts.
 * @property normal Color indicating normal (healthy) CPU usage.
 * @property warning Color indicating elevated CPU usage.
 * @property critical Color indicating dangerously high CPU usage.
 * @property cardBackground Background color for CPU metric cards.
 * @property chartBackground Background color for CPU usage charts.
 * @property gridLines Color for chart grid lines.
 * @property labelPrimary Primary text color for labels.
 * @property labelSecondary Secondary text color for less prominent labels.
 * @property valuePrimary Color for primary metric values.
 * @property gaugeBackground Background color for the CPU gauge.
 * @property gaugeTrack Track color for the CPU gauge ring.
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
    /** Returns a status color (normal, warning, critical) based on CPU usage percentage. */
    fun statusColorForUsage(usagePercent: Float): Color = when {
        usagePercent >= 80f -> critical
        usagePercent >= 50f -> warning
        else -> normal
    }

    /** Returns a distinct color for the CPU core at the given index. */
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
        gridLines = outline.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
        gaugeBackground = surfaceVariant,
        gaugeTrack = outline.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
    )
}
