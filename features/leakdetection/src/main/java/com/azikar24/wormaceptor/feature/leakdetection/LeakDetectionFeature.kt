package com.azikar24.wormaceptor.feature.leakdetection

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.feature.leakdetection.ui.LeakDetectionScreen
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the Memory Leak Detection feature.
 * Provides factory methods and composable entry point.
 *
 * IMPORTANT: This feature should only be used in debug builds.
 * Leak detection adds overhead and should not be included in production.
 */
object LeakDetectionFeature {

    /**
     * Creates a LeakDetectionEngine instance.
     * Use this in your dependency injection setup or as a singleton.
     *
     * @param checkDelayMs Delay in milliseconds before checking if an object was collected
     * @param maxLeakHistory Maximum number of leaks to keep in history
     * @return A new LeakDetectionEngine instance
     */
    fun createEngine(
        checkDelayMs: Long = LeakDetectionEngine.DEFAULT_CHECK_DELAY_MS,
        maxLeakHistory: Int = LeakDetectionEngine.DEFAULT_MAX_LEAK_HISTORY,
    ): LeakDetectionEngine {
        return LeakDetectionEngine(checkDelayMs, maxLeakHistory)
    }

    /**
     * Creates a LeakDetectionViewModel factory for use with viewModel().
     *
     * @param engine The LeakDetectionEngine instance to use
     * @return A ViewModelProvider.Factory for creating LeakDetectionViewModel
     */
    fun createViewModelFactory(engine: LeakDetectionEngine): LeakDetectionViewModelFactory {
        return LeakDetectionViewModelFactory(engine)
    }

    /**
     * Starts leak detection monitoring.
     * Call this in your Application.onCreate() or activity.
     *
     * @param engine The LeakDetectionEngine instance
     * @param application The application instance to monitor
     */
    fun startMonitoring(engine: LeakDetectionEngine, application: Application) {
        engine.start(application)
    }

    /**
     * Stops leak detection monitoring.
     *
     * @param engine The LeakDetectionEngine instance
     */
    fun stopMonitoring(engine: LeakDetectionEngine) {
        engine.stop()
    }

    /**
     * Manually watches an object for potential leaks.
     * Useful for tracking Fragments or other objects not automatically tracked.
     *
     * @param engine The LeakDetectionEngine instance
     * @param obj The object to watch
     * @param description Optional description for the watched object
     */
    fun watchObject(engine: LeakDetectionEngine, obj: Any, description: String = "") {
        engine.watchObject(obj, description)
    }
}

/**
 * Factory for creating LeakDetectionViewModel instances.
 */
class LeakDetectionViewModelFactory(
    private val engine: LeakDetectionEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeakDetectionViewModel::class.java)) {
            return LeakDetectionViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Memory Leak Detection feature.
 * Displays detected memory leaks with filtering and detail capabilities.
 */
@Composable
fun LeakDetector(modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine: LeakDetectionEngine = koinInject()
    val factory = remember(engine) { LeakDetectionFeature.createViewModelFactory(engine) }
    val viewModel: LeakDetectionViewModel = viewModel(factory = factory)

    // Collect state
    val leaks by viewModel.filteredLeaks.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val selectedSeverity by viewModel.selectedSeverity.collectAsState()
    val selectedLeak by viewModel.selectedLeak.collectAsState()

    LeakDetectionScreen(
        leaks = leaks,
        summary = summary,
        isRunning = isRunning,
        selectedSeverity = selectedSeverity,
        selectedLeak = selectedLeak,
        onSeveritySelected = viewModel::setSelectedSeverity,
        onLeakSelected = viewModel::selectLeak,
        onDismissDetail = viewModel::dismissDetail,
        onTriggerCheck = viewModel::triggerCheck,
        onClearLeaks = viewModel::clearLeaks,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
