package com.azikar24.wormaceptorapp.main.uimodel

import com.azikar24.wormaceptorapp.wormaceptorui.components.ToolStatus

data class MainViewState(
    val showCrashDialog: Boolean = false,
    val isGlitchEffectActive: Boolean = false,
    val showTestToolsSheet: Boolean = false,
    val apiTestStatus: ToolStatus = ToolStatus.Idle,
    val webSocketStatus: ToolStatus = ToolStatus.Idle,
    val leakStatus: ToolStatus = ToolStatus.Idle,
    val threadViolationStatus: ToolStatus = ToolStatus.Idle,
)
