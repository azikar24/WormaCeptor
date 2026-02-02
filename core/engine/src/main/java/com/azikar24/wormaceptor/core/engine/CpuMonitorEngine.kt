/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.os.SystemClock
import com.azikar24.wormaceptor.domain.entities.CpuInfo
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
import java.io.File

/**
 * Engine that monitors CPU usage and exposes it as StateFlows.
 *
 * Reads from /proc/stat for CPU usage calculations using jiffies delta method.
 * Reads from /sys/devices/system/cpu/ for frequency information.
 * Temperature reading attempts multiple thermal zone sources.
 *
 * Features:
 * - Configurable sampling interval
 * - Per-core CPU usage tracking
 * - Overall CPU usage percentage
 * - CPU frequency monitoring
 * - Temperature monitoring (if available)
 * - Circular buffer for history
 */
class CpuMonitorEngine(
    private val intervalMs: Long = DEFAULT_INTERVAL_MS,
    private val historySize: Int = DEFAULT_HISTORY_SIZE,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var monitorJob: Job? = null

    // Current CPU snapshot
    private val _currentCpu = MutableStateFlow(CpuInfo.empty())
    val currentCpu: StateFlow<CpuInfo> = _currentCpu.asStateFlow()

    // CPU history for charts (circular buffer)
    private val _cpuHistory = MutableStateFlow<List<CpuInfo>>(emptyList())
    val cpuHistory: StateFlow<List<CpuInfo>> = _cpuHistory.asStateFlow()

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // History buffer
    private val historyBuffer = ArrayDeque<CpuInfo>(historySize)
    private val bufferLock = Any()

    // Previous CPU stats for delta calculation
    private var previousCpuStats: CpuStats? = null
    private var previousPerCoreStats: List<CpuStats>? = null

    // Core count (cached)
    private val coreCount: Int by lazy {
        Runtime.getRuntime().availableProcessors()
    }

    /**
     * Starts CPU monitoring.
     * If already monitoring, this is a no-op.
     */
    fun start() {
        if (_isMonitoring.value) return

        _isMonitoring.value = true
        monitorJob = scope.launch {
            while (isActive && _isMonitoring.value) {
                val sample = collectCpuSample()
                _currentCpu.value = sample
                addToHistory(sample)
                delay(intervalMs)
            }
        }
    }

    /**
     * Stops CPU monitoring.
     */
    fun stop() {
        _isMonitoring.value = false
        monitorJob?.cancel()
        monitorJob = null
    }

    /**
     * Clears all CPU history.
     */
    fun clearHistory() {
        synchronized(bufferLock) {
            historyBuffer.clear()
        }
        _cpuHistory.value = emptyList()
        previousCpuStats = null
        previousPerCoreStats = null
    }

    /**
     * Takes a single CPU sample without affecting history.
     */
    fun takeSample(): CpuInfo = collectCpuSample()

    private fun collectCpuSample(): CpuInfo {
        val timestamp = System.currentTimeMillis()

        // Read /proc/stat for CPU usage
        val (overallUsage, perCoreUsage) = readCpuUsage()

        // Read CPU frequency
        val frequency = readCpuFrequency()

        // Read CPU temperature
        val temperature = readCpuTemperature()

        // Get system uptime
        val uptime = SystemClock.elapsedRealtime()

        return CpuInfo(
            timestamp = timestamp,
            overallUsagePercent = overallUsage,
            perCoreUsage = perCoreUsage,
            coreCount = coreCount,
            cpuFrequencyMHz = frequency,
            cpuTemperature = temperature,
            uptime = uptime,
        )
    }

    /**
     * Reads CPU usage from /proc/stat.
     * Returns overall usage and per-core usage percentages.
     */
    private fun readCpuUsage(): Pair<Float, List<Float>> {
        return try {
            val lines = File(PROC_STAT_PATH).readLines()

            // Parse overall CPU line (first line starting with "cpu ")
            val overallLine = lines.firstOrNull { it.startsWith("cpu ") }
            val currentOverallStats = overallLine?.let { parseCpuLine(it) }

            // Calculate overall usage from delta
            val overallUsage = calculateUsagePercent(previousCpuStats, currentOverallStats)
            previousCpuStats = currentOverallStats

            // Parse per-core CPU lines (lines starting with "cpu0", "cpu1", etc.)
            val perCoreLines = lines.filter { line ->
                line.startsWith("cpu") && line.getOrNull(3)?.isDigit() == true
            }
            val currentPerCoreStats = perCoreLines.map { parseCpuLine(it) }

            // Calculate per-core usage from delta
            val prevStats = previousPerCoreStats
            val perCoreUsage = if (prevStats != null && prevStats.size == currentPerCoreStats.size) {
                currentPerCoreStats.mapIndexed { index, currentStats ->
                    calculateUsagePercent(prevStats[index], currentStats)
                }
            } else {
                List(currentPerCoreStats.size) { 0f }
            }
            previousPerCoreStats = currentPerCoreStats

            Pair(overallUsage, perCoreUsage)
        } catch (e: Exception) {
            // Fallback: return zeros if we can't read /proc/stat
            Pair(0f, List(coreCount) { 0f })
        }
    }

    /**
     * Parses a CPU line from /proc/stat into CpuStats.
     * Format: cpu[N] user nice system idle iowait irq softirq steal guest guest_nice
     */
    private fun parseCpuLine(line: String): CpuStats {
        val parts = line.split("\\s+".toRegex()).drop(1) // Drop "cpu" or "cpuN"
        return if (parts.size >= 4) {
            val user = parts.getOrNull(0)?.toLongOrNull() ?: 0L
            val nice = parts.getOrNull(1)?.toLongOrNull() ?: 0L
            val system = parts.getOrNull(2)?.toLongOrNull() ?: 0L
            val idle = parts.getOrNull(3)?.toLongOrNull() ?: 0L
            val iowait = parts.getOrNull(4)?.toLongOrNull() ?: 0L
            val irq = parts.getOrNull(5)?.toLongOrNull() ?: 0L
            val softirq = parts.getOrNull(6)?.toLongOrNull() ?: 0L
            val steal = parts.getOrNull(7)?.toLongOrNull() ?: 0L

            CpuStats(
                user = user,
                nice = nice,
                system = system,
                idle = idle,
                iowait = iowait,
                irq = irq,
                softirq = softirq,
                steal = steal,
            )
        } else {
            CpuStats()
        }
    }

    /**
     * Calculates CPU usage percentage from the delta between two stat readings.
     */
    private fun calculateUsagePercent(previous: CpuStats?, current: CpuStats?): Float {
        if (previous == null || current == null) return 0f

        val prevTotal = previous.total
        val currTotal = current.total
        val prevIdle = previous.idle + previous.iowait
        val currIdle = current.idle + current.iowait

        val totalDelta = currTotal - prevTotal
        val idleDelta = currIdle - prevIdle

        return if (totalDelta > 0) {
            ((totalDelta - idleDelta).toFloat() / totalDelta.toFloat()) * 100f
        } else {
            0f
        }.coerceIn(0f, 100f)
    }

    /**
     * Reads current CPU frequency from /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq.
     * Returns frequency in MHz.
     */
    private fun readCpuFrequency(): Long {
        return try {
            // Try scaling_cur_freq first (most accurate)
            val freqKHz = File(CPU_FREQ_PATH).readText().trim().toLongOrNull()
            if (freqKHz != null && freqKHz > 0) {
                return freqKHz / 1000 // Convert kHz to MHz
            }

            // Fallback: try cpuinfo_cur_freq
            val fallbackFreqKHz = File(CPU_FREQ_FALLBACK_PATH).readText().trim().toLongOrNull()
            if (fallbackFreqKHz != null && fallbackFreqKHz > 0) {
                return fallbackFreqKHz / 1000
            }

            0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Reads CPU temperature from thermal zones.
     * Returns temperature in Celsius, or null if unavailable.
     */
    private fun readCpuTemperature(): Float? {
        return try {
            // Try multiple thermal zone paths
            for (zonePath in THERMAL_ZONE_PATHS) {
                try {
                    val tempFile = File(zonePath)
                    if (tempFile.exists()) {
                        val tempMilliCelsius = tempFile.readText().trim().toLongOrNull()
                        if (tempMilliCelsius != null && tempMilliCelsius > 0) {
                            // Temperature is in millidegrees Celsius
                            return tempMilliCelsius / 1000f
                        }
                    }
                } catch (e: Exception) {
                    // Try next path
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun addToHistory(sample: CpuInfo) {
        synchronized(bufferLock) {
            if (historyBuffer.size >= historySize) {
                historyBuffer.removeFirst()
            }
            historyBuffer.addLast(sample)
            _cpuHistory.value = historyBuffer.toList()
        }
    }

    /**
     * Internal data class for CPU statistics from /proc/stat.
     */
    private data class CpuStats(
        val user: Long = 0L,
        val nice: Long = 0L,
        val system: Long = 0L,
        val idle: Long = 0L,
        val iowait: Long = 0L,
        val irq: Long = 0L,
        val softirq: Long = 0L,
        val steal: Long = 0L,
    ) {
        val total: Long
            get() = user + nice + system + idle + iowait + irq + softirq + steal
    }

    companion object {
        /** Default sampling interval: 1 second */
        const val DEFAULT_INTERVAL_MS = 1000L

        /** Default history size: 60 samples (1 minute at 1-second intervals) */
        const val DEFAULT_HISTORY_SIZE = 60

        /** Warning threshold for CPU usage percentage */
        const val CPU_WARNING_THRESHOLD = 80f

        /** Medium threshold for CPU usage percentage */
        const val CPU_MEDIUM_THRESHOLD = 50f

        // System file paths
        private const val PROC_STAT_PATH = "/proc/stat"
        private const val CPU_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"
        private const val CPU_FREQ_FALLBACK_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq"

        // Thermal zone paths to try (varies by device)
        private val THERMAL_ZONE_PATHS = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
        )
    }
}
