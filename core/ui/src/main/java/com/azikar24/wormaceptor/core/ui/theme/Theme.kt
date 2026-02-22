package com.azikar24.wormaceptor.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem.ThemeColors

private val LightColorScheme = lightColorScheme(
    primary = ThemeColors.AccentLight,
    onPrimary = ThemeColors.LightBackground,
    primaryContainer = ThemeColors.AccentSubtleLight,
    onPrimaryContainer = ThemeColors.AccentLight,
    secondary = ThemeColors.AccentLight,
    onSecondary = ThemeColors.LightBackground,
    secondaryContainer = ThemeColors.AccentSubtleLight,
    onSecondaryContainer = ThemeColors.AccentLight,
    tertiary = ThemeColors.AccentLight,
    onTertiary = ThemeColors.LightBackground,
    tertiaryContainer = ThemeColors.AccentSubtleLight,
    onTertiaryContainer = ThemeColors.AccentLight,
    error = ThemeColors.Error,
    errorContainer = ThemeColors.Error.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
    onError = ThemeColors.LightBackground,
    onErrorContainer = ThemeColors.Error,
    background = ThemeColors.LightBackground,
    onBackground = ThemeColors.LightTextPrimary,
    surface = ThemeColors.LightSurface,
    onSurface = ThemeColors.LightTextPrimary,
    surfaceVariant = ThemeColors.LightSurface,
    onSurfaceVariant = ThemeColors.LightTextSecondary,
    outline = ThemeColors.LightTextTertiary,
    inverseOnSurface = ThemeColors.LightSurface,
    inverseSurface = ThemeColors.DarkSurface,
    inversePrimary = ThemeColors.AccentDark,
    surfaceTint = ThemeColors.AccentLight,
    outlineVariant = ThemeColors.LightTextTertiary,
    scrim = Color(0xFF000000),
    surfaceDim = ThemeColors.LightSurfaceVariant,
    surfaceBright = ThemeColors.LightBackground,
    surfaceContainerLowest = ThemeColors.LightBackground,
    surfaceContainerLow = ThemeColors.LightSurface,
    surfaceContainer = ThemeColors.LightSurface,
    surfaceContainerHigh = ThemeColors.LightSurfaceVariant,
    surfaceContainerHighest = ThemeColors.LightSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = ThemeColors.AccentDark,
    onPrimary = ThemeColors.DarkBackground,
    primaryContainer = ThemeColors.AccentSubtleDark,
    onPrimaryContainer = ThemeColors.AccentDark,
    secondary = ThemeColors.AccentDark,
    onSecondary = ThemeColors.DarkBackground,
    secondaryContainer = ThemeColors.AccentSubtleDark,
    onSecondaryContainer = ThemeColors.AccentDark,
    tertiary = ThemeColors.AccentDark,
    onTertiary = ThemeColors.DarkBackground,
    tertiaryContainer = ThemeColors.AccentSubtleDark,
    onTertiaryContainer = ThemeColors.AccentDark,
    error = ThemeColors.ErrorDark,
    errorContainer = ThemeColors.Error.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
    onError = ThemeColors.DarkBackground,
    onErrorContainer = ThemeColors.ErrorDark,
    background = ThemeColors.DarkBackground,
    onBackground = ThemeColors.DarkTextPrimary,
    surface = ThemeColors.DarkSurface,
    onSurface = ThemeColors.DarkTextPrimary,
    surfaceVariant = ThemeColors.DarkSurface,
    onSurfaceVariant = ThemeColors.DarkTextSecondary,
    outline = ThemeColors.DarkTextTertiary,
    inverseOnSurface = ThemeColors.DarkSurface,
    inverseSurface = ThemeColors.LightSurface,
    inversePrimary = ThemeColors.AccentLight,
    surfaceTint = ThemeColors.AccentDark,
    outlineVariant = ThemeColors.DarkTextTertiary,
    scrim = Color(0xFF000000),
    surfaceDim = ThemeColors.DarkBackground,
    surfaceBright = ThemeColors.DarkSurfaceVariant,
    surfaceContainerLowest = ThemeColors.DarkBackground,
    surfaceContainerLow = ThemeColors.DarkSurface,
    surfaceContainer = ThemeColors.DarkSurface,
    surfaceContainerHigh = ThemeColors.DarkSurfaceVariant,
    surfaceContainerHighest = ThemeColors.DarkSurfaceVariant,
)

@Composable
fun WormaCeptorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.decorView.setBackgroundColor(colorScheme.background.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
