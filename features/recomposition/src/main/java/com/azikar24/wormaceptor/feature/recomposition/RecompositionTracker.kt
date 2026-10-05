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
        val rate: SlidingWindowRate = SlidingWindowRate(RateWindowMs, RateWindowCapacity),
    ) {
        fun snapshot(nowMs: Long): RecompositionData = RecompositionData(
            name = name,
            count = count.get(),
            lastTimestamp = lastTimestamp.get(),
            ratePerSecond = rate.ratePerSecond(nowMs),
        )
    }

    // Rate reflects recent activity only, so idle composables fall off the top of the list
    private const val RateWindowMs = 3_000L
    private const val RateWindowCapacity = 256

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
        entry.rate.record(now)
    }

    fun getAll(): Map<String, RecompositionData> {
        val now = System.currentTimeMillis()
        return tracked.mapValues { (_, entry) -> entry.snapshot(now) }
    }

    fun getRate(name: String): Float = tracked[name]?.rate?.ratePerSecond(System.currentTimeMillis()) ?: 0f

    fun getTopRecomposers(limit: Int = 10): List<RecompositionData> {
        val now = System.currentTimeMillis()
        return tracked.values
            .map { entry -> entry.snapshot(now) }
            .sortedWith(compareByDescending<RecompositionData> { it.ratePerSecond }.thenByDescending { it.count })
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
