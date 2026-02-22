package com.azikar24.wormaceptorapp.main.uimodel

sealed class MainViewEvent {
    data object LaunchWormaCeptorClicked : MainViewEvent()
    data object GitHubClicked : MainViewEvent()
    data object TestToolsClicked : MainViewEvent()

    data object RunApiTestsClicked : MainViewEvent()
    data object WebSocketTestClicked : MainViewEvent()

    data object TriggerCrashClicked : MainViewEvent()
    data object TriggerLeakClicked : MainViewEvent()
    data object ThreadViolationClicked : MainViewEvent()

    data object LocationClicked : MainViewEvent()
    data object WebViewClicked : MainViewEvent()
    data object SecureStorageClicked : MainViewEvent()

    data object CrashConfirmed : MainViewEvent()
    data object CrashDialogDismissed : MainViewEvent()
    data object TestToolsSheetDismissed : MainViewEvent()

    data object CheckLeakRotation : MainViewEvent()
    data object GlitchAnimationCompleted : MainViewEvent()
}
