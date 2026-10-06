package com.azikar24.wormaceptor.core.engine

import android.os.Build
import android.os.StrictMode
import android.os.strictmode.CustomViolation
import android.os.strictmode.DiskReadViolation
import android.os.strictmode.DiskWriteViolation
import android.os.strictmode.NetworkViolation
import android.os.strictmode.Violation
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    /** All recorded thread violations, most recent first. */
    val violations: StateFlow<List<ThreadViolation>> = _violations.asStateFlow()

    // Statistics
    private val _stats = MutableStateFlow(ViolationStats.empty())

    /** Aggregated violation counts by type. */
    val stats: StateFlow<ViolationStats> = _stats.asStateFlow()

    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)

    /** Whether StrictMode thread violation monitoring is active. */
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // History buffer
    private val violationBuffer = ArrayDeque<ThreadViolation>(historySize)
    private val bufferLock = Any()

    // Store original policy to restore on disable
    private var originalPolicy: StrictMode.ThreadPolicy? = null

    // Policy installed by enable(), used to detect host changes before restoring
    private var installedPolicy: StrictMode.ThreadPolicy? = null

    // Notification callback
    private var onViolationDetected: ((ThreadViolation) -> Unit)? = null

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
     * Configures the thread violation engine with a notification callback.
     *
     * @param hostPackage Ignored. Any non-system frame marks a violation as relevant, because the
     *   application id (which may carry an applicationIdSuffix) need not match code packages.
     * @param onViolationCallback Optional callback invoked when a violation is detected (for notifications)
     */
    @Suppress("UnusedParameter")
    fun configure(
        hostPackage: String? = null,
        onViolationCallback: ((ThreadViolation) -> Unit)? = null,
    ) {
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

        // Save original policy to restore later. Not built on top of it: penalties apply to every
        // detection, so a host's penaltyDeath() would crash on the disk reads we add.
        originalPolicy = StrictMode.getThreadPolicy()

        val builder = StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()

        // detectCustomSlowCalls available since API 11
        builder.detectCustomSlowCalls()

        // Set up violation listener (API 28+) or fallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.penaltyListener({ it.run() }) { violation ->
                handleViolation(violation)
            }
        } else {
            // Fallback for older devices - use penaltyLog and we won't get callbacks
            // but StrictMode will still log violations to logcat
            builder.penaltyLog()
        }

        val policy = builder.build()
        installedPolicy = policy
        StrictMode.setThreadPolicy(policy)
        _isMonitoring.value = true
    }

    /**
     * Disables thread violation monitoring.
     * Restores the original StrictMode.ThreadPolicy unless the host replaced the
     * policy after [enable], in which case the host's policy is left in place.
     */
    fun disable() {
        if (!_isMonitoring.value) return

        // ThreadPolicy has no equals(); toString() exposes the detect/penalty mask
        val hostChangedPolicy = installedPolicy?.toString() != StrictMode.getThreadPolicy().toString()
        if (!hostChangedPolicy) {
            originalPolicy?.let { StrictMode.setThreadPolicy(it) }
                ?: StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        }
        installedPolicy = null

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
    @RequiresApi(Build.VERSION_CODES.P)
    private fun handleViolation(violation: Violation) {
        // The listener executor runs inline on the violating thread; capture its name before hopping
        val threadName = Thread.currentThread().name
        scope.launch {
            val threadViolation = parseViolation(violation, threadName)
            addViolation(threadViolation)
        }
    }

    /**
     * Parses a StrictMode Violation into our ThreadViolation entity.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun parseViolation(
        violation: Violation,
        threadName: String,
    ): ThreadViolation {
        val timestamp = System.currentTimeMillis()
        val violationType = determineViolationType(violation)
        val description = violation.message ?: violation.javaClass.simpleName
        val stackTrace = violation.stackTrace.map { it.toString() }

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
    @RequiresApi(Build.VERSION_CODES.P)
    private fun determineViolationType(violation: Violation): ThreadViolation.ViolationType {
        return when (violation) {
            is DiskReadViolation -> ThreadViolation.ViolationType.DISK_READ
            is DiskWriteViolation -> ThreadViolation.ViolationType.DISK_WRITE
            is NetworkViolation -> ThreadViolation.ViolationType.NETWORK
            is CustomViolation -> ThreadViolation.ViolationType.CUSTOM_SLOW_CODE
            else -> ThreadViolation.ViolationType.SLOW_CALL
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
    private fun formatDescription(
        type: ThreadViolation.ViolationType,
        originalDescription: String,
    ): String {
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
     * Returns true if any frame comes from outside the system packages (host app,
     * WormaCeptor, or third-party libraries running in the host process).
     */
    @VisibleForTesting
    internal fun isRelevantViolation(violation: ThreadViolation): Boolean {
        return violation.stackTrace.any { frame ->
            val frameText = frame.trimStart()
            systemPackagePrefixes.none { prefix -> frameText.startsWith(prefix) }
        }
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

    /** History size defaults. */
    companion object {
        /** Default history size: 100 violations. */
        const val DEFAULT_HISTORY_SIZE = 100
    }
}
