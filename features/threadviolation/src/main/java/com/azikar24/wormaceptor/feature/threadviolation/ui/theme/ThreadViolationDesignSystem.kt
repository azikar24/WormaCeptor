/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.threadviolation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType

/**
 * Colors for the Thread Violation Detection feature.
 *
 * Uses a color scheme based on violation type:
 * - Disk operations: Blue tones
 * - Network: Orange tones
 * - Slow calls: Red tones
 */
@Immutable
data class ThreadViolationColors(
    // Primary accent
    val primary: Color,

    // Violation type colors
    val diskRead: Color,
    val diskWrite: Color,
    val network: Color,
    val slowCall: Color,
    val customSlowCode: Color,

    // Status colors
    val monitoring: Color,
    val idle: Color,

    // Background colors
    val cardBackground: Color,
    val detailBackground: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
) {
    /**
     * Returns the color for a violation type.
     */
    fun colorForType(type: ViolationType): Color = when (type) {
        ViolationType.DISK_READ -> diskRead
        ViolationType.DISK_WRITE -> diskWrite
        ViolationType.NETWORK -> network
        ViolationType.SLOW_CALL -> slowCall
        ViolationType.CUSTOM_SLOW_CODE -> customSlowCode
    }
}

/**
 * Light theme thread violation colors.
 */
val LightThreadViolationColors = ThreadViolationColors(
    primary = Color(0xFFE91E63), // Pink 500

    diskRead = Color(0xFF2196F3), // Blue 500
    diskWrite = Color(0xFF1565C0), // Blue 800
    network = Color(0xFFFF9800), // Orange 500
    slowCall = Color(0xFFF44336), // Red 500
    customSlowCode = Color(0xFF9C27B0), // Purple 500

    monitoring = Color(0xFF4CAF50), // Green 500
    idle = Color(0xFF9E9E9E), // Grey 500

    cardBackground = Color(0xFFFAFAFA),
    detailBackground = Color(0xFFF5F5F5),

    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),
)

/**
 * Dark theme thread violation colors.
 */
val DarkThreadViolationColors = ThreadViolationColors(
    primary = Color(0xFFF48FB1), // Pink 200

    diskRead = Color(0xFF64B5F6), // Blue 300
    diskWrite = Color(0xFF42A5F5), // Blue 400
    network = Color(0xFFFFB74D), // Orange 300
    slowCall = Color(0xFFE57373), // Red 300
    customSlowCode = Color(0xFFCE93D8), // Purple 200

    monitoring = Color(0xFF81C784), // Green 300
    idle = Color(0xFF757575), // Grey 600

    cardBackground = Color(0xFF1E1E1E),
    detailBackground = Color(0xFF2D2D2D),

    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),
)

/**
 * Returns the appropriate thread violation colors based on the current theme.
 */
@Composable
fun threadViolationColors(darkTheme: Boolean = isSystemInDarkTheme()): ThreadViolationColors {
    return if (darkTheme) DarkThreadViolationColors else LightThreadViolationColors
}
