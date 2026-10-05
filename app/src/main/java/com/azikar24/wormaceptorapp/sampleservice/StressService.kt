package com.azikar24.wormaceptorapp.sampleservice

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.system.measureNanoTime

object StressService {

    private const val CPU_INNER_ITERATIONS = 200_000
    private const val JANK_INNER_ITERATIONS = 50_000
    private const val BYTE_MASK = 0xFF
    private const val DEFAULT_DURATION_MS = 3_000L
    private const val DEFAULT_ALLOCATION_BYTES = 50 * 1024 * 1024

    // Jank pattern: short bursts with gaps so the UI stays interactive between drops.
    // Total wall clock ≈ JANK_BURSTS * (JANK_BURST_MS + JANK_GAP_MS).
    private const val JANK_BURSTS = 5
    private const val JANK_BURST_MS = 80L
    private const val JANK_GAP_MS = 220L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val parkedAllocations = mutableListOf<ByteArray>()

    fun burnCpu(durationMs: Long = DEFAULT_DURATION_MS): Job {
        return scope.launch {
            val end = System.currentTimeMillis() + durationMs
            while (System.currentTimeMillis() < end) {
                @Suppress("UnusedPrivateMember")
                measureNanoTime {
                    var acc = 0.0
                    for (i in 1..CPU_INNER_ITERATIONS) {
                        acc += sqrt(i.toDouble()) * sqrt((i + 1).toDouble())
                    }
                }
            }
        }
    }

    fun allocateMemory(
        sizeBytes: Int = DEFAULT_ALLOCATION_BYTES,
        holdMs: Long = DEFAULT_DURATION_MS,
    ): Job {
        return scope.launch {
            val chunk = ByteArray(sizeBytes) { (it and BYTE_MASK).toByte() }
            synchronized(parkedAllocations) { parkedAllocations.add(chunk) }
            delay(holdMs)
            synchronized(parkedAllocations) { parkedAllocations.remove(chunk) }
        }
    }

    /**
     * Posts a sequence of short main-thread blocks so the UI visibly drops frames
     * without becoming unresponsive — the gaps between bursts let touch input and
     * recompositions through.
     */
    fun jankMainThread(
        bursts: Int = JANK_BURSTS,
        burstMs: Long = JANK_BURST_MS,
        gapMs: Long = JANK_GAP_MS,
    ) {
        val interval = burstMs + gapMs
        repeat(bursts) { index ->
            mainHandler.postDelayed(
                {
                    val end = System.currentTimeMillis() + burstMs
                    while (System.currentTimeMillis() < end) {
                        var acc = 0.0
                        for (i in 1..JANK_INNER_ITERATIONS) {
                            acc += sqrt(i.toDouble())
                        }
                        @Suppress("UnusedPrivateMember")
                        acc.toString()
                    }
                },
                index * interval,
            )
        }
    }
}
