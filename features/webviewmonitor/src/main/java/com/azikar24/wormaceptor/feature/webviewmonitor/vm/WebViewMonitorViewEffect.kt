package com.azikar24.wormaceptor.feature.webviewmonitor.vm

/** One-time side effects emitted by [WebViewMonitorViewModel] and consumed by the UI. */
sealed class WebViewMonitorViewEffect {
    data object NavigateToDetail : WebViewMonitorViewEffect()
}
