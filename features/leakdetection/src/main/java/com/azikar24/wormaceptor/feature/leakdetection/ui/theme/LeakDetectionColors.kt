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
 *
 * @property critical Foreground color for critical-severity leaks.
 * @property criticalBackground Background color for critical-severity leak badges.
 * @property high Foreground color for high-severity leaks.
 * @property highBackground Background color for high-severity leak badges.
 * @property medium Foreground color for medium-severity leaks.
 * @property mediumBackground Background color for medium-severity leak badges.
 * @property low Foreground color for low-severity leaks.
 * @property lowBackground Background color for low-severity leak badges.
 * @property monitoring Color indicating that leak detection is actively scanning.
 * @property idle Color indicating that leak detection is idle.
 * @property cardBackground Background color for leak info cards.
 * @property surfaceBackground General surface background color.
 * @property detailBackground Background color for the leak detail view.
 * @property labelPrimary Primary text color for labels.
 * @property labelSecondary Secondary text color for less prominent labels.
 * @property valuePrimary Color for primary metric values.
 * @property actionButton Color for action buttons (e.g., trigger check).
 * @property actionButtonText Text color for action buttons.
 * @property divider Color for divider lines between sections.
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
    /** Returns the foreground color for the given leak severity level. */
    fun colorForSeverity(severity: LeakSeverity): Color = when (severity) {
        LeakSeverity.CRITICAL -> critical
        LeakSeverity.HIGH -> high
        LeakSeverity.MEDIUM -> medium
        LeakSeverity.LOW -> low
    }

    /** Returns the background color for the given leak severity level. */
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
        valuePrimary = onSurface.copy(alpha = alpha.prominent),
        actionButton = primary,
        actionButtonText = MaterialTheme.colorScheme.onPrimary,
        divider = outline.copy(alpha = alpha.medium),
    )
}
