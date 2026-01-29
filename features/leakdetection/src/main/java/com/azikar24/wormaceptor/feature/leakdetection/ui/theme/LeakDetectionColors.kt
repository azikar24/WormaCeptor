/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.leakdetection.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity

/**
 * Colors for the Leak Detection feature.
 * Uses centralized colors from WormaCeptorColors.LeakDetection.
 */
@Immutable
data class LeakDetectionColors(
    val critical: Color,
    val criticalBackground: Color,
    val high: Color,
    val highBackground: Color,
    val medium: Color,
    val mediumBackground: Color,
    val low: Color,
    val lowBackground: Color,
    val monitoring: Color,
    val idle: Color,
    val cardBackground: Color,
    val surfaceBackground: Color,
    val detailBackground: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
    val actionButton: Color,
    val actionButtonText: Color,
    val divider: Color,
) {
    fun colorForSeverity(severity: LeakSeverity): Color = when (severity) {
        LeakSeverity.CRITICAL -> critical
        LeakSeverity.HIGH -> high
        LeakSeverity.MEDIUM -> medium
        LeakSeverity.LOW -> low
    }

    fun backgroundForSeverity(severity: LeakSeverity): Color = when (severity) {
        LeakSeverity.CRITICAL -> criticalBackground
        LeakSeverity.HIGH -> highBackground
        LeakSeverity.MEDIUM -> mediumBackground
        LeakSeverity.LOW -> lowBackground
    }
}

/**
 * Returns the appropriate leak detection colors based on the current theme.
 */
@Composable
fun leakDetectionColors(darkTheme: Boolean = isSystemInDarkTheme()): LeakDetectionColors {
    val alpha = WormaCeptorDesignSystem.Alpha
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline
    val primary = MaterialTheme.colorScheme.primary

    return LeakDetectionColors(
        critical = WormaCeptorColors.LeakDetection.Critical,
        criticalBackground = WormaCeptorColors.LeakDetection.Critical.copy(alpha = alpha.subtle),
        high = WormaCeptorColors.LeakDetection.High,
        highBackground = WormaCeptorColors.LeakDetection.High.copy(alpha = alpha.subtle),
        medium = WormaCeptorColors.LeakDetection.Medium,
        mediumBackground = WormaCeptorColors.LeakDetection.Medium.copy(alpha = alpha.subtle),
        low = WormaCeptorColors.LeakDetection.Low,
        lowBackground = WormaCeptorColors.LeakDetection.Low.copy(alpha = alpha.subtle),
        monitoring = WormaCeptorColors.LeakDetection.Monitoring,
        idle = WormaCeptorColors.LeakDetection.Idle,
        cardBackground = surface,
        surfaceBackground = surface,
        detailBackground = surfaceVariant,
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = 0.87f),
        actionButton = primary,
        actionButtonText = MaterialTheme.colorScheme.onPrimary,
        divider = outline.copy(alpha = 0.2f),
    )
}
