package com.azikar24.wormaceptor.platform.android

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Constants for the floating button service UI and behavior.
 * Uses WormaCeptorDesignSystem tokens where applicable.
 */
internal object FloatingButtonConstants {

    /**
     * Dimension constants for the floating button.
     */
    object Dimensions {
        /** Size of the floating button (matches comfortable touch target). */
        val BUTTON_SIZE = WormaCeptorDesignSystem.TouchTarget.large

        /** Size of the icon inside the button. */
        val ICON_SIZE = WormaCeptorDesignSystem.IconSize.lg

        /** Shadow elevation for the button. */
        val SHADOW_ELEVATION = WormaCeptorDesignSystem.Elevation.xl
    }

    /**
     * Animation duration constants.
     */
    object Animation {
        /** Duration for snap-to-edge animation. */
        val SNAP_DURATION_MS = WormaCeptorDesignSystem.AnimationDuration.normal.toLong()

        /** Duration for press scale animation. */
        val SCALE_DURATION_MS = WormaCeptorDesignSystem.AnimationDuration.ultraFast
    }

    /**
     * Visual effect constants.
     */
    object Visual {
        /** Primary button background color (uses design system accent). */
        val BUTTON_COLOR: Color = WormaCeptorDesignSystem.ThemeColors.AccentLight

        /** Icon tint color. */
        val ICON_TINT: Color = Color.White

        /** Icon alpha/opacity. */
        val ICON_ALPHA = WormaCeptorDesignSystem.Alpha.prominent

        /** Scale factor when button is pressed. */
        const val PRESSED_SCALE = 0.9f

        /** Normal scale factor. */
        const val NORMAL_SCALE = 1f
    }
}
