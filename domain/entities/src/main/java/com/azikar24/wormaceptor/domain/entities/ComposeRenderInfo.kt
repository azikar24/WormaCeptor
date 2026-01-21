/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents render tracking information for a Compose composable.
 *
 * @property composableName The name of the tracked composable
 * @property recomposeCount Number of times the composable has been recomposed
 * @property skipCount Number of times recomposition was skipped
 * @property lastRenderTimeNs Last render time in nanoseconds
 * @property averageRenderTimeNs Average render time in nanoseconds
 * @property parameters List of parameter names being tracked
 * @property depth Nesting depth in the composition tree
 * @property timestamp When this measurement was taken
 */
data class ComposeRenderInfo(
    val composableName: String,
    val recomposeCount: Int,
    val skipCount: Int,
    val lastRenderTimeNs: Long,
    val averageRenderTimeNs: Long,
    val parameters: List<String>,
    val depth: Int,
    val timestamp: Long,
) {
    /**
     * Ratio of recompositions to total composition attempts.
     * Higher values indicate more frequent recompositions.
     */
    val recomposeRatio: Float
        get() = if (recomposeCount + skipCount > 0) {
            recomposeCount.toFloat() / (recomposeCount + skipCount)
        } else 0f

    companion object {
        fun empty() = ComposeRenderInfo(
            composableName = "",
            recomposeCount = 0,
            skipCount = 0,
            lastRenderTimeNs = 0L,
            averageRenderTimeNs = 0L,
            parameters = emptyList(),
            depth = 0,
            timestamp = 0L,
        )

        /**
         * Threshold for considering recompose ratio as excessive.
         * Composables with ratio above this are highlighted as potential performance issues.
         */
        const val EXCESSIVE_RECOMPOSE_RATIO = 0.8f

        /**
         * Threshold for slow render time in nanoseconds (1ms).
         */
        const val SLOW_RENDER_THRESHOLD_NS = 1_000_000L
    }
}

/**
 * Summary statistics for all tracked composables.
 *
 * @property totalComposables Total number of tracked composables
 * @property totalRecompositions Total number of recompositions across all composables
 * @property averageRecomposeRatio Average recomposition ratio across all composables
 * @property slowestComposable Name of the composable with the longest average render time
 * @property mostRecomposed Name of the composable with the most recompositions
 */
data class ComposeRenderStats(
    val totalComposables: Int,
    val totalRecompositions: Int,
    val averageRecomposeRatio: Float,
    val slowestComposable: String?,
    val mostRecomposed: String?,
) {
    companion object {
        val EMPTY = ComposeRenderStats(
            totalComposables = 0,
            totalRecompositions = 0,
            averageRecomposeRatio = 0f,
            slowestComposable = null,
            mostRecomposed = null,
        )
    }
}
