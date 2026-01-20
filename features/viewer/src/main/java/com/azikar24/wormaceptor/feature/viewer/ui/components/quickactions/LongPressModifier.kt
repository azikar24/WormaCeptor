/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.quickactions

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Long-press detection threshold in milliseconds.
 */
private const val LONG_PRESS_THRESHOLD_MS = 500L

/**
 * Scale factor when pressing down.
 */
private const val PRESS_SCALE = 0.96f

/**
 * Scale factor during long-press animation.
 */
private const val LONG_PRESS_SCALE = 0.94f

/**
 * Modifier that adds long-press detection with visual feedback.
 *
 * Features:
 * - Scale animation on press
 * - Further scale reduction during long-press hold
 * - Haptic feedback when long-press is triggered
 * - Smooth spring animations for natural feel
 *
 * @param onLongPress Called when long-press is detected
 * @param onClick Called when tap (not long-press) is detected
 * @param enabled Whether the modifier is active
 */
fun Modifier.longPressWithFeedback(
    onLongPress: () -> Unit,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // Animation state
    val scale = remember { Animatable(1f) }
    var isPressing by remember { mutableStateOf(false) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }

    this
        .scale(scale.value)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput

            detectTapGestures(
                onPress = { _ ->
                    isPressing = true

                    // Animate scale down on press
                    scope.launch {
                        scale.animateTo(
                            targetValue = PRESS_SCALE,
                            animationSpec = tween(
                                durationMillis = 100,
                                easing = LinearEasing,
                            ),
                        )
                    }

                    // Start long-press timer
                    longPressJob = scope.launch {
                        delay(LONG_PRESS_THRESHOLD_MS)

                        // Further scale down during long-press
                        scale.animateTo(
                            targetValue = LONG_PRESS_SCALE,
                            animationSpec = tween(
                                durationMillis = 150,
                                easing = LinearEasing,
                            ),
                        )

                        // Trigger haptic feedback
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                        // Execute callback
                        onLongPress()
                    }

                    // Wait for release
                    tryAwaitRelease()

                    // Cancel long-press if released before threshold
                    longPressJob?.cancel()
                    longPressJob = null
                    isPressing = false

                    // Animate back to normal
                    scope.launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh,
                            ),
                        )
                    }
                },
                onTap = {
                    onClick()
                },
            )
        }
}

/**
 * Alternative long-press modifier with progress indicator support.
 *
 * Provides a progress value (0f to 1f) during the long-press hold,
 * useful for showing a visual progress indicator.
 *
 * @param onLongPress Called when long-press completes
 * @param onClick Called when tap is detected
 * @param onProgressChange Called with progress (0f-1f) during long-press
 * @param enabled Whether the modifier is active
 */
fun Modifier.longPressWithProgress(
    onLongPress: () -> Unit,
    onClick: () -> Unit = {},
    onProgressChange: (Float) -> Unit = {},
    enabled: Boolean = true,
): Modifier = composed {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val scale = remember { Animatable(1f) }
    var longPressJob by remember { mutableStateOf<Job?>(null) }

    this
        .scale(scale.value)
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput

            detectTapGestures(
                onPress = { _ ->
                    // Animate scale
                    scope.launch {
                        scale.animateTo(PRESS_SCALE, tween(100))
                    }

                    // Start progress animation
                    longPressJob = scope.launch {
                        val startTime = System.currentTimeMillis()

                        while (true) {
                            val elapsed = System.currentTimeMillis() - startTime
                            val progress = (elapsed.toFloat() / LONG_PRESS_THRESHOLD_MS).coerceIn(0f, 1f)
                            onProgressChange(progress)

                            if (progress >= 1f) {
                                // Long-press completed
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onLongPress()
                                break
                            }

                            delay(16) // ~60fps
                        }
                    }

                    tryAwaitRelease()

                    // Cleanup
                    longPressJob?.cancel()
                    longPressJob = null
                    onProgressChange(0f)

                    scope.launch {
                        scale.animateTo(
                            1f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessHigh,
                            ),
                        )
                    }
                },
                onTap = {
                    onClick()
                },
            )
        }
}

/**
 * State holder for long-press interactions.
 */
class LongPressState {
    var isPressing by mutableStateOf(false)
        internal set
    var progress by mutableStateOf(0f)
        internal set
    var isLongPressTriggered by mutableStateOf(false)
        internal set
}

/**
 * Remember a LongPressState for custom long-press handling.
 */
@Composable
fun rememberLongPressState(): LongPressState = remember { LongPressState() }

/**
 * Modifier that updates a LongPressState for custom visual feedback.
 *
 * @param state The state to update
 * @param onLongPress Called when long-press is detected
 * @param onClick Called when tap is detected
 */
fun Modifier.longPressState(state: LongPressState, onLongPress: () -> Unit, onClick: () -> Unit = {}): Modifier =
    composed {
        val hapticFeedback = LocalHapticFeedback.current
        val view = LocalView.current
        val scope = rememberCoroutineScope()

        var longPressJob by remember { mutableStateOf<Job?>(null) }

        this.pointerInput(Unit) {
            detectTapGestures(
                onPress = { _ ->
                    state.isPressing = true
                    state.isLongPressTriggered = false

                    // Start progress tracking
                    longPressJob = scope.launch {
                        val startTime = System.currentTimeMillis()

                        while (true) {
                            val elapsed = System.currentTimeMillis() - startTime
                            state.progress = (elapsed.toFloat() / LONG_PRESS_THRESHOLD_MS).coerceIn(0f, 1f)

                            if (state.progress >= 1f && !state.isLongPressTriggered) {
                                state.isLongPressTriggered = true
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onLongPress()
                                break
                            }

                            delay(16)
                        }
                    }

                    tryAwaitRelease()

                    // Cleanup
                    longPressJob?.cancel()
                    longPressJob = null
                    state.isPressing = false
                    state.progress = 0f
                },
                onTap = {
                    onClick()
                },
            )
        }
    }

/**
 * Convenience modifier combining scale animation with long-press state.
 */
@Composable
fun Modifier.animatedLongPress(state: LongPressState, onLongPress: () -> Unit, onClick: () -> Unit = {}): Modifier {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(state.isPressing, state.progress) {
        val targetScale = when {
            state.isLongPressTriggered -> LONG_PRESS_SCALE
            state.isPressing -> PRESS_SCALE - (state.progress * 0.02f)
            else -> 1f
        }

        scale.animateTo(
            targetScale,
            if (state.isPressing) {
                tween(50)
            } else {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh,
                )
            },
        )
    }

    return this
        .scale(scale.value)
        .longPressState(state, onLongPress, onClick)
}
