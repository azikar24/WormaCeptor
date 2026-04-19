package com.azikar24.wormaceptorapp.screens.webview

data class WebViewTestViewState(
    val currentUrl: String = DEFAULT_URL,
) {
    companion object {
        const val DEFAULT_URL = "https://azikar24.com"
        const val HTML_URL = "https://httpbin.org/html"
        const val JSON_URL = "https://httpbin.org/json"
        const val IMAGE_URL = "https://httpbin.org/image/jpeg"
    }
}
