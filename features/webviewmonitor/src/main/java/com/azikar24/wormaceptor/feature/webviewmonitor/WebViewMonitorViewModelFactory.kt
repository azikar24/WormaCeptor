package com.azikar24.wormaceptor.feature.webviewmonitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewModel

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
