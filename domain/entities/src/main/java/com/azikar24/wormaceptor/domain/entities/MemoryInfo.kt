package com.azikar24.wormaceptor.domain.entities

/**
 * Represents memory usage information at a specific point in time.
 *
 * All memory values are in bytes unless otherwise specified.
 *
 * @property timestamp Timestamp in milliseconds when this snapshot was taken
 * @property usedMemory Currently used memory by the Java heap (bytes)
 * @property freeMemory Free memory available in the Java heap (bytes)
 * @property totalMemory Total memory currently allocated to the Java heap (bytes)
 * @property maxMemory Maximum memory the Java heap can grow to (bytes)
 * @property heapUsagePercent Percentage of heap used (0-100)
 * @property nativeHeapSize Total native heap size (bytes)
 * @property nativeHeapAllocated Currently allocated native heap memory (bytes)
 * @property nativeHeapFree Free native heap memory (bytes)
 * @property gcCount Number of GC runs since last sample (if trackable)
 */
data class MemoryInfo(
    val timestamp: Long,
    val usedMemory: Long,
    val freeMemory: Long,
    val totalMemory: Long,
    val maxMemory: Long,
    val heapUsagePercent: Float,
    val nativeHeapSize: Long,
    val nativeHeapAllocated: Long,
    val nativeHeapFree: Long = nativeHeapSize - nativeHeapAllocated,
    val gcCount: Long = 0,
) {
    companion object {
        /**
         * Creates an empty MemoryInfo instance with zero values.
         */
        fun empty(): MemoryInfo = MemoryInfo(
            timestamp = 0L,
            usedMemory = 0L,
            freeMemory = 0L,
            totalMemory = 0L,
            maxMemory = 0L,
            heapUsagePercent = 0f,
            nativeHeapSize = 0L,
            nativeHeapAllocated = 0L,
            nativeHeapFree = 0L,
            gcCount = 0L,
        )
    }
}
