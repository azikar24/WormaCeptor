package com.azikar24.wormaceptorapp.screens.webview

import com.azikar24.wormaceptor.common.presentation.BaseViewModel

class WebViewTestViewModel : BaseViewModel<WebViewTestViewState, WebViewTestViewEffect, WebViewTestViewEvent>(
    WebViewTestViewState(),
) {

    override fun handleEvent(event: WebViewTestViewEvent) {
        when (event) {
            is WebViewTestViewEvent.LoadUrl -> {
                updateState { copy(currentUrl = event.url) }
                emitEffect(WebViewTestViewEffect.NavigateToUrl(event.url))
            }
        }
    }
}
