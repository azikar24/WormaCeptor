package com.azikar24.wormaceptor.feature.recomposition

private const val MillisPerSecond = 1000f

/**
 * Events per second over the last [windowMs], from a bounded ring of event timestamps.
 * Rates above `capacity / window` saturate at that ceiling.
 */
internal class SlidingWindowRate(
    private val windowMs: Long,
    capacity: Int,
) {
    private val timestamps = LongArray(capacity)
    private var head = 0
    private var size = 0

    @Synchronized
    fun record(nowMs: Long) {
        timestamps[head] = nowMs
        head = (head + 1) % timestamps.size
        if (size < timestamps.size) size++
    }

    @Synchronized
    fun ratePerSecond(nowMs: Long): Float {
        val cutoff = nowMs - windowMs
        var inWindow = 0
        for (i in 1..size) {
            val index = (head - i + timestamps.size) % timestamps.size
            if (timestamps[index] <= cutoff) break
            inWindow++
        }
        return inWindow / (windowMs / MillisPerSecond)
    }
}
