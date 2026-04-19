package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset

internal object FullscreenImageDefaults {
    const val MIN_ZOOM = 0.5f
    const val MAX_ZOOM = 5f
    const val DOUBLE_TAP_ZOOM = 2.5f
    const val ZOOM_STEP = 1.5f
    const val ZOOM_THRESHOLD = 1.1f
    const val DISMISS_THRESHOLD = 200f
    const val BACKGROUND_ALPHA = 0.95f

    val ZoomSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    val OffsetSpring = spring<Offset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
