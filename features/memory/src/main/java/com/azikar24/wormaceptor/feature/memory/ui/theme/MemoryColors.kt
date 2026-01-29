/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.memory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Colors for the Memory Monitoring feature.
 * Uses centralized colors from WormaCeptorColors.Memory for chart colors
 * and WormaCeptorColors status colors for status indicators.
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
        gridLines = outline.copy(alpha = 0.3f),
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = 0.87f),
    )
}
