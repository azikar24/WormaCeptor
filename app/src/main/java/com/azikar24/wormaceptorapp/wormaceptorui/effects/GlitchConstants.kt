package com.azikar24.wormaceptorapp.wormaceptorui.effects

/**
 * Constants for the glitch effect shader and fallback animations.
 */
internal object GlitchConstants {

    /**
     * Animation duration constants (milliseconds).
     */
    object Animation {
        /** Duration of the main glitch time animation loop. */
        const val GLITCH_TIME_DURATION_MS = 10_000

        /** Target value for the time animation. */
        const val GLITCH_TIME_TARGET = 100f

        /** Duration for shake offset animation. */
        const val SHAKE_DURATION_MS = 50

        /** Duration for rotation wobble animation. */
        const val ROTATION_DURATION_MS = 100

        /** Duration for noise key animation. */
        const val NOISE_KEY_DURATION_MS = 1500

        /** Target value for noise key animation. */
        const val NOISE_KEY_TARGET = 1000f
    }

    /**
     * Intensity multipliers for visual effects.
     */
    object Intensity {
        /** Maximum shake offset in dp at full progress. */
        const val MAX_SHAKE_DP = 20f

        /** Maximum rotation angle in degrees at full progress. */
        const val MAX_ROTATION_DEGREES = 3f

        /** Scale variation factor (1 +/- this value * progress). */
        const val SCALE_VARIATION = 0.05f

        /** Y-axis shake is reduced by this factor relative to X. */
        const val Y_SHAKE_FACTOR = 0.7f
    }

    /**
     * Overlay and effect alpha values.
     */
    object Alpha {
        /** Maximum alpha for dark overlay at full progress. */
        const val MAX_DARK_OVERLAY = 0.6f

        /** Progress threshold for flash effect to begin. */
        const val FLASH_THRESHOLD = 0.9f

        /** Progress range over which flash ramps up. */
        const val FLASH_RANGE = 0.1f
    }

    /**
     * Scanline effect constants.
     */
    object Scanlines {
        /** Number of horizontal scan lines. */
        const val COUNT = 100

        /** Progress threshold for scanlines to appear. */
        const val PROGRESS_THRESHOLD = 0.2f

        /** Progress range over which scanlines fade in. */
        const val FADE_IN_RANGE = 0.8f

        /** Maximum scanline alpha. */
        const val MAX_ALPHA = 0.3f
    }

    /**
     * Noise effect constants.
     */
    object Noise {
        /** Progress threshold for noise to appear. */
        const val PROGRESS_THRESHOLD = 0.4f

        /** Progress range over which noise fades in. */
        const val FADE_IN_RANGE = 0.6f

        /** Maximum noise alpha. */
        const val MAX_ALPHA = 0.4f

        /** Size of noise pixels in dp. */
        const val PIXEL_SIZE = 4f

        /** Density of noise pixels (percentage of screen covered). */
        const val DENSITY = 0.02f
    }

    /**
     * Progress thresholds for effect stages.
     */
    object ProgressThreshold {
        /** Progress at which scanlines appear. */
        const val SCANLINES = 0.2f

        /** Progress at which dark overlay appears. */
        const val DARK_OVERLAY = 0.3f

        /** Progress at which noise appears. */
        const val NOISE = 0.4f

        /** Progress at which flash begins. */
        const val FLASH = 0.9f
    }
}
