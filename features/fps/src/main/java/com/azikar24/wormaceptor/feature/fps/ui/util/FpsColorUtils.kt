package com.azikar24.wormaceptor.feature.fps.ui.util

import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

internal fun fpsColorForValue(fps: Float) = when {
    fps >= 55f -> WormaCeptorTokens.Colors.Fps.good
    fps >= 30f -> WormaCeptorTokens.Colors.Fps.warning
    else -> WormaCeptorTokens.Colors.Fps.critical
}
