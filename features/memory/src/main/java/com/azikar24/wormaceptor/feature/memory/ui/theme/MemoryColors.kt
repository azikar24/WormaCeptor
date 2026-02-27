package com.azikar24.wormaceptor.feature.memory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Colors for the Memory Monitoring feature.
 * Uses centralized colors from WormaCeptorColors.Memory for chart colors
 * and WormaCeptorColors status colors for status indicators.
 *
 * @property heapUsed Color representing used heap memory in charts.
 * @property heapFree Color representing free heap memory in charts.
 * @property heapTotal Color representing total heap capacity in charts.
 * @property nativeHeap Color representing native heap memory in charts.
 * @property normal Color indicating normal (healthy) memory usage.
 * @property warning Color indicating elevated memory usage.
 * @property critical Color indicating dangerously high memory usage.
 * @property cardBackground Background color for memory metric cards.
 * @property chartBackground Background color for memory charts.
 * @property gridLines Color for chart grid lines.
 * @property labelPrimary Primary text color for labels.
 * @property labelSecondary Secondary text color for less prominent labels.
 * @property valuePrimary Color for primary metric values.
 */
@Immutable
data class MemoryColors(
    val heapUsed: Color,
    val heapFree: Color,
    val heapTotal: Color,
    val nativeHeap: Color,
    val normal: Color,
    val warning: Color,
    val critical: Color,
    val cardBackground: Color,
    val chartBackground: Color,
    val gridLines: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
) {
    /** Returns a status color (normal, warning, critical) based on heap usage percentage. */
    fun statusColorForUsage(usagePercent: Float): Color = when {
        usagePercent >= 80f -> critical
        usagePercent >= 60f -> warning
        else -> normal
    }
}

/**
 * Returns the appropriate memory colors based on the current theme.
 * Uses centralized WormaCeptorColors for chart colors and Material theme for surfaces.
 */
@Composable
fun memoryColors(darkTheme: Boolean = isSystemInDarkTheme()): MemoryColors {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    return MemoryColors(
        heapUsed = WormaCeptorColors.Memory.HeapUsed,
        heapFree = WormaCeptorColors.Memory.HeapFree,
        heapTotal = WormaCeptorColors.Memory.HeapTotal,
        nativeHeap = WormaCeptorColors.Memory.NativeHeap,
        normal = WormaCeptorColors.StatusGreen,
        warning = WormaCeptorColors.StatusAmber,
        critical = WormaCeptorColors.StatusRed,
        cardBackground = surfaceColor,
        chartBackground = surfaceVariant,
        gridLines = outline.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
    )
}
