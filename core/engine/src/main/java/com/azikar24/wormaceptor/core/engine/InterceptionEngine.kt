/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.location.Location
import android.view.MotionEvent
import android.view.View
import com.azikar24.wormaceptor.domain.entities.InterceptionAction
import com.azikar24.wormaceptor.domain.entities.InterceptionConfig
import com.azikar24.wormaceptor.domain.entities.InterceptionEvent
import com.azikar24.wormaceptor.domain.entities.InterceptionRule
import com.azikar24.wormaceptor.domain.entities.InterceptionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.regex.Pattern

/**
 * Engine for intercepting views, touch events, and location requests at runtime.
 *
 * This engine provides a powerful interception framework that allows developers to:
 * - Register interception rules for views (by ID, class, or content description)
 * - Register touch event interceptors (block, modify coordinates, delay)
 * - Register location interceptors (mock location, add offset)
 * - Log all interception events for debugging
 * - Enable/disable individual rules or globally
 *
 * Usage:
 * ```kotlin
 * val engine = InterceptionEngine()
 *
 * // Enable interception
 * engine.enable()
 *
 * // Add a rule to mock location
 * engine.addRule(InterceptionRule.mockLocation("NYC", 40.7128, -74.0060))
 *
 * // Intercept a location request
 * val interceptedLocation = engine.interceptLocation(originalLocation)
 *
 * // Check touch events
 * val result = engine.interceptTouchEvent(motionEvent)
 * if (result.blocked) return true // Consume the event
 *
 * // Check view interactions
 * if (engine.shouldBlockView(view)) return // Don't process the view
 * ```
 */
@Suppress("TooManyFunctions")
class InterceptionEngine {

    // Current configuration
    private val _config = MutableStateFlow(InterceptionConfig.DEFAULT)
    val config: StateFlow<InterceptionConfig> = _config.asStateFlow()

    // Event log
    private val eventLog = ConcurrentLinkedQueue<InterceptionEvent>()
    private val _events = MutableStateFlow<List<InterceptionEvent>>(emptyList())
    val events: StateFlow<List<InterceptionEvent>> = _events.asStateFlow()

    // Statistics
    private val _stats = MutableStateFlow(InterceptionStats.empty())
    val stats: StateFlow<InterceptionStats> = _stats.asStateFlow()

    // Compiled patterns cache for performance
    private val patternCache = mutableMapOf<String, Pattern>()

    /**
     * Enables the interception framework globally.
     */
    fun enable() {
        _config.value = _config.value.copy(globalEnabled = true)
    }

    /**
     * Disables the interception framework globally.
     */
    fun disable() {
        _config.value = _config.value.copy(globalEnabled = false)
    }

    /**
     * Returns whether interception is globally enabled.
     */
    fun isEnabled(): Boolean = _config.value.globalEnabled

    /**
     * Sets the entire configuration.
     */
    fun setConfig(config: InterceptionConfig) {
        _config.value = config
        patternCache.clear() // Clear cache when config changes
    }

    /**
     * Enables or disables a specific interception type.
     */
    fun setTypeEnabled(type: InterceptionType, enabled: Boolean) {
        _config.value = when (type) {
            InterceptionType.VIEW -> _config.value.copy(viewInterceptionEnabled = enabled)
            InterceptionType.TOUCH -> _config.value.copy(touchInterceptionEnabled = enabled)
            InterceptionType.LOCATION -> _config.value.copy(locationInterceptionEnabled = enabled)
        }
    }

    /**
     * Enables or disables event logging.
     */
    fun setLogEvents(enabled: Boolean) {
        _config.value = _config.value.copy(logEvents = enabled)
    }

    // ================== Rule Management ==================

    /**
     * Adds a new interception rule.
     */
    fun addRule(rule: InterceptionRule) {
        val currentRules = _config.value.rules.toMutableList()
        currentRules.add(rule)
        _config.value = _config.value.copy(rules = currentRules)
    }

    /**
     * Removes a rule by its ID.
     */
    fun removeRule(ruleId: String) {
        val currentRules = _config.value.rules.filter { it.id != ruleId }
        _config.value = _config.value.copy(rules = currentRules)
        patternCache.remove(ruleId)
    }

    /**
     * Updates an existing rule.
     */
    fun updateRule(rule: InterceptionRule) {
        val currentRules = _config.value.rules.map {
            if (it.id == rule.id) rule else it
        }
        _config.value = _config.value.copy(rules = currentRules)
        patternCache.remove(rule.id)
    }

    /**
     * Enables or disables a specific rule.
     */
    fun setRuleEnabled(ruleId: String, enabled: Boolean) {
        val rule = _config.value.rules.find { it.id == ruleId } ?: return
        updateRule(rule.copy(enabled = enabled))
    }

    /**
     * Returns a rule by its ID.
     */
    fun getRule(ruleId: String): InterceptionRule? = _config.value.rules.find { it.id == ruleId }

    /**
     * Clears all rules.
     */
    fun clearRules() {
        _config.value = _config.value.copy(rules = emptyList())
        patternCache.clear()
    }

    // ================== View Interception ==================

    /**
     * Result of view interception check.
     */
    data class ViewInterceptionResult(
        val blocked: Boolean,
        val matchedRule: InterceptionRule?,
    )

    /**
     * Checks if a view should be blocked based on registered rules.
     *
     * @param view The view to check
     * @return ViewInterceptionResult indicating whether the view should be blocked
     */
    fun interceptView(view: View): ViewInterceptionResult {
        if (!_config.value.globalEnabled || !_config.value.viewInterceptionEnabled) {
            return ViewInterceptionResult(blocked = false, matchedRule = null)
        }

        val viewId = try {
            view.resources?.getResourceEntryName(view.id)
        } catch (e: Exception) {
            null
        }
        val viewClass = view.javaClass.simpleName
        val contentDesc = view.contentDescription?.toString()

        val matchedRule = findMatchingViewRule(viewId, viewClass, contentDesc)

        if (matchedRule != null) {
            val blocked = matchedRule.action == InterceptionAction.BLOCK
            logEvent(
                InterceptionEvent.viewEvent(
                    ruleId = matchedRule.id,
                    ruleName = matchedRule.name,
                    viewId = viewId,
                    viewClass = viewClass,
                    contentDesc = contentDesc,
                    action = matchedRule.action,
                    blocked = blocked,
                ),
            )
            incrementStats(InterceptionType.VIEW, blocked)
            return ViewInterceptionResult(blocked = blocked, matchedRule = matchedRule)
        }

        return ViewInterceptionResult(blocked = false, matchedRule = null)
    }

    /**
     * Convenience method to check if a view should be blocked.
     */
    fun shouldBlockView(view: View): Boolean = interceptView(view).blocked

    private fun findMatchingViewRule(viewId: String?, viewClass: String, contentDesc: String?): InterceptionRule? {
        return _config.value.activeRulesByType(InterceptionType.VIEW).find { rule ->
            val idPattern = rule.parameters[InterceptionRule.PARAM_VIEW_ID_PATTERN]
            val classPattern = rule.parameters[InterceptionRule.PARAM_VIEW_CLASS_PATTERN]
            val descPattern = rule.parameters[InterceptionRule.PARAM_CONTENT_DESC_PATTERN]

            when {
                idPattern != null && viewId != null -> matchesPattern(idPattern, viewId)
                classPattern != null -> matchesPattern(classPattern, viewClass)
                descPattern != null && contentDesc != null -> matchesPattern(descPattern, contentDesc)
                else -> rule.targetPattern == "*" || matchesPattern(rule.targetPattern, viewClass)
            }
        }
    }

    // ================== Touch Interception ==================

    /**
     * Result of touch event interception.
     */
    data class TouchInterceptionResult(
        val blocked: Boolean,
        val modifiedEvent: MotionEvent?,
        val delayMs: Long,
        val matchedRule: InterceptionRule?,
    )

    /**
     * Intercepts a touch event and returns the interception result.
     *
     * @param event The motion event to intercept
     * @return TouchInterceptionResult with the interception outcome
     */
    fun interceptTouchEvent(event: MotionEvent): TouchInterceptionResult {
        if (!_config.value.globalEnabled || !_config.value.touchInterceptionEnabled) {
            return TouchInterceptionResult(
                blocked = false,
                modifiedEvent = null,
                delayMs = 0,
                matchedRule = null,
            )
        }

        val x = event.x
        val y = event.y

        val matchedRule = findMatchingTouchRule(x, y)

        if (matchedRule != null) {
            return processTouchRule(event, matchedRule)
        }

        return TouchInterceptionResult(
            blocked = false,
            modifiedEvent = null,
            delayMs = 0,
            matchedRule = null,
        )
    }

    private fun findMatchingTouchRule(x: Float, y: Float): InterceptionRule? {
        return _config.value.activeRulesByType(InterceptionType.TOUCH).find { rule ->
            when {
                rule.targetPattern == "*" -> true
                rule.targetPattern.startsWith("area:") -> {
                    val left = rule.parameters[InterceptionRule.PARAM_TOUCH_AREA_LEFT]?.toFloatOrNull() ?: 0f
                    val top = rule.parameters[InterceptionRule.PARAM_TOUCH_AREA_TOP]?.toFloatOrNull() ?: 0f
                    val right = rule.parameters[InterceptionRule.PARAM_TOUCH_AREA_RIGHT]?.toFloatOrNull() ?: Float.MAX_VALUE
                    val bottom = rule.parameters[InterceptionRule.PARAM_TOUCH_AREA_BOTTOM]?.toFloatOrNull() ?: Float.MAX_VALUE
                    x >= left && x <= right && y >= top && y <= bottom
                }
                rule.targetPattern.startsWith("offset:") -> true
                rule.targetPattern.startsWith("delay:") -> true
                else -> true
            }
        }
    }

    private fun processTouchRule(event: MotionEvent, rule: InterceptionRule): TouchInterceptionResult {
        val x = event.x
        val y = event.y

        return when (rule.action) {
            InterceptionAction.BLOCK -> {
                logEvent(
                    InterceptionEvent.touchEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        x = x,
                        y = y,
                        touchAction = event.action,
                        action = InterceptionAction.BLOCK,
                    ),
                )
                incrementStats(InterceptionType.TOUCH, blocked = true)
                TouchInterceptionResult(blocked = true, modifiedEvent = null, delayMs = 0, matchedRule = rule)
            }

            InterceptionAction.MODIFY -> {
                val offsetX = rule.parameters[InterceptionRule.PARAM_TOUCH_OFFSET_X]?.toFloatOrNull() ?: 0f
                val offsetY = rule.parameters[InterceptionRule.PARAM_TOUCH_OFFSET_Y]?.toFloatOrNull() ?: 0f
                val newX = x + offsetX
                val newY = y + offsetY

                val modifiedEvent = MotionEvent.obtain(event).apply {
                    setLocation(newX, newY)
                }

                logEvent(
                    InterceptionEvent.touchEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        x = x,
                        y = y,
                        touchAction = event.action,
                        action = InterceptionAction.MODIFY,
                        modifiedX = newX,
                        modifiedY = newY,
                    ),
                )
                incrementStats(InterceptionType.TOUCH, blocked = false)
                TouchInterceptionResult(blocked = false, modifiedEvent = modifiedEvent, delayMs = 0, matchedRule = rule)
            }

            InterceptionAction.DELAY -> {
                val delayMs = rule.parameters[InterceptionRule.PARAM_TOUCH_DELAY_MS]?.toLongOrNull() ?: 0L
                logEvent(
                    InterceptionEvent.touchEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        x = x,
                        y = y,
                        touchAction = event.action,
                        action = InterceptionAction.DELAY,
                    ),
                )
                incrementStats(InterceptionType.TOUCH, blocked = false)
                TouchInterceptionResult(blocked = false, modifiedEvent = null, delayMs = delayMs, matchedRule = rule)
            }

            InterceptionAction.LOG_ONLY -> {
                logEvent(
                    InterceptionEvent.touchEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        x = x,
                        y = y,
                        touchAction = event.action,
                        action = InterceptionAction.LOG_ONLY,
                    ),
                )
                incrementStats(InterceptionType.TOUCH, blocked = false)
                TouchInterceptionResult(blocked = false, modifiedEvent = null, delayMs = 0, matchedRule = rule)
            }

            else -> TouchInterceptionResult(blocked = false, modifiedEvent = null, delayMs = 0, matchedRule = rule)
        }
    }

    // ================== Location Interception ==================

    /**
     * Result of location interception.
     */
    data class LocationInterceptionResult(
        val blocked: Boolean,
        val modifiedLocation: Location?,
        val matchedRule: InterceptionRule?,
    )

    /**
     * Intercepts a location request and returns the interception result.
     *
     * @param location The original location
     * @return LocationInterceptionResult with the interception outcome
     */
    fun interceptLocation(location: Location): LocationInterceptionResult {
        if (!_config.value.globalEnabled || !_config.value.locationInterceptionEnabled) {
            return LocationInterceptionResult(blocked = false, modifiedLocation = null, matchedRule = null)
        }

        val matchedRule = _config.value.activeRulesByType(InterceptionType.LOCATION).firstOrNull()

        if (matchedRule != null) {
            return processLocationRule(location, matchedRule)
        }

        return LocationInterceptionResult(blocked = false, modifiedLocation = null, matchedRule = null)
    }

    /**
     * Convenience method to get an intercepted location or the original.
     */
    fun getInterceptedLocation(location: Location): Location {
        val result = interceptLocation(location)
        return result.modifiedLocation ?: location
    }

    private fun processLocationRule(location: Location, rule: InterceptionRule): LocationInterceptionResult {
        val originalLat = location.latitude
        val originalLng = location.longitude

        return when (rule.action) {
            InterceptionAction.BLOCK -> {
                logEvent(
                    InterceptionEvent.locationEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        provider = location.provider ?: "unknown",
                        latitude = originalLat,
                        longitude = originalLng,
                        altitude = location.altitude,
                        accuracy = location.accuracy,
                        action = InterceptionAction.BLOCK,
                    ),
                )
                incrementStats(InterceptionType.LOCATION, blocked = true)
                LocationInterceptionResult(blocked = true, modifiedLocation = null, matchedRule = rule)
            }

            InterceptionAction.MOCK -> {
                val mockLat = rule.parameters[InterceptionRule.PARAM_MOCK_LATITUDE]?.toDoubleOrNull() ?: originalLat
                val mockLng = rule.parameters[InterceptionRule.PARAM_MOCK_LONGITUDE]?.toDoubleOrNull() ?: originalLng
                val mockAlt = rule.parameters[InterceptionRule.PARAM_MOCK_ALTITUDE]?.toDoubleOrNull() ?: location.altitude
                val mockAcc = rule.parameters[InterceptionRule.PARAM_MOCK_ACCURACY]?.toFloatOrNull() ?: location.accuracy

                val modifiedLocation = Location(location.provider ?: "mock").apply {
                    latitude = mockLat
                    longitude = mockLng
                    altitude = mockAlt
                    accuracy = mockAcc
                    time = System.currentTimeMillis()
                }

                logEvent(
                    InterceptionEvent.locationEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        provider = location.provider ?: "unknown",
                        latitude = originalLat,
                        longitude = originalLng,
                        altitude = location.altitude,
                        accuracy = location.accuracy,
                        action = InterceptionAction.MOCK,
                        mockLatitude = mockLat,
                        mockLongitude = mockLng,
                    ),
                )
                incrementStats(InterceptionType.LOCATION, blocked = false)
                LocationInterceptionResult(blocked = false, modifiedLocation = modifiedLocation, matchedRule = rule)
            }

            InterceptionAction.MODIFY -> {
                val offsetLat = rule.parameters[InterceptionRule.PARAM_LOCATION_OFFSET_LAT]?.toDoubleOrNull() ?: 0.0
                val offsetLng = rule.parameters[InterceptionRule.PARAM_LOCATION_OFFSET_LNG]?.toDoubleOrNull() ?: 0.0

                val modifiedLocation = Location(location).apply {
                    latitude = originalLat + offsetLat
                    longitude = originalLng + offsetLng
                }

                logEvent(
                    InterceptionEvent.locationEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        provider = location.provider ?: "unknown",
                        latitude = originalLat,
                        longitude = originalLng,
                        altitude = location.altitude,
                        accuracy = location.accuracy,
                        action = InterceptionAction.MODIFY,
                        mockLatitude = modifiedLocation.latitude,
                        mockLongitude = modifiedLocation.longitude,
                    ),
                )
                incrementStats(InterceptionType.LOCATION, blocked = false)
                LocationInterceptionResult(blocked = false, modifiedLocation = modifiedLocation, matchedRule = rule)
            }

            InterceptionAction.LOG_ONLY -> {
                logEvent(
                    InterceptionEvent.locationEvent(
                        ruleId = rule.id,
                        ruleName = rule.name,
                        provider = location.provider ?: "unknown",
                        latitude = originalLat,
                        longitude = originalLng,
                        altitude = location.altitude,
                        accuracy = location.accuracy,
                        action = InterceptionAction.LOG_ONLY,
                    ),
                )
                incrementStats(InterceptionType.LOCATION, blocked = false)
                LocationInterceptionResult(blocked = false, modifiedLocation = null, matchedRule = rule)
            }

            else -> LocationInterceptionResult(blocked = false, modifiedLocation = null, matchedRule = rule)
        }
    }

    // ================== Event Logging ==================

    private fun logEvent(event: InterceptionEvent) {
        if (!_config.value.logEvents) return

        eventLog.add(event)

        // Trim log if it exceeds max size
        while (eventLog.size > _config.value.maxEventLogSize) {
            eventLog.poll()
        }

        _events.value = eventLog.toList()
    }

    /**
     * Clears all logged events.
     */
    fun clearEvents() {
        eventLog.clear()
        _events.value = emptyList()
    }

    /**
     * Returns events filtered by type.
     */
    fun getEventsByType(type: InterceptionType): List<InterceptionEvent> = _events.value.filter { it.type == type }

    // ================== Statistics ==================

    private fun incrementStats(type: InterceptionType, blocked: Boolean) {
        val current = _stats.value
        _stats.value = when (type) {
            InterceptionType.VIEW -> current.copy(
                viewInterceptions = current.viewInterceptions + 1,
                viewBlocked = current.viewBlocked + if (blocked) 1 else 0,
            )
            InterceptionType.TOUCH -> current.copy(
                touchInterceptions = current.touchInterceptions + 1,
                touchBlocked = current.touchBlocked + if (blocked) 1 else 0,
            )
            InterceptionType.LOCATION -> current.copy(
                locationInterceptions = current.locationInterceptions + 1,
                locationBlocked = current.locationBlocked + if (blocked) 1 else 0,
            )
        }
    }

    /**
     * Clears all statistics.
     */
    fun clearStats() {
        _stats.value = InterceptionStats.empty()
    }

    // ================== Pattern Matching ==================

    private fun matchesPattern(pattern: String, value: String): Boolean {
        if (pattern == "*") return true
        if (pattern == value) return true

        return try {
            val compiledPattern = patternCache.getOrPut(pattern) {
                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            }
            compiledPattern.matcher(value).matches()
        } catch (e: Exception) {
            pattern.equals(value, ignoreCase = true)
        }
    }

    companion object {
        private const val TAG = "InterceptionEngine"
    }
}

/**
 * Statistics about interception operations.
 */
data class InterceptionStats(
    val viewInterceptions: Int,
    val viewBlocked: Int,
    val touchInterceptions: Int,
    val touchBlocked: Int,
    val locationInterceptions: Int,
    val locationBlocked: Int,
) {
    val totalInterceptions: Int
        get() = viewInterceptions + touchInterceptions + locationInterceptions

    val totalBlocked: Int
        get() = viewBlocked + touchBlocked + locationBlocked

    companion object {
        fun empty() = InterceptionStats(
            viewInterceptions = 0,
            viewBlocked = 0,
            touchInterceptions = 0,
            touchBlocked = 0,
            locationInterceptions = 0,
            locationBlocked = 0,
        )
    }
}
