package com.azikar24.wormaceptor.platform.android

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Constants for the floating button service UI and behavior.
 * Uses WormaCeptorTokens where applicable.
 */
internal object FloatingButtonConstants {

    /**
     * Dimension constants for the floating button.
     */
    object Dimensions {
        /** Size of the floating button (matches comfortable touch target). */
        val BUTTON_SIZE = WormaCeptorTokens.TouchTarget.large

        /** Size of the icon inside the button. */
        val ICON_SIZE = WormaCeptorTokens.IconSize.lg

        /** Shadow elevation for the button. */
        val SHADOW_ELEVATION = WormaCeptorTokens.Elevation.xl
    }

    /**
     * Animation duration constants.
     */
    object Animation {
        /** Duration for snap-to-edge animation. */
        val SNAP_DURATION_MS = WormaCeptorTokens.Animation.NORMAL.toLong()

        /** Duration for press scale animation. */
        val SCALE_DURATION_MS = WormaCeptorTokens.Animation.ULTRA_FAST
    }

    /**
     * Visual effect constants.
     */
    object Visual {
        /** Primary button background color (teal accent). */
        val BUTTON_COLOR: Color = Color(0xFF0D9488)

        /** Icon tint color. */
        val ICON_TINT: Color = Color(0xFFFFFFFF)

        /** Icon alpha/opacity. */
        val ICON_ALPHA = WormaCeptorTokens.Alpha.PROMINENT

        /** Scale factor when button is pressed. */
        const val PRESSED_SCALE = 0.9f

        /** Normal scale factor. */
        const val NORMAL_SCALE = 1f
    }
}
