/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

/**
 * Represents an interception event that was captured by the interception framework.
 *
 * Each event records what was intercepted, the original value, the intercepted/modified value,
 * and which rule triggered the interception.
 *
 * @property id Unique identifier for this event
 * @property timestamp When the interception occurred
 * @property type Type of interception that occurred
 * @property ruleId ID of the rule that triggered this event (null if global logging)
 * @property ruleName Name of the rule for display purposes
 * @property originalValue String representation of the original value before interception
 * @property interceptedValue String representation of the value after interception (or null if blocked)
 * @property action Action that was performed
 * @property details Additional details about the interception
 */
data class InterceptionEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: InterceptionType,
    val ruleId: String?,
    val ruleName: String?,
    val originalValue: String,
    val interceptedValue: String?,
    val action: InterceptionAction,
    val details: Map<String, String> = emptyMap(),
) {
    /**
     * Returns a formatted timestamp string.
     */
    val formattedTimestamp: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
            return format.format(date)
        }

    /**
     * Returns a short description of the event.
     */
    val shortDescription: String
        get() = when (type) {
            InterceptionType.VIEW -> "View: ${ruleName ?: "Global"}"
            InterceptionType.TOUCH -> "Touch: ${originalValue.take(20)}"
            InterceptionType.LOCATION -> "Location: ${originalValue.take(20)}"
        }

    /**
     * Returns whether the event was a modification (not just logged or blocked).
     */
    val wasModified: Boolean
        get() = action == InterceptionAction.MODIFY && interceptedValue != null && interceptedValue != originalValue

    companion object {
        // Detail keys for VIEW events
        const val DETAIL_VIEW_ID = "view_id"
        const val DETAIL_VIEW_CLASS = "view_class"
        const val DETAIL_VIEW_CONTENT_DESC = "view_content_desc"
        const val DETAIL_VIEW_ACTION = "view_action"

        // Detail keys for TOUCH events
        const val DETAIL_TOUCH_X = "touch_x"
        const val DETAIL_TOUCH_Y = "touch_y"
        const val DETAIL_TOUCH_ACTION = "touch_action"
        const val DETAIL_TOUCH_POINTER_ID = "touch_pointer_id"
        const val DETAIL_TOUCH_MODIFIED_X = "touch_modified_x"
        const val DETAIL_TOUCH_MODIFIED_Y = "touch_modified_y"

        // Detail keys for LOCATION events
        const val DETAIL_LOCATION_PROVIDER = "location_provider"
        const val DETAIL_LOCATION_LAT = "location_lat"
        const val DETAIL_LOCATION_LNG = "location_lng"
        const val DETAIL_LOCATION_ALTITUDE = "location_altitude"
        const val DETAIL_LOCATION_ACCURACY = "location_accuracy"
        const val DETAIL_LOCATION_MOCK_LAT = "location_mock_lat"
        const val DETAIL_LOCATION_MOCK_LNG = "location_mock_lng"

        /**
         * Creates a VIEW interception event.
         */
        fun viewEvent(
            ruleId: String?,
            ruleName: String?,
            viewId: String?,
            viewClass: String,
            contentDesc: String?,
            action: InterceptionAction,
            blocked: Boolean = false,
        ): InterceptionEvent = InterceptionEvent(
            type = InterceptionType.VIEW,
            ruleId = ruleId,
            ruleName = ruleName,
            originalValue = buildString {
                append(viewClass)
                viewId?.let { append(" ($it)") }
            },
            interceptedValue = if (blocked) null else "Allowed",
            action = action,
            details = buildMap {
                viewId?.let { put(DETAIL_VIEW_ID, it) }
                put(DETAIL_VIEW_CLASS, viewClass)
                contentDesc?.let { put(DETAIL_VIEW_CONTENT_DESC, it) }
            },
        )

        /**
         * Creates a TOUCH interception event.
         */
        fun touchEvent(
            ruleId: String?,
            ruleName: String?,
            x: Float,
            y: Float,
            touchAction: Int,
            action: InterceptionAction,
            modifiedX: Float? = null,
            modifiedY: Float? = null,
        ): InterceptionEvent = InterceptionEvent(
            type = InterceptionType.TOUCH,
            ruleId = ruleId,
            ruleName = ruleName,
            originalValue = "($x, $y)",
            interceptedValue = when {
                action == InterceptionAction.BLOCK -> null
                modifiedX != null && modifiedY != null -> "($modifiedX, $modifiedY)"
                else -> "($x, $y)"
            },
            action = action,
            details = buildMap {
                put(DETAIL_TOUCH_X, x.toString())
                put(DETAIL_TOUCH_Y, y.toString())
                put(DETAIL_TOUCH_ACTION, touchAction.toString())
                modifiedX?.let { put(DETAIL_TOUCH_MODIFIED_X, it.toString()) }
                modifiedY?.let { put(DETAIL_TOUCH_MODIFIED_Y, it.toString()) }
            },
        )

        /**
         * Creates a LOCATION interception event.
         */
        fun locationEvent(
            ruleId: String?,
            ruleName: String?,
            provider: String,
            latitude: Double,
            longitude: Double,
            altitude: Double?,
            accuracy: Float?,
            action: InterceptionAction,
            mockLatitude: Double? = null,
            mockLongitude: Double? = null,
        ): InterceptionEvent = InterceptionEvent(
            type = InterceptionType.LOCATION,
            ruleId = ruleId,
            ruleName = ruleName,
            originalValue = "$latitude, $longitude",
            interceptedValue = when {
                action == InterceptionAction.BLOCK -> null
                mockLatitude != null && mockLongitude != null -> "$mockLatitude, $mockLongitude"
                else -> "$latitude, $longitude"
            },
            action = action,
            details = buildMap {
                put(DETAIL_LOCATION_PROVIDER, provider)
                put(DETAIL_LOCATION_LAT, latitude.toString())
                put(DETAIL_LOCATION_LNG, longitude.toString())
                altitude?.let { put(DETAIL_LOCATION_ALTITUDE, it.toString()) }
                accuracy?.let { put(DETAIL_LOCATION_ACCURACY, it.toString()) }
                mockLatitude?.let { put(DETAIL_LOCATION_MOCK_LAT, it.toString()) }
                mockLongitude?.let { put(DETAIL_LOCATION_MOCK_LNG, it.toString()) }
            },
        )
    }
}
