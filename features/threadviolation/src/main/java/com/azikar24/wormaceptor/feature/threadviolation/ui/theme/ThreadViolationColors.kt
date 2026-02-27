package com.azikar24.wormaceptor.feature.threadviolation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType

/**
 * Colors for the Thread Violation Detection feature.
 * Uses centralized colors from WormaCeptorColors.ThreadViolation.
 *
 * @property primary Primary accent color for the feature.
 * @property diskRead Color representing disk-read violations.
 * @property diskWrite Color representing disk-write violations.
 * @property network Color representing network violations on the main thread.
 * @property slowCall Color representing slow method call violations.
 * @property customSlowCode Color representing custom slow-code violations.
 * @property monitoring Color indicating that violation monitoring is active.
 * @property idle Color indicating that violation monitoring is idle.
 * @property cardBackground Background color for violation cards.
 * @property detailBackground Background color for the violation detail view.
 * @property labelPrimary Primary text color for labels.
 * @property labelSecondary Secondary text color for less prominent labels.
 * @property valuePrimary Color for primary metric values.
 */
@Immutable
data class ThreadViolationColors(
    val primary: Color,
    val diskRead: Color,
    val diskWrite: Color,
    val network: Color,
    val slowCall: Color,
    val customSlowCode: Color,
    val monitoring: Color,
    val idle: Color,
    val cardBackground: Color,
    val detailBackground: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
) {
    /** Returns the color associated with the given violation type. */
    fun colorForType(type: ViolationType): Color = when (type) {
        ViolationType.DISK_READ -> diskRead
        ViolationType.DISK_WRITE -> diskWrite
        ViolationType.NETWORK -> network
        ViolationType.SLOW_CALL -> slowCall
        ViolationType.CUSTOM_SLOW_CODE -> customSlowCode
    }
}

/**
 * Returns the appropriate thread violation colors based on the current theme.
 */
@Composable
fun threadViolationColors(darkTheme: Boolean = isSystemInDarkTheme()): ThreadViolationColors {
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    return ThreadViolationColors(
        primary = MaterialTheme.colorScheme.primary,
        diskRead = WormaCeptorColors.ThreadViolation.DiskRead,
        diskWrite = WormaCeptorColors.ThreadViolation.DiskWrite,
        network = WormaCeptorColors.ThreadViolation.Network,
        slowCall = WormaCeptorColors.ThreadViolation.SlowCall,
        customSlowCode = WormaCeptorColors.ThreadViolation.CustomSlowCode,
        monitoring = WormaCeptorColors.ThreadViolation.Monitoring,
        idle = WormaCeptorColors.ThreadViolation.Idle,
        cardBackground = surface,
        detailBackground = surfaceVariant,
        labelPrimary = onSurface,
        labelSecondary = onSurfaceVariant,
        valuePrimary = onSurface.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
    )
}
