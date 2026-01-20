/*
 * Copyright AziKar24 2025.
 * Gesture Utilities for WormaCeptor
 *
 * Provides helper functions and extensions for gesture handling:
 * - Haptic feedback helpers
 * - Gesture detection utilities
 * - Animation helpers
 * - State management
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =============================================================================
// HAPTIC FEEDBACK HELPERS
// =============================================================================

/**
 * Types of haptic feedback for gesture interactions.
 */
enum class GestureHapticType {
    /** Light tap feedback for minor interactions */
    LIGHT_TAP,

    /** Medium feedback for threshold crossings */
    THRESHOLD_CROSSED,

    /** Strong feedback for significant actions */
    ACTION_CONFIRMED,

    /** Error/rejection feedback */
    REJECTION,

    /** Page change feedback */
    PAGE_TURN,

    /** Zoom level change */
    ZOOM_CHANGE,
}

/**
 * Provides haptic feedback for gesture interactions.
 * Uses the most appropriate feedback type based on Android version.
 */
@Composable
fun rememberGestureHaptics(): GestureHaptics {
    val view = LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    return remember(view, hapticFeedback) {
        GestureHaptics(view, hapticFeedback)
    }
}

/**
 * Wrapper class for haptic feedback with gesture-specific methods.
 */
class GestureHaptics(
    private val view: View,
    private val hapticFeedback: HapticFeedback,
) {
    /**
     * Performs haptic feedback based on the gesture type.
     */
    fun perform(type: GestureHapticType) {
        when (type) {
            GestureHapticType.LIGHT_TAP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
            GestureHapticType.THRESHOLD_CROSSED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            GestureHapticType.ACTION_CONFIRMED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            GestureHapticType.REJECTION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
            GestureHapticType.PAGE_TURN -> {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
            GestureHapticType.ZOOM_CHANGE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }

    /**
     * Performs haptic feedback only if enough time has passed since last feedback.
     * Prevents excessive haptic feedback during continuous gestures.
     */
    private var lastHapticTime = 0L
    private val hapticCooldown = 50L // ms

    fun performThrottled(type: GestureHapticType) {
        val now = System.currentTimeMillis()
        if (now - lastHapticTime >= hapticCooldown) {
            perform(type)
            lastHapticTime = now
        }
    }
}

// =============================================================================
// GESTURE STATE HELPERS
// =============================================================================

/**
 * Represents the state of a drag gesture.
 */
data class DragState(
    val isDragging: Boolean = false,
    val offset: Offset = Offset.Zero,
    val velocity: Offset = Offset.Zero,
    val startPosition: Offset = Offset.Zero,
) {
    val totalDistance: Float
        get() = offset.getDistance()

    val horizontalProgress: Float
        get() = if (startPosition.x != 0f) offset.x / startPosition.x else 0f

    val verticalProgress: Float
        get() = if (startPosition.y != 0f) offset.y / startPosition.y else 0f
}

/**
 * Remembers and manages drag state with animated reset.
 */
@Composable
fun rememberAnimatedDragState(initialOffset: Offset = Offset.Zero): AnimatedDragState {
    val animatedX = remember { Animatable(initialOffset.x) }
    val animatedY = remember { Animatable(initialOffset.y) }

    return remember(animatedX, animatedY) {
        AnimatedDragState(animatedX, animatedY)
    }
}

class AnimatedDragState(
    private val animatedX: Animatable<Float, AnimationVector1D>,
    private val animatedY: Animatable<Float, AnimationVector1D>,
) {
    val offset: Offset
        get() = Offset(animatedX.value, animatedY.value)

    suspend fun snapTo(offset: Offset) {
        animatedX.snapTo(offset.x)
        animatedY.snapTo(offset.y)
    }

    suspend fun animateTo(
        offset: Offset,
        animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
    ) {
        coroutineScope {
            launch {
                animatedX.animateTo(offset.x, animationSpec)
            }
            launch {
                animatedY.animateTo(offset.y, animationSpec)
            }
        }
    }

    suspend fun reset() {
        animateTo(Offset.Zero)
    }
}

// =============================================================================
// GESTURE DETECTION UTILITIES
// =============================================================================

/**
 * Determines the dominant direction of a drag gesture.
 */
enum class DragDirection {
    NONE,
    LEFT,
    RIGHT,
    UP,
    DOWN,
}

fun Offset.dominantDirection(): DragDirection {
    return when {
        x == 0f && y == 0f -> DragDirection.NONE
        kotlin.math.abs(x) > kotlin.math.abs(y) -> {
            if (x > 0) DragDirection.RIGHT else DragDirection.LEFT
        }
        else -> {
            if (y > 0) DragDirection.DOWN else DragDirection.UP
        }
    }
}

/**
 * Checks if the gesture is primarily horizontal.
 */
fun Offset.isHorizontal(threshold: Float = 1.5f): Boolean {
    if (y == 0f) return true
    return kotlin.math.abs(x / y) > threshold
}

/**
 * Checks if the gesture is primarily vertical.
 */
fun Offset.isVertical(threshold: Float = 1.5f): Boolean {
    if (x == 0f) return true
    return kotlin.math.abs(y / x) > threshold
}

// =============================================================================
// THRESHOLD HELPERS
// =============================================================================

/**
 * Represents configurable thresholds for gesture actions.
 */
data class GestureThresholds(
    val swipeBack: Float = 100f,
    val pullToRefresh: Float = 120f,
    val swipeNavigation: Float = 0.3f, // As fraction of screen width
    val zoomDoubleTap: Float = 2f,
    val zoomMin: Float = 0.5f,
    val zoomMax: Float = 3f,
)

/**
 * Default thresholds used throughout the app.
 */
val DefaultGestureThresholds = GestureThresholds()

/**
 * Checks if a value has crossed a threshold and triggers a callback.
 */
@Composable
fun ThresholdCrossing(value: Float, threshold: Float, onCrossUp: () -> Unit = {}, onCrossDown: () -> Unit = {}) {
    var wasAboveThreshold by remember { mutableStateOf(value >= threshold) }

    LaunchedEffect(value) {
        val isAboveThreshold = value >= threshold
        if (isAboveThreshold != wasAboveThreshold) {
            if (isAboveThreshold) {
                onCrossUp()
            } else {
                onCrossDown()
            }
            wasAboveThreshold = isAboveThreshold
        }
    }
}

// =============================================================================
// MODIFIER EXTENSIONS
// =============================================================================

/**
 * Modifier that provides haptic feedback when a condition changes.
 */
fun Modifier.hapticOnChange(
    value: Boolean,
    hapticType: GestureHapticType = GestureHapticType.THRESHOLD_CROSSED,
): Modifier = composed {
    val haptics = rememberGestureHaptics()
    var previousValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != previousValue) {
            haptics.perform(hapticType)
            previousValue = value
        }
    }

    this
}

/**
 * Modifier that adds a brief delay before showing content.
 * Useful for hint UI that shouldn't appear on quick gestures.
 */
fun Modifier.delayedVisibility(visible: Boolean, delayMs: Long = 200): Modifier = composed {
    var delayedVisible by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs)
            delayedVisible = true
        } else {
            delayedVisible = false
        }
    }

    if (delayedVisible) this else Modifier
}

// =============================================================================
// ANIMATION UTILITIES
// =============================================================================

/**
 * Calculates a decay position based on velocity.
 * Useful for fling animations.
 */
fun calculateDecayTarget(currentPosition: Float, velocity: Float, friction: Float = 0.8f): Float {
    val deceleration = friction * -1
    val duration = velocity / deceleration
    return currentPosition + (velocity * duration) + (0.5f * deceleration * duration * duration)
}

/**
 * Clamps a position to boundaries with optional rubber-banding.
 */
fun clampWithRubberBand(position: Float, min: Float, max: Float, rubberBandFactor: Float = 0.3f): Float {
    return when {
        position < min -> min - (min - position) * rubberBandFactor
        position > max -> max + (position - max) * rubberBandFactor
        else -> position
    }
}
