/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.os.Build
import android.os.Looper
import android.os.StrictMode
import android.os.strictmode.Violation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicLong

/**
 * Engine that monitors main thread violations using Android's StrictMode.
 *
 * Detects operations that should not be performed on the main thread:
 * - Disk reads and writes
 * - Network operations
 * - Slow method calls
 * - Custom slow code blocks
 *
 * Features:
 * - Real-time violation detection via StrictMode.ThreadPolicy
 * - Circular buffer for violation history
 * - Statistics aggregation by violation type
 * - Enable/disable monitoring at runtime
 *
 * Note: StrictMode violation listener requires API 28+. On older devices,
 * violations are still detected but may use penaltyLog() fallback.
 */
class ThreadViolationEngine(
    private val historySize: Int = DEFAULT_HISTORY_SIZE,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val violationIdCounter = AtomicLong(0)

    // Violations list
    private val _violations = MutableStateFlow<List<ThreadViolation>>(emptyList())
    val violations: StateFlow<List<ThreadViolation>> = _violations.asStateFlow()

    // Statistics
    private val _stats = MutableStateFlow(ViolationStats.empty())
    val stats: StateFlow<ViolationStats> = _stats.asStateFlow()

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // History buffer
    private val violationBuffer = ArrayDeque<ThreadViolation>(historySize)
    private val bufferLock = Any()

    // Store original policy to restore on disable
    private var originalPolicy: StrictMode.ThreadPolicy? = null

    // Notification callback
    private var onViolationDetected: ((ThreadViolation) -> Unit)? = null

    // Package filter for violations
    private var hostPackageName: String? = null

    // System packages to exclude from violation tracking
    private val systemPackagePrefixes = listOf(
        "android.",
        "com.android.",
        "com.google.android.",
        "androidx.",
        "dalvik.",
        "java.",
        "javax.",
        "kotlin.",
        "kotlinx.",
        "sun.",
        "libcore.",
    )

    /**
     * Configures the thread violation engine with notification callback and package filter.
     *
     * @param hostPackage The host app's package name to filter violations
     * @param onViolationCallback Optional callback invoked when a violation is detected (for notifications)
     */
    fun configure(
        hostPackage: String? = null,
        onViolationCallback: ((ThreadViolation) -> Unit)? = null,
    ) {
        this.hostPackageName = hostPackage
        this.onViolationDetected = onViolationCallback
    }

    /**
     * Enables thread violation monitoring.
     * Sets up StrictMode.ThreadPolicy to detect and report violations.
     *
     * If already monitoring, this is a no-op.
     */
    fun enable() {
        if (_isMonitoring.value) return

        // Save original policy to restore later
        originalPolicy = StrictMode.getThreadPolicy()

        val builder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()

        // detectCustomSlowCalls available since API 11
        builder.detectCustomSlowCalls()

        // Set up violation listener (API 28+) or fallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.penaltyListener(Executor { it.run() }) { violation ->
                handleViolation(violation)
            }
        } else {
            // Fallback for older devices - use penaltyLog and we won't get callbacks
            // but StrictMode will still log violations to logcat
            builder.penaltyLog()
        }

        StrictMode.setThreadPolicy(builder.build())
        _isMonitoring.value = true
    }

    /**
     * Disables thread violation monitoring.
     * Restores the original StrictMode.ThreadPolicy.
     */
    fun disable() {
        if (!_isMonitoring.value) return

        // Restore original policy
        originalPolicy?.let { StrictMode.setThreadPolicy(it) }
            ?: StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)

        _isMonitoring.value = false
    }

    /**
     * Clears all recorded violations and resets statistics.
     */
    fun clearViolations() {
        synchronized(bufferLock) {
            violationBuffer.clear()
        }
        _violations.value = emptyList()
        _stats.value = ViolationStats.empty()
    }

    /**
     * Handles a violation detected by StrictMode.
     * Available only on API 28+.
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.P)
    private fun handleViolation(violation: Violation) {
        scope.launch {
            val threadViolation = parseViolation(violation)
            addViolation(threadViolation)
        }
    }

    /**
     * Parses a StrictMode Violation into our ThreadViolation entity.
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.P)
    private fun parseViolation(violation: Violation): ThreadViolation {
        val timestamp = System.currentTimeMillis()
        val violationType = determineViolationType(violation)
        val description = violation.message ?: violation.javaClass.simpleName
        val stackTrace = violation.stackTrace.map { it.toString() }
        val threadName = Thread.currentThread().name

        // Try to extract duration if available in the message
        val durationMs = extractDuration(violation.message)

        return ThreadViolation(
            id = violationIdCounter.incrementAndGet(),
            timestamp = timestamp,
            violationType = violationType,
            description = formatDescription(violationType, description),
            stackTrace = stackTrace,
            durationMs = durationMs,
            threadName = threadName,
        )
    }

    /**
     * Determines the violation type from the StrictMode Violation class.
     */
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.P)
    private fun determineViolationType(violation: Violation): ThreadViolation.ViolationType {
        val className = violation.javaClass.simpleName.lowercase()
        val message = violation.message?.lowercase() ?: ""

        return when {
            className.contains("diskread") || message.contains("read") -> {
                ThreadViolation.ViolationType.DISK_READ
            }
            className.contains("diskwrite") || message.contains("write") -> {
                ThreadViolation.ViolationType.DISK_WRITE
            }
            className.contains("network") -> {
                ThreadViolation.ViolationType.NETWORK
            }
            className.contains("customslowcall") -> {
                ThreadViolation.ViolationType.CUSTOM_SLOW_CODE
            }
            else -> {
                ThreadViolation.ViolationType.SLOW_CALL
            }
        }
    }

    /**
     * Extracts duration from violation message if present.
     */
    private fun extractDuration(message: String?): Long? {
        if (message == null) return null

        // Pattern: "duration=123 ms" or "took 123ms"
        val patterns = listOf(
            Regex("duration[=:]\\s*(\\d+)"),
            Regex("took\\s*(\\d+)"),
            Regex("(\\d+)\\s*ms"),
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].toLongOrNull()
            }
        }
        return null
    }

    /**
     * Formats the description to be more human-readable.
     */
    private fun formatDescription(type: ThreadViolation.ViolationType, originalDescription: String): String {
        val prefix = when (type) {
            ThreadViolation.ViolationType.DISK_READ -> "Disk Read"
            ThreadViolation.ViolationType.DISK_WRITE -> "Disk Write"
            ThreadViolation.ViolationType.NETWORK -> "Network Access"
            ThreadViolation.ViolationType.SLOW_CALL -> "Slow Call"
            ThreadViolation.ViolationType.CUSTOM_SLOW_CODE -> "Custom Slow Code"
        }

        // Clean up the description
        val cleaned = originalDescription
            .replace("StrictMode policy violation", "")
            .replace("android.os.strictmode.", "")
            .trim()
            .ifEmpty { "on main thread" }

        return "$prefix: $cleaned"
    }

    /**
     * Adds a violation to the buffer and updates statistics.
     * Filters out violations that don't originate from the host app or WormaCeptor.
     */
    private fun addViolation(violation: ThreadViolation) {
        // Filter out system-only violations
        if (!isRelevantViolation(violation)) {
            return
        }

        synchronized(bufferLock) {
            if (violationBuffer.size >= historySize) {
                violationBuffer.removeFirst()
            }
            violationBuffer.addLast(violation)
            _violations.value = violationBuffer.toList().reversed() // Most recent first
        }
        updateStats()

        // Invoke callback for notifications
        onViolationDetected?.invoke(violation)
    }

    /**
     * Checks if a violation is relevant based on its stack trace.
     * Returns true if the stack trace contains frames from the host app or WormaCeptor.
     */
    private fun isRelevantViolation(violation: ThreadViolation): Boolean {
        val wormaCeptorPrefix = "com.azikar24.wormaceptor"

        for (frame in violation.stackTrace) {
            // Skip system package frames
            val isSystemFrame = systemPackagePrefixes.any { prefix ->
                frame.contains(prefix)
            }
            if (isSystemFrame) continue

            // Check if it's a WormaCeptor frame
            if (frame.contains(wormaCeptorPrefix)) {
                return true
            }

            // Check if it's a host app frame
            hostPackageName?.let { hostPkg ->
                if (frame.contains(hostPkg)) {
                    return true
                }
            }
        }

        // If no host package is set, accept any non-system violation
        // Otherwise, reject if we didn't find a relevant frame
        return hostPackageName == null
    }

    /**
     * Updates the statistics based on current violations.
     */
    private fun updateStats() {
        val currentViolations = _violations.value

        val stats = ViolationStats(
            totalViolations = currentViolations.size,
            diskReadCount = currentViolations.count {
                it.violationType == ThreadViolation.ViolationType.DISK_READ
            },
            diskWriteCount = currentViolations.count {
                it.violationType == ThreadViolation.ViolationType.DISK_WRITE
            },
            networkCount = currentViolations.count {
                it.violationType == ThreadViolation.ViolationType.NETWORK
            },
            slowCallCount = currentViolations.count {
                it.violationType == ThreadViolation.ViolationType.SLOW_CALL
            },
            customSlowCodeCount = currentViolations.count {
                it.violationType == ThreadViolation.ViolationType.CUSTOM_SLOW_CODE
            },
        )
        _stats.value = stats
    }

    /**
     * Manually records a custom violation.
     * Useful for integrating with custom performance monitoring.
     */
    fun recordViolation(type: ThreadViolation.ViolationType, description: String, durationMs: Long? = null) {
        val violation = ThreadViolation(
            id = violationIdCounter.incrementAndGet(),
            timestamp = System.currentTimeMillis(),
            violationType = type,
            description = description,
            stackTrace = Thread.currentThread().stackTrace.map { it.toString() },
            durationMs = durationMs,
            threadName = Thread.currentThread().name,
        )
        scope.launch {
            addViolation(violation)
        }
    }

    /**
     * Checks if the current thread is the main thread.
     */
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    companion object {
        /** Default history size: 100 violations */
        const val DEFAULT_HISTORY_SIZE = 100
    }
}
