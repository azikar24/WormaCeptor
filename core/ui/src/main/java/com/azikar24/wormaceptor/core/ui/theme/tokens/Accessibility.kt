package com.azikar24.wormaceptor.core.ui.theme.tokens

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

/**
 * Returns `true` when the host device has animations disabled via
 * Developer Options "Animator duration scale = off" or
 * Accessibility "Remove animations". Components that run infinite or
 * decorative animations should suppress them while this is true and
 * fall back to a static end-state.
 *
 * Read once at composition; does not react to live setting changes.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    }
}
