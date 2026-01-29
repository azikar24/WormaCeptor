/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.location.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Location feature colors - delegates to centralized WormaCeptorColors.Location.
 */
object LocationColors {
    val enabled: Color = WormaCeptorColors.Location.Enabled
    val disabled: Color = WormaCeptorColors.Location.Disabled
    val warning: Color = WormaCeptorColors.Location.Warning
    val error: Color = WormaCeptorColors.Location.Error
    val builtIn: Color = WormaCeptorColors.Location.BuiltInPreset
    val userPreset: Color = WormaCeptorColors.Location.UserPreset
    val coordinate: Color = WormaCeptorColors.Location.Coordinate
}
