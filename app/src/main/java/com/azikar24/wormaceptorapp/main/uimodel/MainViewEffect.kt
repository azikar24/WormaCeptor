package com.azikar24.wormaceptorapp.main.uimodel

sealed class MainViewEffect {
    data object OpenWormaCeptor : MainViewEffect()
    data object OpenGitHub : MainViewEffect()

    data object NavigateToLocation : MainViewEffect()
    data object NavigateToWebView : MainViewEffect()
    data object NavigateToSecureStorage : MainViewEffect()

    data object SimulateCrash : MainViewEffect()
    data object TriggerMemoryLeak : MainViewEffect()
    data object TriggerThreadViolation : MainViewEffect()
}
