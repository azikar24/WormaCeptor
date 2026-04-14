package com.azikar24.wormaceptor.core.ui.util

import androidx.compose.ui.graphics.Color

// Relative luminance weights from ITU-R BT.601 (perceived brightness).
private const val LuminanceWeightRed = 0.299
private const val LuminanceWeightGreen = 0.587
private const val LuminanceWeightBlue = 0.114
private const val LuminanceThreshold = 0.5

/**
 * Returns black or white depending on which has better perceived contrast
 * against [background]. Uses the ITU-R BT.601 relative-luminance formula --
 * fast, dependency-free, and good enough for solid-fill UI surfaces like
 * badges, chips, and status pills.
 *
 * NOT WCAG-compliant and should not be used for body text contrast
 * calculations where AA/AAA compliance matters.
 */
fun contentColorFor(background: Color): Color {
    val luminance = LuminanceWeightRed * background.red +
        LuminanceWeightGreen * background.green +
        LuminanceWeightBlue * background.blue
    return if (luminance > LuminanceThreshold) Color.Black else Color.White
}
