package com.azikar24.wormaceptor.core.engine

import android.os.SystemClock
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.CpuMeasurementSource
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
 * Reads from /proc/stat for system-wide CPU usage (jiffies delta method).
 * On Android 8+ where /proc/stat may be restricted by SELinux, automatically
 * falls back to /proc/self/stat for app process CPU measurement.
 *
 * Also reads from /sys/devices/system/cpu/ for frequency information
 * and attempts multiple thermal zone sources for temperature.
 *
 * Features:
 * - Configurable sampling interval
 * - Per-core CPU usage tracking (system mode only)
 * - Overall CPU usage percentage
 * - Automatic fallback from system-wide to process-level measurement
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

    /** The most recent CPU usage snapshot. */
    val currentCpu: StateFlow<CpuInfo> = _currentCpu.asStateFlow()

    // CPU history for charts (circular buffer)
    private val _cpuHistory = MutableStateFlow<List<CpuInfo>>(emptyList())

    /** Historical CPU snapshots for charting, stored in a circular buffer. */
    val cpuHistory: StateFlow<List<CpuInfo>> = _cpuHistory.asStateFlow()

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)

    /** Whether the CPU monitor is actively sampling. */
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // History buffer
    private val historyBuffer = ArrayDeque<CpuInfo>(historySize)
    private val bufferLock = Any()

    // Previous CPU stats for system-wide delta calculation
    private var previousCpuStats: CpuStats? = null
    private var previousPerCoreStats: List<CpuStats>? = null

    // Previous stats for process-level delta calculation
    private var previousProcessCpuTicks: Long? = null
    private var previousProcessWallTimeNs: Long? = null

    // Fallback detection: switch to process mode if /proc/stat returns stale data
    private var consecutiveZeroReadings = 0
    private var useProcessFallback = false

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
     * Clears all CPU history and resets measurement strategy.
     */
    fun clearHistory() {
        synchronized(bufferLock) {
            historyBuffer.clear()
        }
        _cpuHistory.value = emptyList()
        previousCpuStats = null
        previousPerCoreStats = null
        previousProcessCpuTicks = null
        previousProcessWallTimeNs = null
        consecutiveZeroReadings = 0
        useProcessFallback = false
    }

    /**
     * Takes a single CPU sample without affecting history.
     */
    fun takeSample(): CpuInfo = collectCpuSample()

    private fun collectCpuSample(): CpuInfo {
        val timestamp = System.currentTimeMillis()

        val (overallUsage, perCoreUsage) = readCpuUsage()
        val frequency = readCpuFrequency()
        val temperature = readCpuTemperature()
        val uptime = SystemClock.elapsedRealtime()
        val source = if (useProcessFallback) {
            CpuMeasurementSource.PROCESS
        } else {
            CpuMeasurementSource.SYSTEM
        }

        return CpuInfo(
            timestamp = timestamp,
            overallUsagePercent = overallUsage,
            perCoreUsage = perCoreUsage,
            coreCount = coreCount,
            cpuFrequencyMHz = frequency,
            cpuTemperature = temperature,
            uptime = uptime,
            measurementSource = source,
        )
    }

    /**
     * Reads CPU usage, trying system-wide first and falling back to process-level
     * if /proc/stat is inaccessible or returns stale data.
     */
    private fun readCpuUsage(): Pair<Float, List<Float>> {
        if (useProcessFallback) {
            return readProcessCpuUsage()
        }

        val hadPreviousStats = previousCpuStats != null
        val result = readSystemCpuUsage()

        // After warmup (first reading is always 0 by design), detect stale data
        if (hadPreviousStats && result.first == 0f) {
            consecutiveZeroReadings++
            if (consecutiveZeroReadings >= FALLBACK_THRESHOLD) {
                useProcessFallback = true
                previousProcessCpuTicks = null
                previousProcessWallTimeNs = null
                return readProcessCpuUsage()
            }
        } else if (result.first > 0f) {
            consecutiveZeroReadings = 0
        }

        return result
    }

    /**
     * Reads system-wide CPU usage from /proc/stat.
     * Returns overall usage and per-core usage percentages.
     */
    private fun readSystemCpuUsage(): Pair<Float, List<Float>> {
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
        } catch (_: Exception) {
            // /proc/stat inaccessible — switch to process fallback immediately
            useProcessFallback = true
            previousProcessCpuTicks = null
            previousProcessWallTimeNs = null
            readProcessCpuUsage()
        }
    }

    /**
     * Reads app process CPU usage from /proc/self/stat.
     * Always accessible for the app's own process on all Android versions.
     *
     * Returns the app's CPU usage as a percentage of total system capacity (0-100).
     * Per-core data is not available in this mode.
     */
    private fun readProcessCpuUsage(): Pair<Float, List<Float>> {
        return try {
            val statContent = File(PROC_SELF_STAT_PATH).readText()

            // /proc/self/stat format: pid (comm) state ...
            // The comm field may contain spaces/parens, so find the last ')' to skip it.
            val closingParen = statContent.lastIndexOf(')')
            if (closingParen < 0) return Pair(0f, emptyList())

            val fields = statContent.substring(closingParen + 2).trim().split(" ")
            // After ')': index 0=state, 1=ppid, ..., utime(field14), stime(field15)
            val utime = fields.getOrNull(PROC_STAT_UTIME_INDEX)?.toLongOrNull() ?: 0L
            val stime = fields.getOrNull(PROC_STAT_STIME_INDEX)?.toLongOrNull() ?: 0L
            val cpuTicks = utime + stime

            val wallTimeNs = System.nanoTime()

            val prevCpu = previousProcessCpuTicks
            val prevWall = previousProcessWallTimeNs

            previousProcessCpuTicks = cpuTicks
            previousProcessWallTimeNs = wallTimeNs

            if (prevCpu != null && prevWall != null) {
                val cpuDelta = cpuTicks - prevCpu
                val wallDeltaNs = wallTimeNs - prevWall

                if (wallDeltaNs > 0 && cpuDelta >= 0) {
                    val cpuDeltaNs = cpuDelta * NS_PER_CLOCK_TICK
                    // Normalize to total CPU capacity (all cores) for 0-100% range
                    val usage = cpuDeltaNs.toFloat() / wallDeltaNs.toFloat() * 100f / coreCount
                    Pair(usage.coerceIn(0f, 100f), emptyList())
                } else {
                    Pair(0f, emptyList())
                }
            } else {
                Pair(0f, emptyList())
            }
        } catch (_: Exception) {
            Pair(0f, emptyList())
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
    private fun calculateUsagePercent(
        previous: CpuStats?,
        current: CpuStats?,
    ): Float {
        if (previous == null || current == null) return 0f

        val prevTotal = previous.total
        val currTotal = current.total
        val prevIdle = previous.idle + previous.iowait
        val currIdle = current.idle + current.iowait

        val totalDelta = currTotal - prevTotal
        val idleDelta = currIdle - prevIdle

        return if (totalDelta > 0) {
            (totalDelta - idleDelta).toFloat() / totalDelta.toFloat() * 100f
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
        } catch (_: Exception) {
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
                } catch (_: Exception) {
                    // Try next path
                }
            }
            null
        } catch (_: Exception) {
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

    /** Sampling intervals, history size, and threshold defaults. */
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
        private const val PROC_SELF_STAT_PATH = "/proc/self/stat"
        private const val CPU_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"
        private const val CPU_FREQ_FALLBACK_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq"

        /** Number of consecutive zero readings after warmup before switching to process fallback. */
        private const val FALLBACK_THRESHOLD = 3

        /** Standard Linux clock ticks per second (USER_HZ). */
        private const val CLOCK_TICKS_PER_SECOND = 100L

        /** Nanoseconds per clock tick. */
        private const val NS_PER_CLOCK_TICK = 1_000_000_000L / CLOCK_TICKS_PER_SECOND

        /** Index of utime field in /proc/self/stat (after stripping pid and comm). */
        private const val PROC_STAT_UTIME_INDEX = 11

        /** Index of stime field in /proc/self/stat (after stripping pid and comm). */
        private const val PROC_STAT_STIME_INDEX = 12

        // Thermal zone paths to try (varies by device)
        private val THERMAL_ZONE_PATHS = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
        )
    }
}
