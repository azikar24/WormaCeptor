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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.azikar24.wormaceptor.core.ui.theme.tokens.Palette
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha

private val LightColorScheme = lightColorScheme(
    primary = Palette.Teal600,
    onPrimary = Palette.White,
    primaryContainer = Palette.AccentSubtleLight,
    onPrimaryContainer = Palette.Teal600,
    secondary = Palette.Teal600,
    onSecondary = Palette.White,
    secondaryContainer = Palette.AccentSubtleLight,
    onSecondaryContainer = Palette.Teal600,
    tertiary = Palette.Teal600,
    onTertiary = Palette.White,
    tertiaryContainer = Palette.AccentSubtleLight,
    onTertiaryContainer = Palette.Teal600,
    error = Palette.Red600,
    errorContainer = Palette.Red600.copy(alpha = TokenAlpha.LIGHT),
    onError = Palette.White,
    onErrorContainer = Palette.Red600,
    background = Palette.White,
    onBackground = Palette.Gray990,
    surface = Palette.Gray50,
    onSurface = Palette.Gray990,
    surfaceVariant = Palette.Gray50,
    onSurfaceVariant = Palette.Gray650,
    outline = Palette.Gray400,
    inverseOnSurface = Palette.Gray50,
    inverseSurface = Palette.Gray975,
    inversePrimary = Palette.TealBright,
    surfaceTint = Palette.Teal600,
    outlineVariant = Palette.Gray400,
    scrim = Palette.Black,
    surfaceDim = Palette.Gray150,
    surfaceBright = Palette.White,
    surfaceContainerLowest = Palette.White,
    surfaceContainerLow = Palette.Gray50,
    surfaceContainer = Palette.Gray50,
    surfaceContainerHigh = Palette.Gray150,
    surfaceContainerHighest = Palette.Gray150,
)

private val DarkColorScheme = darkColorScheme(
    primary = Palette.TealBright,
    onPrimary = Palette.Gray990,
    primaryContainer = Palette.AccentSubtleDark,
    onPrimaryContainer = Palette.TealBright,
    secondary = Palette.TealBright,
    onSecondary = Palette.Gray990,
    secondaryContainer = Palette.AccentSubtleDark,
    onSecondaryContainer = Palette.TealBright,
    tertiary = Palette.TealBright,
    onTertiary = Palette.Gray990,
    tertiaryContainer = Palette.AccentSubtleDark,
    onTertiaryContainer = Palette.TealBright,
    error = Palette.Red800,
    errorContainer = Palette.Red600.copy(alpha = TokenAlpha.LIGHT),
    onError = Palette.Gray990,
    onErrorContainer = Palette.Red800,
    background = Palette.Gray990,
    onBackground = Palette.Gray50,
    surface = Palette.Gray975,
    onSurface = Palette.Gray50,
    surfaceVariant = Palette.Gray975,
    onSurfaceVariant = Palette.Gray500,
    outline = Palette.Gray800,
    inverseOnSurface = Palette.Gray975,
    inverseSurface = Palette.Gray50,
    inversePrimary = Palette.Teal600,
    surfaceTint = Palette.TealBright,
    outlineVariant = Palette.Gray800,
    scrim = Palette.Black,
    surfaceDim = Palette.Gray990,
    surfaceBright = Palette.Gray925,
    surfaceContainerLowest = Palette.Gray990,
    surfaceContainerLow = Palette.Gray975,
    surfaceContainer = Palette.Gray975,
    surfaceContainerHigh = Palette.Gray925,
    surfaceContainerHighest = Palette.Gray925,
)

/** Applies the WormaCeptor Material 3 theme with optional dynamic color support. */
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
        typography = WormaCeptorTypography,
        content = content,
    )
}
