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
    val seedDatabaseStatus: ToolStatus = ToolStatus.Idle,
    val seedPreferencesStatus: ToolStatus = ToolStatus.Idle,
    val writeFilesStatus: ToolStatus = ToolStatus.Idle,
    val logsStatus: ToolStatus = ToolStatus.Idle,
    val cpuStressStatus: ToolStatus = ToolStatus.Idle,
    val memoryStressStatus: ToolStatus = ToolStatus.Idle,
    val frameDropStatus: ToolStatus = ToolStatus.Idle,
    val recompositionStormActive: Boolean = false,
)
