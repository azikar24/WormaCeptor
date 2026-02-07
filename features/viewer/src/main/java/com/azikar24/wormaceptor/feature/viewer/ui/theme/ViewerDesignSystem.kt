package com.azikar24.wormaceptor.feature.viewer.ui.theme

import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Viewer-specific design system extensions.
 *
 * Core design tokens (Spacing, CornerRadius, BorderWidth, Alpha, Shapes, AnimationDuration,
 * Elevation, AnimationSpecs, CardPadding) are provided by the shared core:ui module via
 * [WormaCeptorDesignSystem].
 *
 * Extension functions (asSubtleBackground, asLightBackground) are also available from
 * [com.azikar24.wormaceptor.core.ui.theme].
 */
object ViewerDesignSystem {
    /**
     * Type alias for accessing core design system from viewer module.
     */
    val Spacing = WormaCeptorDesignSystem.Spacing
    val CornerRadius = WormaCeptorDesignSystem.CornerRadius
    val Alpha = WormaCeptorDesignSystem.Alpha
}
