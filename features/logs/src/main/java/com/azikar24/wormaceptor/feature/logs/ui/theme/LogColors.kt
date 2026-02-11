package com.azikar24.wormaceptor.feature.logs.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
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
 * - ASSERT: Deep Red/Magenta (critical assertions, feature-specific)
 */
@Immutable
data class LogLevelColors(
    val verbose: Color,
    val debug: Color,
    val info: Color,
    val warn: Color,
    val error: Color,
    val assert: Color,
    val verboseBackground: Color,
    val debugBackground: Color,
    val infoBackground: Color,
    val warnBackground: Color,
    val errorBackground: Color,
    val assertBackground: Color,
) {
    fun forLevel(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> verbose
        LogLevel.DEBUG -> debug
        LogLevel.INFO -> info
        LogLevel.WARN -> warn
        LogLevel.ERROR -> error
        LogLevel.ASSERT -> assert
    }

    fun backgroundForLevel(level: LogLevel): Color = when (level) {
        LogLevel.VERBOSE -> verboseBackground
        LogLevel.DEBUG -> debugBackground
        LogLevel.INFO -> infoBackground
        LogLevel.WARN -> warnBackground
        LogLevel.ERROR -> errorBackground
        LogLevel.ASSERT -> assertBackground
    }
}

private val AssertColor = Color(0xFFC2185B)

/**
 * Returns the appropriate log level colors based on the current theme.
 * Delegates to WormaCeptorColors for status semantics and Material theme for surfaces.
 */
@Composable
fun logLevelColors(darkTheme: Boolean = isSystemInDarkTheme()): LogLevelColors {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    return LogLevelColors(
        verbose = WormaCeptorColors.StatusGrey,
        debug = WormaCeptorColors.StatusBlue,
        info = WormaCeptorColors.StatusGreen,
        warn = WormaCeptorColors.StatusAmber,
        error = WormaCeptorColors.StatusRed,
        assert = AssertColor,
        verboseBackground = surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        debugBackground = WormaCeptorColors.StatusBlue.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        infoBackground = WormaCeptorColors.StatusGreen.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        warnBackground = WormaCeptorColors.StatusAmber.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        errorBackground = WormaCeptorColors.StatusRed.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        assertBackground = AssertColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
    )
}

val LocalLogLevelColors = staticCompositionLocalOf {
    LogLevelColors(
        verbose = WormaCeptorColors.StatusGrey,
        debug = WormaCeptorColors.StatusBlue,
        info = WormaCeptorColors.StatusGreen,
        warn = WormaCeptorColors.StatusAmber,
        error = WormaCeptorColors.StatusRed,
        assert = AssertColor,
        verboseBackground = Color.Transparent,
        debugBackground = Color.Transparent,
        infoBackground = Color.Transparent,
        warnBackground = Color.Transparent,
        errorBackground = Color.Transparent,
        assertBackground = Color.Transparent,
    )
}
