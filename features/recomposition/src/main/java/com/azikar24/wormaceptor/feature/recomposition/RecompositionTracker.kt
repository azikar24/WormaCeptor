package com.azikar24.wormaceptor.feature.recomposition

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Tracks Jetpack Compose recomposition counts per composable in real-time.
 *
 * Thread-safe singleton. The public entry point is `Modifier.trackRecomposition(name)`
 * in `api-client`, which reflectively invokes [record] when this class is on the
 * classpath. In release builds this feature module is absent (it ships only through
 * `debugImplementation`), so the reflection lookup fails and the modifier no-ops.
 */
object RecompositionTracker {

    data class RecompositionData(
        val name: String,
        val count: Long,
        val lastTimestamp: Long,
        val ratePerSecond: Float = 0f,
    )

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
     * Invoked reflectively from `api-client`'s `trackRecomposition` modifier.
     */
    fun record(name: String) {
        val now = System.currentTimeMillis()
        sessionStart.compareAndSet(0, now)

        val entry = tracked.getOrPut(name) { MutableEntry(name) }
        entry.count.incrementAndGet()
        entry.lastTimestamp.set(now)
    }

    fun getAll(): Map<String, RecompositionData> = tracked.mapValues { (_, entry) -> entry.snapshot() }

    fun getRate(name: String): Float {
        val entry = tracked[name] ?: return 0f
        val elapsed = getSessionDuration().coerceAtLeast(1)
        val count = entry.count.get().toFloat()
        return count / (elapsed / 1000f).coerceAtLeast(0.001f)
    }

    fun getTopRecomposers(limit: Int = 10): List<RecompositionData> {
        val sessionMs = getSessionDuration().coerceAtLeast(1)
        val sessionSeconds = (sessionMs / 1000f).coerceAtLeast(0.001f)
        return tracked.values
            .map { entry ->
                val snapshot = entry.snapshot()
                snapshot.copy(ratePerSecond = snapshot.count.toFloat() / sessionSeconds)
            }
            .sortedByDescending { it.ratePerSecond }
            .take(limit)
    }

    fun reset() {
        tracked.clear()
        sessionStart.set(0)
    }

    fun getSessionDuration(): Long {
        val start = sessionStart.get()
        if (start == 0L) return 0L
        return System.currentTimeMillis() - start
    }

    fun getTotalRecompositions(): Long = tracked.values.sumOf { it.count.get() }
}
