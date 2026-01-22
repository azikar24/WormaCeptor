/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Configuration for the Interception Framework.
 *
 * Controls global interception settings and maintains the list of active rules.
 *
 * @property globalEnabled Whether interception is globally enabled
 * @property logEvents Whether to log all interception events
 * @property rules List of interception rules
 * @property maxEventLogSize Maximum number of events to keep in the log
 * @property viewInterceptionEnabled Whether view interception is enabled
 * @property touchInterceptionEnabled Whether touch interception is enabled
 * @property locationInterceptionEnabled Whether location interception is enabled
 */
data class InterceptionConfig(
    val globalEnabled: Boolean = false,
    val logEvents: Boolean = true,
    val rules: List<InterceptionRule> = emptyList(),
    val maxEventLogSize: Int = DEFAULT_MAX_EVENT_LOG_SIZE,
    val viewInterceptionEnabled: Boolean = true,
    val touchInterceptionEnabled: Boolean = true,
    val locationInterceptionEnabled: Boolean = true,
) {
    /**
     * Returns only enabled rules sorted by priority (highest first).
     */
    val activeRules: List<InterceptionRule>
        get() = rules.filter { it.enabled }.sortedByDescending { it.priority }

    /**
     * Returns rules filtered by type.
     */
    fun rulesByType(type: InterceptionType): List<InterceptionRule> = rules.filter { it.type == type }

    /**
     * Returns active rules filtered by type.
     */
    fun activeRulesByType(type: InterceptionType): List<InterceptionRule> = activeRules.filter { it.type == type }

    /**
     * Returns whether a specific interception type is enabled.
     */
    fun isTypeEnabled(type: InterceptionType): Boolean = when (type) {
        InterceptionType.VIEW -> viewInterceptionEnabled
        InterceptionType.TOUCH -> touchInterceptionEnabled
        InterceptionType.LOCATION -> locationInterceptionEnabled
    }

    /**
     * Returns the count of rules by type.
     */
    fun ruleCountByType(type: InterceptionType): Int = rulesByType(type).size

    /**
     * Returns the count of enabled rules by type.
     */
    fun enabledRuleCountByType(type: InterceptionType): Int = activeRulesByType(type).size

    companion object {
        /** Default maximum number of events to keep in the log */
        const val DEFAULT_MAX_EVENT_LOG_SIZE = 1000

        /** Minimum event log size */
        const val MIN_EVENT_LOG_SIZE = 100

        /** Maximum event log size */
        const val MAX_EVENT_LOG_SIZE = 10000

        /** Default configuration with no rules */
        val DEFAULT = InterceptionConfig()

        /**
         * Common rule templates for quick setup.
         */
        object Templates {
            /**
             * Template: Log all button clicks.
             */
            val logButtonClicks = InterceptionRule(
                name = "Log Button Clicks",
                type = InterceptionType.VIEW,
                targetPattern = ".*Button.*",
                action = InterceptionAction.LOG_ONLY,
                parameters = mapOf(InterceptionRule.PARAM_VIEW_CLASS_PATTERN to ".*Button.*"),
            )

            /**
             * Template: Log all RecyclerView interactions.
             */
            val logRecyclerViewInteractions = InterceptionRule(
                name = "Log RecyclerView Interactions",
                type = InterceptionType.VIEW,
                targetPattern = ".*RecyclerView.*",
                action = InterceptionAction.LOG_ONLY,
                parameters = mapOf(InterceptionRule.PARAM_VIEW_CLASS_PATTERN to ".*RecyclerView.*"),
            )

            /**
             * Template: Block touch in status bar area.
             */
            val blockStatusBarTouch = InterceptionRule(
                name = "Block Status Bar Touch",
                type = InterceptionType.TOUCH,
                targetPattern = "area:0,0,9999,100",
                action = InterceptionAction.BLOCK,
                parameters = mapOf(
                    InterceptionRule.PARAM_TOUCH_AREA_LEFT to "0",
                    InterceptionRule.PARAM_TOUCH_AREA_TOP to "0",
                    InterceptionRule.PARAM_TOUCH_AREA_RIGHT to "9999",
                    InterceptionRule.PARAM_TOUCH_AREA_BOTTOM to "100",
                ),
            )

            /**
             * Template: Add touch delay for accessibility testing.
             */
            val touchDelay100ms = InterceptionRule(
                name = "Touch Delay 100ms",
                type = InterceptionType.TOUCH,
                targetPattern = "delay:100",
                action = InterceptionAction.DELAY,
                parameters = mapOf(InterceptionRule.PARAM_TOUCH_DELAY_MS to "100"),
            )

            /**
             * Template: Mock location to New York City.
             */
            val mockLocationNYC = InterceptionRule(
                name = "Mock Location - NYC",
                type = InterceptionType.LOCATION,
                targetPattern = "mock:40.7128,-74.0060",
                action = InterceptionAction.MOCK,
                parameters = mapOf(
                    InterceptionRule.PARAM_MOCK_LATITUDE to "40.7128",
                    InterceptionRule.PARAM_MOCK_LONGITUDE to "-74.0060",
                    InterceptionRule.PARAM_MOCK_ACCURACY to "10.0",
                ),
            )

            /**
             * Template: Mock location to London.
             */
            val mockLocationLondon = InterceptionRule(
                name = "Mock Location - London",
                type = InterceptionType.LOCATION,
                targetPattern = "mock:51.5074,-0.1278",
                action = InterceptionAction.MOCK,
                parameters = mapOf(
                    InterceptionRule.PARAM_MOCK_LATITUDE to "51.5074",
                    InterceptionRule.PARAM_MOCK_LONGITUDE to "-0.1278",
                    InterceptionRule.PARAM_MOCK_ACCURACY to "10.0",
                ),
            )

            /**
             * Template: Mock location to Tokyo.
             */
            val mockLocationTokyo = InterceptionRule(
                name = "Mock Location - Tokyo",
                type = InterceptionType.LOCATION,
                targetPattern = "mock:35.6762,139.6503",
                action = InterceptionAction.MOCK,
                parameters = mapOf(
                    InterceptionRule.PARAM_MOCK_LATITUDE to "35.6762",
                    InterceptionRule.PARAM_MOCK_LONGITUDE to "139.6503",
                    InterceptionRule.PARAM_MOCK_ACCURACY to "10.0",
                ),
            )

            /**
             * Template: Mock location to San Francisco.
             */
            val mockLocationSanFrancisco = InterceptionRule(
                name = "Mock Location - San Francisco",
                type = InterceptionType.LOCATION,
                targetPattern = "mock:37.7749,-122.4194",
                action = InterceptionAction.MOCK,
                parameters = mapOf(
                    InterceptionRule.PARAM_MOCK_LATITUDE to "37.7749",
                    InterceptionRule.PARAM_MOCK_LONGITUDE to "-122.4194",
                    InterceptionRule.PARAM_MOCK_ACCURACY to "10.0",
                ),
            )

            /**
             * Template: Log all touch events.
             */
            val logAllTouches = InterceptionRule(
                name = "Log All Touches",
                type = InterceptionType.TOUCH,
                targetPattern = "*",
                action = InterceptionAction.LOG_ONLY,
            )

            /**
             * Template: Log all location requests.
             */
            val logAllLocationRequests = InterceptionRule(
                name = "Log All Location Requests",
                type = InterceptionType.LOCATION,
                targetPattern = "*",
                action = InterceptionAction.LOG_ONLY,
            )

            /**
             * Returns all available templates grouped by type.
             */
            val allTemplates: Map<InterceptionType, List<InterceptionRule>>
                get() = mapOf(
                    InterceptionType.VIEW to listOf(
                        logButtonClicks,
                        logRecyclerViewInteractions,
                    ),
                    InterceptionType.TOUCH to listOf(
                        blockStatusBarTouch,
                        touchDelay100ms,
                        logAllTouches,
                    ),
                    InterceptionType.LOCATION to listOf(
                        mockLocationNYC,
                        mockLocationLondon,
                        mockLocationTokyo,
                        mockLocationSanFrancisco,
                        logAllLocationRequests,
                    ),
                )

            /**
             * Returns templates for a specific type.
             */
            fun templatesForType(type: InterceptionType): List<InterceptionRule> = allTemplates[type] ?: emptyList()
        }
    }
}
