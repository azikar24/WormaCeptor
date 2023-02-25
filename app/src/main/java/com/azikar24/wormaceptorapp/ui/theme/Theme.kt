/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable


private val DarkColorPalette = darkColors(
    primary = brandPrimaryColor,
    onPrimary = whiteColor,
    primaryVariant = brandVariantColor,
    secondary = brandVariantColor
)

private val LightColorPalette = lightColors(
    primary = brandPrimaryColor,
    onPrimary = whiteColor,
    primaryVariant = brandVariantColor,
    secondary = brandVariantColor

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun WormaCeptorMainTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}