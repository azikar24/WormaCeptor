@file:Suppress("UndocumentedPublicProperty")

package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** OpenType feature tag for tabular figures -- fixed-width digits for table alignment. */
private const val TabularFigures = "tnum"

/**
 * Debug-tool-specific text styles that extend M3's Typography: monospace
 * code styles, compact badge labels, and big hero numeric styles for live
 * gauges. All numeric/code styles enable OpenType `tnum` so digits stay
 * aligned across rows when values tick live.
 *
 * Prefer [androidx.compose.material3.MaterialTheme.typography] for generic
 * body/headline/title/label text -- reach here only for the specialized
 * roles below.
 */
@Suppress("MagicNumber")
object TokenTypography {

    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.25.sp,
    )

    val labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )

    val labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )

    val codeMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = TabularFigures,
    )

    val codeSmall = TextStyle(
        fontSize = 10.sp,
        lineHeight = 14.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontFeatureSettings = TabularFigures,
    )

    val sectionHeader = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    )

    val overline = TextStyle(
        fontSize = 10.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
    )

    /** Hero monospace value for big live gauges -- FPS counter, memory readouts. */
    val displayNumber = TextStyle(
        fontSize = 72.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        fontFeatureSettings = TabularFigures,
    )

    /** Sans-serif numeric style for inline metrics where a mono face feels heavy. */
    val metricValue = TextStyle(
        fontSize = 20.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = TabularFigures,
    )

    val overlineWide = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
    )

    /** Label text inside the floating performance overlay pill. */
    val overlayLabel = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )

    /** Monospace metric value inside the floating performance overlay pill. */
    val overlayValue = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        fontFeatureSettings = TabularFigures,
    )
}
