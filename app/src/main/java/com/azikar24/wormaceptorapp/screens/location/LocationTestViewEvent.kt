package com.azikar24.wormaceptorapp.screens.location

sealed class LocationTestViewEvent {
    data object PermissionResult : LocationTestViewEvent()
    data object OpenMockLocationTool : LocationTestViewEvent()
    data object ScreenResumed : LocationTestViewEvent()
    data object ScreenPaused : LocationTestViewEvent()
}
