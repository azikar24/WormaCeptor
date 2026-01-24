/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.azikar24.wormaceptor.domain.contracts.LeakRepository
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Engine that detects memory leaks by tracking Activity and Fragment lifecycle.
 *
 * Uses WeakReference tracking to detect objects that should be garbage collected
 * but are still retained in memory. After onDestroy() is called, a delayed check
 * verifies if the WeakReference has been cleared.
 *
 * Features:
 * - Automatic Activity lifecycle tracking via ActivityLifecycleCallbacks
 * - Configurable check delay after onDestroy
 * - Severity classification based on retained size
 * - Force GC and manual check capability
 * - Thread-safe leak tracking
 */
class LeakDetectionEngine(
    private val checkDelayMs: Long = DEFAULT_CHECK_DELAY_MS,
    private val maxLeakHistory: Int = DEFAULT_MAX_LEAK_HISTORY,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())

    // Tracked objects pending GC verification
    private val pendingChecks = ConcurrentHashMap<String, PendingCheck>()

    // Detected leaks
    private val leaksList = mutableListOf<LeakInfo>()
    private val leaksLock = Any()

    // StateFlows for UI observation
    private val _detectedLeaks = MutableStateFlow<List<LeakInfo>>(emptyList())
    val detectedLeaks: StateFlow<List<LeakInfo>> = _detectedLeaks.asStateFlow()

    private val _leakSummary = MutableStateFlow(LeakSummary.empty())
    val leakSummary: StateFlow<LeakSummary> = _leakSummary.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // Persistence and notification configuration
    private var leakRepository: LeakRepository? = null
    private var onLeakDetected: ((LeakInfo) -> Unit)? = null

    // Activity lifecycle callbacks
    private var application: Application? = null
    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            // Track activity creation for reference path
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (_isRunning.value) {
                trackDestroyedActivity(activity)
            }
        }
    }

    /**
     * Data class representing a pending leak check.
     */
    private data class PendingCheck(
        val id: String,
        val weakRef: WeakReference<Any>,
        val className: String,
        val destroyedAt: Long,
        val description: String,
    )

    /**
     * Starts leak detection monitoring.
     * Registers Activity lifecycle callbacks to track destroyed activities.
     *
     * @param app The application instance to monitor
     */
    fun start(app: Application) {
        if (_isRunning.value) return

        application = app
        app.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        _isRunning.value = true
    }

    /**
     * Stops leak detection monitoring.
     * Unregisters lifecycle callbacks and clears pending checks.
     */
    fun stop() {
        _isRunning.value = false
        application?.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        application = null
        pendingChecks.clear()
    }

    /**
     * Configures the leak detection engine with persistence and notification callbacks.
     *
     * @param repository Optional repository for persisting detected leaks
     * @param onLeakCallback Optional callback invoked when a leak is detected (for notifications)
     */
    fun configure(repository: LeakRepository? = null, onLeakCallback: ((LeakInfo) -> Unit)? = null) {
        this.leakRepository = repository
        this.onLeakDetected = onLeakCallback
    }

    /**
     * Manually triggers a leak check.
     * Forces garbage collection and checks all pending references.
     */
    fun triggerCheck() {
        scope.launch {
            forceGarbageCollection()
            checkPendingReferences()
        }
    }

    /**
     * Clears all detected leaks.
     */
    fun clearLeaks() {
        synchronized(leaksLock) {
            leaksList.clear()
        }
        _detectedLeaks.value = emptyList()
        _leakSummary.value = LeakSummary.empty()
    }

    /**
     * Manually tracks an object for potential leak detection.
     * Useful for tracking Fragments or other objects not automatically tracked.
     *
     * @param obj The object to track
     * @param description Optional description for the tracked object
     */
    fun watchObject(obj: Any, description: String = "") {
        if (!_isRunning.value) return

        val id = UUID.randomUUID().toString()
        val className = obj.javaClass.name
        val desc = description.ifEmpty { "Manually watched: $className" }

        val pendingCheck = PendingCheck(
            id = id,
            weakRef = WeakReference(obj),
            className = className,
            destroyedAt = System.currentTimeMillis(),
            description = desc,
        )

        pendingChecks[id] = pendingCheck
        scheduleLeakCheck(id)
    }

    /**
     * Tracks a destroyed Activity for potential leak detection.
     */
    private fun trackDestroyedActivity(activity: Activity) {
        val id = UUID.randomUUID().toString()
        val className = activity.javaClass.name

        val pendingCheck = PendingCheck(
            id = id,
            weakRef = WeakReference(activity),
            className = className,
            destroyedAt = System.currentTimeMillis(),
            description = "Activity destroyed: $className",
        )

        pendingChecks[id] = pendingCheck
        scheduleLeakCheck(id)
    }

    /**
     * Schedules a delayed leak check for a specific pending reference.
     */
    private fun scheduleLeakCheck(checkId: String) {
        mainHandler.postDelayed({
            scope.launch {
                checkSingleReference(checkId)
            }
        }, checkDelayMs)
    }

    /**
     * Forces garbage collection.
     * Note: This is a best-effort operation - the VM may not immediately collect.
     */
    private fun forceGarbageCollection() {
        System.gc()
        System.runFinalization()
        System.gc()

        // Brief pause to allow GC to complete
        Thread.sleep(GC_WAIT_MS)
    }

    /**
     * Checks a single pending reference for leaks.
     */
    private fun checkSingleReference(checkId: String) {
        val pendingCheck = pendingChecks.remove(checkId) ?: return

        // Force GC before checking
        forceGarbageCollection()

        // If the reference is still alive, it's a leak
        val leakedObject = pendingCheck.weakRef.get()
        if (leakedObject != null) {
            reportLeak(pendingCheck, leakedObject)
        }
    }

    /**
     * Checks all pending references for leaks.
     */
    private fun checkPendingReferences() {
        val checksToProcess = pendingChecks.toMap()
        checksToProcess.forEach { (id, check) ->
            val leakedObject = check.weakRef.get()
            if (leakedObject != null) {
                pendingChecks.remove(id)
                reportLeak(check, leakedObject)
            } else {
                // Object was collected, remove from pending
                pendingChecks.remove(id)
            }
        }
    }

    /**
     * Reports a detected memory leak.
     */
    private fun reportLeak(check: PendingCheck, leakedObject: Any) {
        val retainedSize = estimateRetainedSize(leakedObject)
        val severity = classifySeverity(check.className, retainedSize)
        val referencePath = buildReferencePath(leakedObject)

        val leakInfo = LeakInfo(
            timestamp = System.currentTimeMillis(),
            objectClass = check.className,
            leakDescription = check.description,
            retainedSize = retainedSize,
            referencePath = referencePath,
            severity = severity,
        )

        synchronized(leaksLock) {
            leaksList.add(0, leakInfo)
            // Trim to max size
            while (leaksList.size > maxLeakHistory) {
                leaksList.removeAt(leaksList.size - 1)
            }
            updateStateFlows()
        }

        // Persist to repository if configured
        leakRepository?.let { repo ->
            scope.launch {
                repo.saveLeak(leakInfo)
            }
        }

        // Invoke callback for notifications
        onLeakDetected?.invoke(leakInfo)
    }

    /**
     * Estimates the retained size of an object.
     * This is an approximation based on object type and known patterns.
     */
    private fun estimateRetainedSize(obj: Any): Long {
        return when (obj) {
            is Activity -> ESTIMATED_ACTIVITY_SIZE
            else -> ESTIMATED_DEFAULT_SIZE
        }
    }

    /**
     * Classifies the severity of a leak based on object type and retained size.
     */
    private fun classifySeverity(className: String, retainedSize: Long): LeakSeverity {
        // Activities are always high or critical severity
        val isActivity = className.contains("Activity")

        return when {
            isActivity && retainedSize >= CRITICAL_SIZE_THRESHOLD -> LeakSeverity.CRITICAL
            isActivity -> LeakSeverity.HIGH
            retainedSize >= CRITICAL_SIZE_THRESHOLD -> LeakSeverity.HIGH
            retainedSize >= HIGH_SIZE_THRESHOLD -> LeakSeverity.MEDIUM
            else -> LeakSeverity.LOW
        }
    }

    /**
     * Builds a simplified reference path for the leaked object.
     * Note: Full reference path analysis requires additional tooling.
     *
     * IMPORTANT: This method only uses class metadata (class name, field names, field types)
     * and does NOT access field values via reflection. Accessing field values would cause
     * the leaked object to be retained even longer, exacerbating the memory leak.
     */
    private fun buildReferencePath(obj: Any): List<String> {
        val path = mutableListOf<String>()

        // Capture class info immediately to avoid holding the object reference
        val objClassName = obj.javaClass.simpleName
        val objClass = obj.javaClass
        val isActivity = obj is Activity

        // Add the leaked object class
        path.add("GC Root")

        // Add context hierarchy for Activities
        if (isActivity) {
            path.add("Static reference or callback")
            path.add("-> $objClassName")

            // Check for common leak patterns using only class metadata (NOT instance values)
            // This avoids retaining the activity through reflection access
            objClass.declaredFields.forEach { field ->
                try {
                    if (isLikelyLeakSource(field.name)) {
                        // Only report field name and declared type, not actual value
                        val fieldTypeName = field.type.simpleName
                        path.add("   .${field.name} ($fieldTypeName)")
                    }
                } catch (e: Exception) {
                    // Field access failed, skip
                }
            }
        } else {
            path.add("-> $objClassName")
        }

        path.add("Leaked $objClassName instance")

        return path
    }

    /**
     * Checks if a field name suggests a common leak source.
     */
    private fun isLikelyLeakSource(fieldName: String): Boolean {
        val leakPatterns = listOf(
            "listener",
            "callback",
            "handler",
            "observer",
            "delegate",
            "presenter",
            "viewModel",
            "context",
            "activity",
        )
        return leakPatterns.any { fieldName.contains(it, ignoreCase = true) }
    }

    /**
     * Updates StateFlows with current leak data.
     */
    private fun updateStateFlows() {
        val currentLeaks = leaksList.toList()
        _detectedLeaks.value = currentLeaks
        _leakSummary.value = calculateSummary(currentLeaks)
    }

    /**
     * Calculates summary statistics from leak list.
     */
    private fun calculateSummary(leaks: List<LeakInfo>): LeakSummary {
        if (leaks.isEmpty()) return LeakSummary.empty()

        return LeakSummary(
            totalLeaks = leaks.size,
            criticalCount = leaks.count { it.severity == LeakSeverity.CRITICAL },
            highCount = leaks.count { it.severity == LeakSeverity.HIGH },
            mediumCount = leaks.count { it.severity == LeakSeverity.MEDIUM },
            lowCount = leaks.count { it.severity == LeakSeverity.LOW },
            totalRetainedBytes = leaks.sumOf { it.retainedSize },
        )
    }

    companion object {
        /** Default delay before checking if an object was collected (5 seconds) */
        const val DEFAULT_CHECK_DELAY_MS = 5000L

        /** Default maximum number of leaks to keep in history */
        const val DEFAULT_MAX_LEAK_HISTORY = 100

        /** Wait time after GC to allow collection to complete */
        private const val GC_WAIT_MS = 100L

        /** Estimated retained size for an Activity (~2MB) */
        private const val ESTIMATED_ACTIVITY_SIZE = 2_097_152L

        /** Default estimated size for unknown objects (~256KB) */
        private const val ESTIMATED_DEFAULT_SIZE = 262_144L

        /** Size threshold for critical severity (10MB) */
        private const val CRITICAL_SIZE_THRESHOLD = 10_485_760L

        /** Size threshold for high severity (1MB) */
        private const val HIGH_SIZE_THRESHOLD = 1_048_576L
    }
}
