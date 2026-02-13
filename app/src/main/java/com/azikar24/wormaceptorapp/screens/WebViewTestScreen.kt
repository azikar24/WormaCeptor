package com.azikar24.wormaceptorapp.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.azikar24.wormaceptor.api.WormaCeptorWebView
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewTestScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WebView Test",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        WebViewTestContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewTestContent(modifier: Modifier = Modifier) {
    var currentUrl by remember { mutableStateOf("https://httpbin.org/html") }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(
        modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        UrlButtonTopRow(
            webView = webView,
            onUrlChange = { currentUrl = it },
        )
        UrlButtonBottomRow(
            webView = webView,
            onUrlChange = { currentUrl = it },
        )
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WormaCeptorWebView.createMonitoringClient(
                        webViewId = "test_webview",
                        delegate = WebViewClient(),
                    )
                    loadUrl(currentUrl)
                    webView = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md)),
        )
    }
}

@Composable
private fun UrlButtonTopRow(webView: WebView?, onUrlChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Button(
            onClick = {
                onUrlChange("https://httpbin.org/html")
                webView?.loadUrl("https://httpbin.org/html")
            },
            modifier = Modifier.weight(1f),
        ) {
            Text("HTML")
        }
        Button(
            onClick = {
                onUrlChange("https://httpbin.org/json")
                webView?.loadUrl("https://httpbin.org/json")
            },
            modifier = Modifier.weight(1f),
        ) {
            Text("JSON")
        }
        Button(
            onClick = {
                onUrlChange("https://httpbin.org/image/png")
                webView?.loadUrl("https://httpbin.org/image/png")
            },
            modifier = Modifier.weight(1f),
        ) {
            Text("Image")
        }
    }
}

@Composable
private fun UrlButtonBottomRow(webView: WebView?, onUrlChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Button(
            onClick = {
                onUrlChange("https://example.com")
                webView?.loadUrl("https://example.com")
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
}
