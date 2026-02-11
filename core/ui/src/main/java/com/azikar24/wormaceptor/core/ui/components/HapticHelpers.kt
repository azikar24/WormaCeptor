package com.azikar24.wormaceptor.core.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView

/**
 * Remembers a haptic feedback trigger that fires only once per pull-to-refresh gesture.
 * Resets when [resetKey] changes to false.
 *
 * Usage:
 * ```
 * val (hasTriggered, triggerHaptic, resetHaptic) = rememberHapticOnce()
 * LaunchedEffect(pullToRefreshState.distanceFraction) {
 *     if (pullToRefreshState.distanceFraction >= 1f && !hasTriggered) {
 *         triggerHaptic()
 *     } else if (pullToRefreshState.distanceFraction < 1f) {
 *         resetHaptic()
 *     }
 * }
 * ```
 */
@Composable
fun rememberHapticOnce(): HapticOnceState {
    val view = LocalView.current
    var hasTriggered by remember { mutableStateOf(false) }
    return remember(view) {
        HapticOnceState(
            hasTriggered = { hasTriggered },
            trigger = {
                if (!hasTriggered) {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    hasTriggered = true
                }
            },
            reset = { hasTriggered = false },
        )
    }
}

class HapticOnceState(
    private val hasTriggered: () -> Boolean,
    private val trigger: () -> Unit,
    private val reset: () -> Unit,
) {
    val isTriggered: Boolean get() = hasTriggered()

    fun triggerHaptic() = trigger()

    fun resetHaptic() = reset()

    operator fun component1(): Boolean = isTriggered
    operator fun component2(): () -> Unit = trigger
    operator fun component3(): () -> Unit = reset
}
