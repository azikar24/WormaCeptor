package com.azikar24.wormaceptor.core.engine

import android.os.Debug
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Engine that monitors memory usage and exposes it as StateFlows.
 *
 * Uses Android's Runtime.getRuntime() for Java heap metrics and
 * android.os.Debug for native heap metrics.
 *
 * Features:
 * - Configurable sampling interval
 * - Circular buffer for history (default 60 samples for 1-minute chart)
 * - GC event detection (approximate)
 * - Force GC capability
 */
class MemoryMonitorEngine(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
    private val historySize: Int = DEFAULT_HISTORY_SIZE,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var monitorJob: Job? = null

    private val runtime = Runtime.getRuntime()

    // Current memory snapshot
    private val _currentMemory = MutableStateFlow(MemoryInfo.empty())
    val currentMemory: StateFlow<MemoryInfo> = _currentMemory.asStateFlow()

    // Memory history for charts (circular buffer)
    private val _memoryHistory = MutableStateFlow<List<MemoryInfo>>(emptyList())
    val memoryHistory: StateFlow<List<MemoryInfo>> = _memoryHistory.asStateFlow()

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // History buffer
    private val historyBuffer = ArrayDeque<MemoryInfo>(historySize)
    private val bufferLock = Any()

    // GC tracking
    private var lastGcCount = getApproximateGcCount()

    /**
     * Starts memory monitoring.
     * If already monitoring, this is a no-op.
     */
    fun start() {
        if (_isMonitoring.value) return

        _isMonitoring.value = true
        monitorJob = scope.launch {
            while (isActive && _isMonitoring.value) {
                val sample = collectMemorySample()
                _currentMemory.value = sample
                addToHistory(sample)
                delay(intervalMs)
            }
        }
    }

    /**
     * Stops memory monitoring.
     */
    fun stop() {
        _isMonitoring.value = false
        monitorJob?.cancel()
        monitorJob = null
    }

    /**
     * Clears all memory history.
     */
    fun clearHistory() {
        synchronized(bufferLock) {
            historyBuffer.clear()
        }
        _memoryHistory.value = emptyList()
    }

    /**
     * Forces a garbage collection run.
     * Note: This is a suggestion to the VM, not guaranteed to execute immediately.
     */
    fun forceGc() {
        System.gc()
        // Also request finalizers to run
        System.runFinalization()
        // Collect again after finalizers
        System.gc()
    }

    /**
     * Takes a single memory sample without affecting history.
     * Useful for one-off measurements.
     */
    fun takeSample(): MemoryInfo = collectMemorySample()

    private fun collectMemorySample(): MemoryInfo {
        val timestamp = System.currentTimeMillis()

        // Java heap metrics from Runtime
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory

        // Calculate heap usage percentage
        val heapUsagePercent = if (maxMemory > 0) {
            usedMemory.toFloat() / maxMemory.toFloat() * 100f
        } else {
            0f
        }

        // Native heap metrics from Debug
        val nativeHeapSize = Debug.getNativeHeapSize()
        val nativeHeapAllocated = Debug.getNativeHeapAllocatedSize()
        val nativeHeapFree = Debug.getNativeHeapFreeSize()

        // GC tracking (approximate)
        val currentGcCount = getApproximateGcCount()
        val gcDelta = if (currentGcCount > lastGcCount) {
            currentGcCount - lastGcCount
        } else {
            0L
        }
        lastGcCount = currentGcCount

        return MemoryInfo(
            timestamp = timestamp,
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            totalMemory = totalMemory,
            maxMemory = maxMemory,
            heapUsagePercent = heapUsagePercent,
            nativeHeapSize = nativeHeapSize,
            nativeHeapAllocated = nativeHeapAllocated,
            nativeHeapFree = nativeHeapFree,
            gcCount = gcDelta,
        )
    }

    private fun addToHistory(sample: MemoryInfo) {
        synchronized(bufferLock) {
            if (historyBuffer.size >= historySize) {
                historyBuffer.removeFirst()
            }
            historyBuffer.addLast(sample)
            _memoryHistory.value = historyBuffer.toList()
        }
    }

    /**
     * Gets an approximate GC count by using a weak reference technique.
     * This is not perfectly accurate but gives a reasonable indication.
     */
    private fun getApproximateGcCount(): Long {
        // Use Debug.getRuntimeStat if available (API 23+)
        return try {
            val gcCountString = Debug.getRuntimeStat("art.gc.gc-count")
            gcCountString?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            // Fallback: return 0 if stats not available
            0L
        }
    }

    companion object {
        /** Default sampling interval: 1 second */
        const val DEFAULT_INTERVAL_MS = 1000L

        /** Default history size: 60 samples (1 minute at 1-second intervals) */
        const val DEFAULT_HISTORY_SIZE = 60

        /** Warning threshold for heap usage percentage */
        const val HEAP_WARNING_THRESHOLD = 80f
    }
}
