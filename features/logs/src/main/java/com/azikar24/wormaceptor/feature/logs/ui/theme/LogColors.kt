package com.azikar24.wormaceptor.feature.logs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.LogLevel

/**
 * Log level colors for consistent visual representation across the UI.
 *
 * Color scheme inspired by standard log level conventions:
 * - VERBOSE: Gray (least important, subtle)
 * - DEBUG: Blue (informational debugging)
 * - INFO: Green (successful operations, normal flow)
 * - WARN: Yellow/Amber (potential issues, attention needed)
 * - ERROR: Red (errors, failures)
 * - ASSERT: Deep Red/Magenta (critical assertions)
 */
@Immutable
data class LogLevelColors(
    val verbose: Color,
    val debug: Color,
    val info: Color,
    val warn: Color,
    val error: Color,
    val assert: Color,
    // Background tints for log entries
    val verboseBackground: Color,
    val debugBackground: Color,
    val infoBackground: Color,
    val warnBackground: Color,
    val errorBackground: Color,
    val assertBackground: Color,
) {
    /**
     * Returns the foreground color for the given log level.
     */
    fun forLevel(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> verbose
        LogLevel.DEBUG -> debug
        LogLevel.INFO -> info
        LogLevel.WARN -> warn
        LogLevel.ERROR -> error
        LogLevel.ASSERT -> assert
    }

    /**
     * Returns the background color for the given log level.
     */
    fun backgroundForLevel(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> verboseBackground
        LogLevel.DEBUG -> debugBackground
        LogLevel.INFO -> infoBackground
        LogLevel.WARN -> warnBackground
        LogLevel.ERROR -> errorBackground
        LogLevel.ASSERT -> assertBackground
    }
}

/**
 * Light theme log level colors.
 * Uses saturated colors for good visibility on light backgrounds.
 */
val LightLogLevelColors = LogLevelColors(
    verbose = Color(0xFF757575), // Gray 600
    debug = Color(0xFF1976D2), // Blue 700
    info = Color(0xFF388E3C), // Green 700
    warn = Color(0xFFF57C00), // Orange 700
    error = Color(0xFFD32F2F), // Red 700
    assert = Color(0xFFC2185B), // Pink 700
    // Subtle backgrounds
    verboseBackground = Color(0xFFF5F5F5), // Gray 100
    debugBackground = Color(0xFFE3F2FD), // Blue 50
    infoBackground = Color(0xFFE8F5E9), // Green 50
    warnBackground = Color(0xFFFFF3E0), // Orange 50
    errorBackground = Color(0xFFFFEBEE), // Red 50
    assertBackground = Color(0xFFFCE4EC), // Pink 50
)

/**
 * Dark theme log level colors.
 * Uses slightly desaturated colors for comfortable viewing on dark backgrounds.
 */
val DarkLogLevelColors = LogLevelColors(
    verbose = Color(0xFF9E9E9E), // Gray 500
    debug = Color(0xFF64B5F6), // Blue 300
    info = Color(0xFF81C784), // Green 300
    warn = Color(0xFFFFB74D), // Orange 300
    error = Color(0xFFE57373), // Red 300
    assert = Color(0xFFF06292), // Pink 300
    // Subtle backgrounds (very dark tints)
    verboseBackground = Color(0xFF212121), // Gray 900
    debugBackground = Color(0xFF0D47A1).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Blue with alpha
    infoBackground = Color(0xFF1B5E20).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Green with alpha
    warnBackground = Color(0xFFE65100).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Orange with alpha
    errorBackground = Color(0xFFB71C1C).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Red with alpha
    assertBackground = Color(0xFF880E4F).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Pink with alpha
)

/**
 * Composition local for log level colors.
 */
val LocalLogLevelColors = staticCompositionLocalOf { LightLogLevelColors }

/**
 * Returns the appropriate log level colors based on the current theme.
 */
@Composable
fun logLevelColors(darkTheme: Boolean = isSystemInDarkTheme()): LogLevelColors {
    return if (darkTheme) DarkLogLevelColors else LightLogLevelColors
}
