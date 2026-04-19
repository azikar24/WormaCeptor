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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.azikar24.wormaceptor.api.WormaCeptorWebView
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptorapp.screens.webview.WebViewTestViewEffect
import com.azikar24.wormaceptorapp.screens.webview.WebViewTestViewEvent
import com.azikar24.wormaceptorapp.screens.webview.WebViewTestViewModel
import com.azikar24.wormaceptorapp.screens.webview.WebViewTestViewState

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewTestScreen(
    viewModel: WebViewTestViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is WebViewTestViewEffect.NavigateToUrl -> webViewRef?.loadUrl(effect.url)
            }
        },
    ) { state, onEvent ->
        WebViewTestScreenContent(
            state = state,
            onBack = onBack,
            onEvent = onEvent,
            onCreateWebView = { webViewRef = it },
            modifier = modifier,
        )
    }
}

@Composable
private fun WebViewTestScreenContent(
    state: WebViewTestViewState,
    onBack: () -> Unit,
    onEvent: (WebViewTestViewEvent) -> Unit,
    onCreateWebView: ((WebView) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            WormaCeptorTopBar(
                title = "WebView Test",
                onBack = onBack,
                backContentDescription = "Back",
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WormaCeptorTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            UrlButtons(onEvent = onEvent)

            if (onCreateWebView != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            @SuppressLint("SetJavaScriptEnabled")
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WormaCeptorWebView.createMonitoringClient(
                                webViewId = "test_webview",
                                delegate = WebViewClient(),
                            )
                            loadUrl(state.currentUrl)
                            onCreateWebView(this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(WormaCeptorTokens.Shapes.card),
                )
            }
        }
    }
}

@Composable
private fun UrlButtons(onEvent: (WebViewTestViewEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        WormaCeptorButton(
            text = "HTML",
            onClick = { onEvent(WebViewTestViewEvent.LoadUrl(WebViewTestViewState.HTML_URL)) },
            modifier = Modifier.weight(1f),
        )
        WormaCeptorButton(
            text = "JSON",
            onClick = { onEvent(WebViewTestViewEvent.LoadUrl(WebViewTestViewState.JSON_URL)) },
            modifier = Modifier.weight(1f),
        )
        WormaCeptorButton(
            text = "Image",
            onClick = { onEvent(WebViewTestViewEvent.LoadUrl(WebViewTestViewState.IMAGE_URL)) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WebViewTestScreenPreview() {
    WormaCeptorTheme {
        WebViewTestScreenContent(
            state = WebViewTestViewState(),
            onBack = {},
            onEvent = {},
            onCreateWebView = null,
        )
    }
}
