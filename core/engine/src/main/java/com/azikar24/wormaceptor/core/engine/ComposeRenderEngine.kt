/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.ComposeRenderInfo
import com.azikar24.wormaceptor.domain.entities.ComposeRenderStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Engine that tracks Compose recomposition counts and render times.
 *
 * This is a simplified implementation that provides manual tracking
 * via a Modifier extension. Users can wrap composables with
 * `Modifier.trackRecomposition(name)` to track their recomposition behavior.
 *
 * Note: For more detailed tracking, consider using Compose compiler metrics
 * and reports which provide comprehensive recomposition analysis.
 */
class ComposeRenderEngine {

    // Internal tracking data
    private data class TrackingData(
        var recomposeCount: Int = 0,
        var skipCount: Int = 0,
        var lastRenderTimeNs: Long = 0L,
        var totalRenderTimeNs: Long = 0L,
        var renderCount: Int = 0,
        var parameters: List<String> = emptyList(),
        var depth: Int = 0,
        var lastTimestamp: Long = 0L,
    )

    private val trackedComposables = ConcurrentHashMap<String, TrackingData>()

    private val _composables = MutableStateFlow<List<ComposeRenderInfo>>(emptyList())
    val composables: StateFlow<List<ComposeRenderInfo>> = _composables.asStateFlow()

    private val _stats = MutableStateFlow(ComposeRenderStats.EMPTY)
    val stats: StateFlow<ComposeRenderStats> = _stats.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    /**
     * Starts tracking composable recompositions.
     * Existing data is preserved - call [clearStats] to reset.
     */
    fun startTracking() {
        _isTracking.value = true
    }

    /**
     * Stops tracking composable recompositions.
     * Data is preserved and can be reviewed after stopping.
     */
    fun stopTracking() {
        _isTracking.value = false
    }

    /**
     * Clears all tracked composable data.
     */
    fun clearStats() {
        trackedComposables.clear()
        _composables.value = emptyList()
        _stats.value = ComposeRenderStats.EMPTY
    }

    /**
     * Records a recomposition event for the specified composable.
     * Call this from within a composable to track recomposition.
     *
     * @param name Unique name/identifier for the composable
     * @param parameters Optional list of parameter names for context
     * @param depth Nesting depth in the composition tree
     */
    fun trackRecomposition(
        name: String,
        parameters: List<String> = emptyList(),
        depth: Int = 0,
    ) {
        if (!_isTracking.value) return

        val startTime = System.nanoTime()

        val data = trackedComposables.getOrPut(name) { TrackingData() }
        data.apply {
            recomposeCount++
            this.parameters = parameters
            this.depth = depth
            lastTimestamp = System.currentTimeMillis()
        }

        // Record render time after the fact (this is approximate)
        val renderTime = System.nanoTime() - startTime
        data.lastRenderTimeNs = renderTime
        data.totalRenderTimeNs += renderTime
        data.renderCount++

        updateFlows()
    }

    /**
     * Records that a composable was skipped (not recomposed).
     * This is useful for tracking skip counts when using SideEffect or similar.
     *
     * @param name Unique name/identifier for the composable
     */
    fun trackSkip(name: String) {
        if (!_isTracking.value) return

        val data = trackedComposables.getOrPut(name) { TrackingData() }
        data.skipCount++
        data.lastTimestamp = System.currentTimeMillis()

        updateFlows()
    }

    /**
     * Manually records render time for a composable.
     * Useful when wrapping composable content with timing.
     *
     * @param name Unique name/identifier for the composable
     * @param renderTimeNs Render time in nanoseconds
     */
    fun recordRenderTime(name: String, renderTimeNs: Long) {
        if (!_isTracking.value) return

        val data = trackedComposables.getOrPut(name) { TrackingData() }
        data.lastRenderTimeNs = renderTimeNs
        data.totalRenderTimeNs += renderTimeNs
        data.renderCount++
        data.lastTimestamp = System.currentTimeMillis()

        updateFlows()
    }

    /**
     * Registers a composable for tracking without recording a recomposition.
     * Useful for initial setup before tracking begins.
     *
     * @param name Unique name/identifier for the composable
     * @param parameters Optional list of parameter names for context
     * @param depth Nesting depth in the composition tree
     */
    fun trackComposable(
        name: String,
        parameters: List<String> = emptyList(),
        depth: Int = 0,
    ) {
        val data = trackedComposables.getOrPut(name) { TrackingData() }
        data.parameters = parameters
        data.depth = depth

        if (_isTracking.value) {
            updateFlows()
        }
    }

    private fun updateFlows() {
        val timestamp = System.currentTimeMillis()
        val infoList = trackedComposables.map { (name, data) ->
            ComposeRenderInfo(
                composableName = name,
                recomposeCount = data.recomposeCount,
                skipCount = data.skipCount,
                lastRenderTimeNs = data.lastRenderTimeNs,
                averageRenderTimeNs = if (data.renderCount > 0) {
                    data.totalRenderTimeNs / data.renderCount
                } else 0L,
                parameters = data.parameters,
                depth = data.depth,
                timestamp = data.lastTimestamp,
            )
        }.sortedByDescending { it.recomposeCount }

        _composables.value = infoList

        // Calculate stats
        if (infoList.isNotEmpty()) {
            val totalRecompositions = infoList.sumOf { it.recomposeCount }
            val avgRatio = infoList.map { it.recomposeRatio }.average().toFloat()
            val slowest = infoList.maxByOrNull { it.averageRenderTimeNs }?.composableName
            val mostRecomposed = infoList.maxByOrNull { it.recomposeCount }?.composableName

            _stats.value = ComposeRenderStats(
                totalComposables = infoList.size,
                totalRecompositions = totalRecompositions,
                averageRecomposeRatio = avgRatio,
                slowestComposable = slowest,
                mostRecomposed = mostRecomposed,
            )
        } else {
            _stats.value = ComposeRenderStats.EMPTY
        }
    }

    companion object {
        @Volatile
        private var instance: ComposeRenderEngine? = null

        /**
         * Gets or creates a singleton instance of ComposeRenderEngine.
         * Useful for global tracking across the app.
         */
        fun getInstance(): ComposeRenderEngine {
            return instance ?: synchronized(this) {
                instance ?: ComposeRenderEngine().also { instance = it }
            }
        }
    }
}
