package com.azikar24.wormaceptor.core.ui.components.toggle

import androidx.compose.ui.graphics.Color

/** Visual style for [WormaCeptorSwitch]. */
sealed interface SwitchVariant {
    /** Default M3 switch colors. */
    data object Standard : SwitchVariant

    /** Checked track uses [color]; thumb uses `onPrimary` for contrast. */
    data class Accent(val color: Color) : SwitchVariant
}
