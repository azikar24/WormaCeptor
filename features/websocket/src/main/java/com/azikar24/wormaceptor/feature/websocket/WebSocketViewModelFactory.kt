package com.azikar24.wormaceptor.feature.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.feature.websocket.navigator.WebSocketNavigator
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewModel

class WebSocketViewModelFactory(
    private val engine: WebSocketMonitorEngine,
    private val navigator: WebSocketNavigator,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebSocketViewModel::class.java)) {
            return WebSocketViewModel(engine, navigator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
