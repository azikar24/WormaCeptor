package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Custom text styles for the WormaCeptor design system.
 * Relocated from [WormaCeptorDesignSystem.Typography].
 */
@Suppress("MagicNumber")
object TokenTypography {

    /** Body - Main content text. */
    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp,
    )

    /** Label - UI labels and captions. */
    val labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )

    /** Small label for compact UI elements. */
    val labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )

    /** Code - Monospace for code display. */
    val codeMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
    )

    /** Small monospace text style for compact code display. */
    val codeSmall = TextStyle(
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
    )

    /** Section header - titled sections within containers. */
    val sectionHeader = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    )

    /** Overline - Category labels. */
    val overline = TextStyle(
        fontSize = 10.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
    )

    /** Display number - Large monospace hero values (FPS counter, gauges). */
    val displayNumber = TextStyle(
        fontSize = 72.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )

    /** Overline wide - Section label with wide letter spacing. */
    val overlineWide = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )

    /** Overlay label - Small text for performance overlay pill. */
    val overlayLabel = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )

    /** Overlay value - Metric value for performance overlay pill. */
    val overlayValue = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )
}
