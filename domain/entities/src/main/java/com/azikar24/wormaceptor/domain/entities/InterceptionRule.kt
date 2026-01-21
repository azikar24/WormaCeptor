/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

/**
 * Types of interception that can be performed.
 */
enum class InterceptionType {
    /** Intercept view operations by ID, class, or content description */
    VIEW,
    /** Intercept touch events to block, modify coordinates, or delay */
    TOUCH,
    /** Intercept location requests to mock or offset coordinates */
    LOCATION,
}

/**
 * Actions that can be performed when an interception rule matches.
 */
enum class InterceptionAction {
    /** Block the event entirely */
    BLOCK,
    /** Log the event without modification */
    LOG_ONLY,
    /** Modify the event according to rule parameters */
    MODIFY,
    /** Delay the event processing */
    DELAY,
    /** Replace with mock data */
    MOCK,
}

/**
 * Represents an interception rule that defines what to intercept and how.
 *
 * Rules can target views, touch events, or location requests and specify
 * actions to take when a match occurs.
 *
 * @property id Unique identifier for this rule
 * @property name Human-readable name for this rule
 * @property type Type of interception (VIEW, TOUCH, LOCATION)
 * @property targetPattern Pattern to match against (view ID, class name, etc.)
 * @property action Action to perform when matched
 * @property enabled Whether this rule is currently active
 * @property parameters Additional parameters for the action (e.g., mock coordinates, delay duration)
 * @property priority Priority for rule evaluation (higher = evaluated first)
 * @property createdAt Timestamp when the rule was created
 */
data class InterceptionRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: InterceptionType,
    val targetPattern: String,
    val action: InterceptionAction,
    val enabled: Boolean = true,
    val parameters: Map<String, String> = emptyMap(),
    val priority: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
) {
    companion object {
        // Parameter keys for VIEW interception
        const val PARAM_VIEW_ID_PATTERN = "view_id_pattern"
        const val PARAM_VIEW_CLASS_PATTERN = "view_class_pattern"
        const val PARAM_CONTENT_DESC_PATTERN = "content_desc_pattern"

        // Parameter keys for TOUCH interception
        const val PARAM_TOUCH_OFFSET_X = "touch_offset_x"
        const val PARAM_TOUCH_OFFSET_Y = "touch_offset_y"
        const val PARAM_TOUCH_DELAY_MS = "touch_delay_ms"
        const val PARAM_TOUCH_AREA_LEFT = "touch_area_left"
        const val PARAM_TOUCH_AREA_TOP = "touch_area_top"
        const val PARAM_TOUCH_AREA_RIGHT = "touch_area_right"
        const val PARAM_TOUCH_AREA_BOTTOM = "touch_area_bottom"

        // Parameter keys for LOCATION interception
        const val PARAM_MOCK_LATITUDE = "mock_latitude"
        const val PARAM_MOCK_LONGITUDE = "mock_longitude"
        const val PARAM_MOCK_ALTITUDE = "mock_altitude"
        const val PARAM_MOCK_ACCURACY = "mock_accuracy"
        const val PARAM_LOCATION_OFFSET_LAT = "location_offset_lat"
        const val PARAM_LOCATION_OFFSET_LNG = "location_offset_lng"

        /**
         * Creates a rule to block a view by its resource ID pattern.
         */
        fun blockViewById(name: String, idPattern: String): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.VIEW,
            targetPattern = idPattern,
            action = InterceptionAction.BLOCK,
            parameters = mapOf(PARAM_VIEW_ID_PATTERN to idPattern),
        )

        /**
         * Creates a rule to log view interactions by class name.
         */
        fun logViewByClass(name: String, classPattern: String): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.VIEW,
            targetPattern = classPattern,
            action = InterceptionAction.LOG_ONLY,
            parameters = mapOf(PARAM_VIEW_CLASS_PATTERN to classPattern),
        )

        /**
         * Creates a rule to block touches in a specific area.
         */
        fun blockTouchInArea(
            name: String,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
        ): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.TOUCH,
            targetPattern = "area:$left,$top,$right,$bottom",
            action = InterceptionAction.BLOCK,
            parameters = mapOf(
                PARAM_TOUCH_AREA_LEFT to left.toString(),
                PARAM_TOUCH_AREA_TOP to top.toString(),
                PARAM_TOUCH_AREA_RIGHT to right.toString(),
                PARAM_TOUCH_AREA_BOTTOM to bottom.toString(),
            ),
        )

        /**
         * Creates a rule to offset touch coordinates.
         */
        fun offsetTouch(name: String, offsetX: Float, offsetY: Float): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.TOUCH,
            targetPattern = "offset:$offsetX,$offsetY",
            action = InterceptionAction.MODIFY,
            parameters = mapOf(
                PARAM_TOUCH_OFFSET_X to offsetX.toString(),
                PARAM_TOUCH_OFFSET_Y to offsetY.toString(),
            ),
        )

        /**
         * Creates a rule to delay touch events.
         */
        fun delayTouch(name: String, delayMs: Long): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.TOUCH,
            targetPattern = "delay:$delayMs",
            action = InterceptionAction.DELAY,
            parameters = mapOf(PARAM_TOUCH_DELAY_MS to delayMs.toString()),
        )

        /**
         * Creates a rule to mock location to specific coordinates.
         */
        fun mockLocation(
            name: String,
            latitude: Double,
            longitude: Double,
            altitude: Double = 0.0,
            accuracy: Float = 1.0f,
        ): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.LOCATION,
            targetPattern = "mock:$latitude,$longitude",
            action = InterceptionAction.MOCK,
            parameters = mapOf(
                PARAM_MOCK_LATITUDE to latitude.toString(),
                PARAM_MOCK_LONGITUDE to longitude.toString(),
                PARAM_MOCK_ALTITUDE to altitude.toString(),
                PARAM_MOCK_ACCURACY to accuracy.toString(),
            ),
        )

        /**
         * Creates a rule to offset location coordinates.
         */
        fun offsetLocation(
            name: String,
            latOffset: Double,
            lngOffset: Double,
        ): InterceptionRule = InterceptionRule(
            name = name,
            type = InterceptionType.LOCATION,
            targetPattern = "offset:$latOffset,$lngOffset",
            action = InterceptionAction.MODIFY,
            parameters = mapOf(
                PARAM_LOCATION_OFFSET_LAT to latOffset.toString(),
                PARAM_LOCATION_OFFSET_LNG to lngOffset.toString(),
            ),
        )
    }
}
