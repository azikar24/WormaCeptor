package com.azikar24.wormaceptor.feature.webviewmonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewModel

/**
 * Entry point for the WebView Network Monitoring feature.
 * Provides factory methods and composable entry point.
 */
object WebViewMonitorFeature {

    /**
     * Creates a WebViewMonitorViewModel factory for use with viewModel().
     *
     * @param engine The WebViewMonitorEngine instance to use
     * @return A ViewModelProvider.Factory for creating WebViewMonitorViewModel
     */
    fun createViewModelFactory(engine: WebViewMonitorEngine): WebViewMonitorViewModelFactory {
        return WebViewMonitorViewModelFactory(engine)
    }
}

/**
 * Factory for creating WebViewMonitorViewModel instances.
 */
class WebViewMonitorViewModelFactory(
    private val engine: WebViewMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebViewMonitorViewModel::class.java)) {
            return WebViewMonitorViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
