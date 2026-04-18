package com.azikar24.wormaceptor.feature.logs.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LogLevel

@Composable
internal fun logLevelColor(level: LogLevel): Color {
    val logColors = WormaCeptorTokens.Colors.LogLevel
    return when (level) {
        LogLevel.VERBOSE -> logColors.verbose
        LogLevel.DEBUG -> logColors.debug
        LogLevel.INFO -> logColors.info
        LogLevel.WARN -> logColors.warn
        LogLevel.ERROR -> logColors.error
        LogLevel.ASSERT -> logColors.assert
    }
}

@Composable
internal fun logLevelBackground(
    level: LogLevel,
    levelColor: Color,
): Color = if (level == LogLevel.VERBOSE) {
    WormaCeptorTokens.semantic().surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
} else {
    levelColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
}
