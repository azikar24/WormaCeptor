/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitor
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme

/**
 * Test activity for the WebView Monitor feature.
 * Displays a WebView and the WebView Monitor side by side or switchable.
 */
class WebViewTestActivity : ComponentActivity() {

    private var webViewEngine: WebViewMonitorEngine? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        webViewEngine = WebViewMonitorEngine()
        webViewEngine?.enable()

        setContent {
            WormaCeptorMainTheme {
                var showMonitor by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = if (showMonitor) "WebView Monitor" else "WebView Test",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                }
                            },
                            actions = {
                                OutlinedButton(
                                    onClick = { showMonitor = !showMonitor },
                                ) {
                                    Icon(
                                        imageVector = if (showMonitor) Icons.Default.Language else Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (showMonitor) "WebView" else "Monitor")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    },
                ) { padding ->
                    if (showMonitor) {
                        WebViewMonitor(
                            engine = webViewEngine!!,
                            onNavigateBack = { showMonitor = false },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        )
                    } else {
                        WebViewTestContent(
                            engine = webViewEngine!!,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewEngine = null
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewTestContent(engine: WebViewMonitorEngine, modifier: Modifier = Modifier) {
    var currentUrl by remember { mutableStateOf("https://httpbin.org/html") }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        // URL buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Button(
                onClick = {
                    currentUrl = "https://httpbin.org/html"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("HTML")
            }
            Button(
                onClick = {
                    currentUrl = "https://httpbin.org/json"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("JSON")
            }
            Button(
                onClick = {
                    currentUrl = "https://httpbin.org/image/png"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Image")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Button(
                onClick = {
                    currentUrl = "https://example.com"
                    webView?.loadUrl(currentUrl)
                },
                modifier = Modifier.weight(1f),
            ) {
                Text("Example")
            }
            IconButton(
                onClick = { webView?.reload() },
            ) {
                Icon(Icons.Default.Refresh, "Reload")
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // WebView
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = engine.createMonitoringClient(
                        webViewId = "test_webview",
                        delegate = WebViewClient(),
                    )
                    loadUrl(currentUrl)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}
