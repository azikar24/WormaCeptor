/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.theme

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkAccentSubtle,
    onPrimaryContainer = DarkAccent,
    secondary = DarkAccent,
    onSecondary = DarkBackground,
    secondaryContainer = DarkAccentSubtle,
    onSecondaryContainer = DarkAccent,
    tertiary = DarkAccent,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkAccentSubtle,
    onTertiaryContainer = DarkAccent,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkTextTertiary,
    outlineVariant = DarkTextTertiary,
    error = DarkDestructive,
    onError = DarkBackground,
    errorContainer = DarkDestructive.copy(alpha = 0.12f),
    onErrorContainer = DarkDestructive,
)

private val LightColorScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightBackground,
    primaryContainer = LightAccentSubtle,
    onPrimaryContainer = LightAccent,
    secondary = LightAccent,
    onSecondary = LightBackground,
    secondaryContainer = LightAccentSubtle,
    onSecondaryContainer = LightAccent,
    tertiary = LightAccent,
    onTertiary = LightBackground,
    tertiaryContainer = LightAccentSubtle,
    onTertiaryContainer = LightAccent,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightTextTertiary,
    outlineVariant = LightTextTertiary,
    error = LightDestructive,
    onError = LightBackground,
    errorContainer = LightDestructive.copy(alpha = 0.12f),
    onErrorContainer = LightDestructive,
)

@Composable
fun WormaCeptorMainTheme(
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
