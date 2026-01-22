/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a touch point for touch visualization.
 *
 * @property id Unique identifier for this touch pointer (finger)
 * @property x The X coordinate of the touch point in pixels
 * @property y The Y coordinate of the touch point in pixels
 * @property pressure The pressure of the touch (0.0 to 1.0)
 * @property size The size of the touch area (0.0 to 1.0)
 * @property timestamp When this touch event occurred
 * @property action The type of touch action
 */
data class TouchPoint(
    val id: Int,
    val x: Float,
    val y: Float,
    val pressure: Float,
    val size: Float,
    val timestamp: Long,
    val action: TouchAction,
) {
    companion object {
        /**
         * Default empty TouchPoint instance.
         */
        val EMPTY = TouchPoint(
            id = -1,
            x = 0f,
            y = 0f,
            pressure = 0f,
            size = 0f,
            timestamp = 0L,
            action = TouchAction.UP,
        )
    }
}

/**
 * Represents the type of touch action.
 */
enum class TouchAction {
    /** Finger touched the screen */
    DOWN,

    /** Finger moved on the screen */
    MOVE,

    /** Finger lifted from the screen */
    UP,
}
