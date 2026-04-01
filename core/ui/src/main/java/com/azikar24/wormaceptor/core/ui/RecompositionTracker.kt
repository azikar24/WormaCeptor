package com.azikar24.wormaceptor.core.ui

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Tracks Jetpack Compose recomposition counts per composable in real-time.
 *
 * Thread-safe singleton that records recomposition events registered via
 * [Modifier.trackRecomposition][trackRecomposition] and exposes aggregated
 * data for the Recomposition Inspector dashboard.
 */
object RecompositionTracker {

    /**
     * Immutable snapshot of a single composable's recomposition data.
     *
     * @property name Identifier of the tracked composable.
     * @property count Total recomposition count since tracking began.
     * @property lastTimestamp Epoch millis of the most recent recomposition.
     */
    data class RecompositionData(
        val name: String,
        val count: Long,
        val lastTimestamp: Long,
    )

    /**
     * Internal mutable holder stored in the concurrent map.
     * Mutations are confined to atomic operations so no external lock is needed.
     */
    private class MutableEntry(
        val name: String,
        val count: AtomicLong = AtomicLong(0),
        val lastTimestamp: AtomicLong = AtomicLong(0),
    ) {
        fun snapshot(): RecompositionData = RecompositionData(
            name = name,
            count = count.get(),
            lastTimestamp = lastTimestamp.get(),
        )
    }

    private val tracked = ConcurrentHashMap<String, MutableEntry>()
    private val sessionStart = AtomicLong(0)

    /**
     * Records a single recomposition event for the composable identified by [name].
     * Creates a new entry if the composable has not been seen before.
     */
    fun record(name: String) {
        val now = System.currentTimeMillis()
        sessionStart.compareAndSet(0, now)

        val entry = tracked.getOrPut(name) { MutableEntry(name) }
        entry.count.incrementAndGet()
        entry.lastTimestamp.set(now)
    }

    /**
     * Returns an immutable snapshot of all tracked composables.
     */
    fun getAll(): Map<String, RecompositionData> = tracked.mapValues { (_, entry) -> entry.snapshot() }

    /**
     * Calculates the recomposition rate (per second) for [name] over the
     * session duration. Returns `0f` when no data is available.
     */
    fun getRate(name: String): Float {
        val entry = tracked[name] ?: return 0f
        val elapsed = getSessionDuration().coerceAtLeast(1)
        val count = entry.count.get().toFloat()
        return count / (elapsed / 1000f).coerceAtLeast(0.001f)
    }

    /**
     * Returns the top [limit] composables sorted by recomposition rate (descending).
     */
    fun getTopRecomposers(limit: Int = 10): List<RecompositionData> {
        val sessionMs = getSessionDuration().coerceAtLeast(1)
        val sessionSeconds = (sessionMs / 1000f).coerceAtLeast(0.001f)
        return tracked.values
            .map { it.snapshot() }
            .sortedByDescending { it.count.toFloat() / sessionSeconds }
            .take(limit)
    }

    /**
     * Clears all tracking data and resets the session start time.
     */
    fun reset() {
        tracked.clear()
        sessionStart.set(0)
    }

    /**
     * Returns how long (in millis) since the first [record] call, or `0` if
     * nothing has been recorded yet.
     */
    fun getSessionDuration(): Long {
        val start = sessionStart.get()
        if (start == 0L) return 0L
        return System.currentTimeMillis() - start
    }

    /**
     * Returns the sum of all recomposition counts across every tracked composable.
     */
    fun getTotalRecompositions(): Long = tracked.values.sumOf { it.count.get() }
}
