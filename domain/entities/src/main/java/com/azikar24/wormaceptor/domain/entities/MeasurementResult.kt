/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the result of a distance measurement between two points.
 *
 * @property startPoint The starting point of the measurement
 * @property endPoint The ending point of the measurement
 * @property distancePx The distance in pixels
 * @property distanceDp The distance in density-independent pixels
 * @property angle The angle from horizontal in degrees (0-360)
 * @property timestamp When this measurement was taken
 */
data class MeasurementResult(
    val startPoint: Point,
    val endPoint: Point,
    val distancePx: Float,
    val distanceDp: Float,
    val angle: Float,
    val timestamp: Long,
) {
    /**
     * Represents a 2D point with x and y coordinates.
     */
    data class Point(val x: Float, val y: Float)

    companion object {
        /**
         * Creates an empty measurement result with default values.
         */
        fun empty() = MeasurementResult(
            startPoint = Point(0f, 0f),
            endPoint = Point(0f, 0f),
            distancePx = 0f,
            distanceDp = 0f,
            angle = 0f,
            timestamp = 0L,
        )
    }
}

/**
 * Represents the measurement of a specific view's dimensions and layout properties.
 *
 * @property viewClass The simple class name of the view
 * @property resourceId The resource ID name of the view, if available
 * @property widthPx The width of the view in pixels
 * @property heightPx The height of the view in pixels
 * @property widthDp The width of the view in density-independent pixels
 * @property heightDp The height of the view in density-independent pixels
 * @property x The X position of the view on screen
 * @property y The Y position of the view on screen
 * @property paddingLeft Left padding in pixels
 * @property paddingTop Top padding in pixels
 * @property paddingRight Right padding in pixels
 * @property paddingBottom Bottom padding in pixels
 * @property marginLeft Left margin in pixels (if ViewGroup.MarginLayoutParams)
 * @property marginTop Top margin in pixels (if ViewGroup.MarginLayoutParams)
 * @property marginRight Right margin in pixels (if ViewGroup.MarginLayoutParams)
 * @property marginBottom Bottom margin in pixels (if ViewGroup.MarginLayoutParams)
 */
data class ViewMeasurement(
    val viewClass: String,
    val resourceId: String?,
    val widthPx: Int,
    val heightPx: Int,
    val widthDp: Float,
    val heightDp: Float,
    val x: Int,
    val y: Int,
    val paddingLeft: Int,
    val paddingTop: Int,
    val paddingRight: Int,
    val paddingBottom: Int,
    val marginLeft: Int,
    val marginTop: Int,
    val marginRight: Int,
    val marginBottom: Int,
) {
    companion object {
        /**
         * Creates an empty view measurement with default values.
         */
        fun empty() = ViewMeasurement(
            viewClass = "",
            resourceId = null,
            widthPx = 0,
            heightPx = 0,
            widthDp = 0f,
            heightDp = 0f,
            x = 0,
            y = 0,
            paddingLeft = 0,
            paddingTop = 0,
            paddingRight = 0,
            paddingBottom = 0,
            marginLeft = 0,
            marginTop = 0,
            marginRight = 0,
            marginBottom = 0,
        )
    }
}

/**
 * The mode of measurement operation.
 */
enum class MeasurementMode {
    /** Measure distance between two tapped points */
    DISTANCE,

    /** Measure bounds and properties of a tapped view */
    VIEW_BOUNDS,
}

/**
 * Configuration for the measurement tool.
 *
 * @property showGuidelines Whether to show alignment guidelines
 * @property snapToGrid Whether to snap measurement points to a grid
 * @property gridSize The grid size in dp for snap-to-grid feature
 */
data class MeasurementConfig(
    val showGuidelines: Boolean = true,
    val snapToGrid: Boolean = false,
    val gridSize: Int = 8,
) {
    companion object {
        val DEFAULT = MeasurementConfig()
    }
}
