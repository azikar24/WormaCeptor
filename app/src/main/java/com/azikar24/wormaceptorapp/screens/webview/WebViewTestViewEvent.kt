package com.azikar24.wormaceptorapp.screens.webview

sealed class WebViewTestViewEvent {
    data class LoadUrl(val url: String) : WebViewTestViewEvent()
}
