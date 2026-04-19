package com.azikar24.wormaceptorapp.screens.webview

sealed class WebViewTestViewEffect {
    data class NavigateToUrl(val url: String) : WebViewTestViewEffect()
}
