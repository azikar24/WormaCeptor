package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
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
 * - Severity classification based on object type, retention time, and recurrence
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

    /** All memory leaks detected so far, most recent first. */
    val detectedLeaks: StateFlow<List<LeakInfo>> = _detectedLeaks.asStateFlow()

    private val _leakSummary = MutableStateFlow(LeakSummary.empty())

    /** Aggregated summary of detected leaks by severity. */
    val leakSummary: StateFlow<LeakSummary> = _leakSummary.asStateFlow()

    private val _isRunning = MutableStateFlow(false)

    /** Whether the leak detection engine is actively monitoring. */
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // Persistence and notification configuration
    private var leakRepository: LeakRepository? = null
    private var onLeakDetected: ((LeakInfo) -> Unit)? = null

    // Activity lifecycle callbacks
    private var application: Application? = null
    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?,
        ) = Unit

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle,
        ) = Unit

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
    fun configure(
        repository: LeakRepository? = null,
        onLeakCallback: ((LeakInfo) -> Unit)? = null,
    ) {
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
     * Clears all detected leaks, including persisted ones.
     */
    fun clearLeaks() {
        synchronized(leaksLock) {
            leaksList.clear()
        }
        _detectedLeaks.value = emptyList()
        _leakSummary.value = LeakSummary.empty()
        leakRepository?.let { repo ->
            scope.launch {
                repo.clearLeaks()
            }
        }
    }

    /**
     * Manually tracks an object for potential leak detection.
     * Useful for tracking Fragments or other objects not automatically tracked.
     *
     * @param obj The object to track
     * @param description Optional description for the tracked object
     */
    fun watchObject(
        obj: Any,
        description: String = "",
    ) {
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
     * Checks pending references whose grace period has elapsed.
     * Younger references stay pending for their scheduled check.
     */
    private fun checkPendingReferences() {
        val now = System.currentTimeMillis()
        val checksToProcess = pendingChecks.filterValues { now - it.destroyedAt >= checkDelayMs }
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
    private fun reportLeak(
        check: PendingCheck,
        leakedObject: Any,
    ) {
        val kind = leakedObjectKind(leakedObject)
        val referencePath = buildReferencePath(leakedObject)
        val now = System.currentTimeMillis()

        val leakInfo = synchronized(leaksLock) {
            val severity = classifyLeakSeverity(
                kind = kind,
                retainedMs = now - check.destroyedAt,
                isRecurring = leaksList.any { it.objectClass == check.className },
            )
            // Heap size isn't measured (that needs a heap dump), so no size is reported
            val info = LeakInfo(
                timestamp = now,
                objectClass = check.className,
                leakDescription = check.description,
                retainedSize = 0L,
                referencePath = referencePath,
                severity = severity,
            )
            leaksList.add(0, info)
            // Trim to max size
            while (leaksList.size > maxLeakHistory) {
                leaksList.removeAt(leaksList.size - 1)
            }
            updateStateFlows()
            info
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

    private fun leakedObjectKind(obj: Any): LeakedObjectKind = when {
        obj is Activity -> LeakedObjectKind.ACTIVITY
        obj is View -> LeakedObjectKind.VIEW
        isFragment(obj.javaClass) -> LeakedObjectKind.FRAGMENT
        else -> LeakedObjectKind.OTHER
    }

    // Name-based so core needs no dependency on androidx.fragment
    private fun isFragment(clazz: Class<*>): Boolean = generateSequence<Class<*>>(clazz) { it.superclass }
        .any { it.name == ANDROIDX_FRAGMENT || it.name == PLATFORM_FRAGMENT }

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
                } catch (_: Exception) {
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

    /** Timing and history size defaults. */
    companion object {
        /** Default delay before checking if an object was collected (5 seconds). */
        const val DEFAULT_CHECK_DELAY_MS = 5000L

        /** Default maximum number of leaks to keep in history. */
        const val DEFAULT_MAX_LEAK_HISTORY = 100

        /** Wait time after GC to allow collection to complete. */
        private const val GC_WAIT_MS = 100L

        private const val ANDROIDX_FRAGMENT = "androidx.fragment.app.Fragment"
        private const val PLATFORM_FRAGMENT = "android.app.Fragment"
    }
}

internal enum class LeakedObjectKind { ACTIVITY, FRAGMENT, VIEW, OTHER }

/** Retention past this means the object outlived far more than a normal GC cycle. */
internal const val LongRetentionMs = 30_000L

/**
 * Severity from what leaked and how badly: an Activity pins its whole view tree and resources,
 * a Fragment or View pins part of it, anything else is unknown. A leak that recurs for the same
 * class or stays retained past [LongRetentionMs] is escalated one level.
 */
internal fun classifyLeakSeverity(
    kind: LeakedObjectKind,
    retainedMs: Long,
    isRecurring: Boolean,
): LeakSeverity {
    val base = when (kind) {
        LeakedObjectKind.ACTIVITY -> LeakSeverity.HIGH
        LeakedObjectKind.FRAGMENT, LeakedObjectKind.VIEW -> LeakSeverity.MEDIUM
        LeakedObjectKind.OTHER -> LeakSeverity.LOW
    }
    val escalate = isRecurring || retainedMs >= LongRetentionMs
    return if (escalate) LeakSeverity.entries[base.ordinal + 1] else base
}
