package com.azikar24.wormaceptorapp.screens.webview

import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator

class WebViewTestViewModel :
    BaseViewModel<WebViewTestViewState, WebViewTestViewEffect, WebViewTestViewEvent, NoOpNavigator>(
        WebViewTestViewState(),
        NoOpNavigator,
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
