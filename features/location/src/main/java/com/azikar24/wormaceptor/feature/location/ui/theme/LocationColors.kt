package com.azikar24.wormaceptor.feature.location.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Location feature colors - delegates to centralized WormaCeptorColors.Location.
 */
object LocationColors {
    /** Color indicating mock location is enabled. */
    val enabled: Color = WormaCeptorColors.Location.Enabled

    /** Color indicating mock location is disabled. */
    val disabled: Color = WormaCeptorColors.Location.Disabled

    /** Color for warning states such as missing permissions. */
    val warning: Color = WormaCeptorColors.Location.Warning

    /** Color for error states such as provider failures. */
    val error: Color = WormaCeptorColors.Location.Error

    /** Color for built-in location presets. */
    val builtIn: Color = WormaCeptorColors.Location.BuiltInPreset

    /** Color for user-created location presets. */
    val userPreset: Color = WormaCeptorColors.Location.UserPreset

    /** Color for coordinate text display. */
    val coordinate: Color = WormaCeptorColors.Location.Coordinate
}
