package com.azikar24.wormaceptor.api

import android.util.Log
import android.webkit.WebViewClient

private const val TAG = "WormaCeptorWebView"

/**
 * Public API for monitoring WebView network requests with WormaCeptor.
 *
 * Usage:
 * ```kotlin
 * val webView: WebView = ...
 * webView.webViewClient = WormaCeptorWebView.createMonitoringClient(
 *     webViewId = "my_webview",
 *     delegate = myWebViewClient,
 * )
 * ```
 *
 * When the WormaCeptor implementation is not available (e.g. release builds),
 * the delegate is returned as-is (or a no-op [WebViewClient] if no delegate is provided).
 */
object WormaCeptorWebView {

    /**
     * Creates a [WebViewClient] that monitors network requests made by a WebView.
     *
     * All page loads and sub-resource requests will be intercepted and recorded
     * for inspection in WormaCeptor, while being forwarded to the optional [delegate].
     *
     * @param webViewId Unique identifier for the WebView being monitored
     * @param delegate Optional delegate WebViewClient to forward calls to
     * @return A monitoring WebViewClient, or the [delegate] (or a no-op client) if monitoring is unavailable
     */
    fun createMonitoringClient(webViewId: String, delegate: WebViewClient? = null): WebViewClient {
        return try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine",
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            val engine = getMethod.invoke(null, engineClass)

            val enableMethod = engineClass.getMethod("enable")
            enableMethod.invoke(engine)

            val createMethod = engineClass.getMethod(
                "createMonitoringClient",
                String::class.java,
                WebViewClient::class.java,
            )
            createMethod.invoke(engine, webViewId, delegate) as WebViewClient
        } catch (e: ReflectiveOperationException) {
            Log.d(TAG, "WebView monitoring not available: ${e.message}")
            delegate ?: WebViewClient()
        }
    }
}
