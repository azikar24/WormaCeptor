package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

internal object ToolRegistry {

    fun allTools(): List<McpTool> = listOf(
        // Network (8)
        ListTransactionsTool(),
        GetTransactionTool(),
        GetRequestBodyTool(),
        GetResponseBodyTool(),
        ListWebSocketConnectionsTool(),
        ListWebSocketMessagesTool(),
        SetRateLimitTool(),
        GetRateLimitTool(),
        // Diagnostics (6)
        ListCrashesTool(),
        GetCrashTool(),
        TailLogsTool(),
        ListLeaksTool(),
        ListViolationsTool(),
        GetDeviceInfoTool(),
        // Performance (5)
        GetCpuStatsTool(),
        GetMemoryStatsTool(),
        GetFpsStatsTool(),
        GetPerformanceSnapshotTool(),
        SetMonitoringTool(),
        // Storage (8)
        ListPreferencesTool(),
        ListDatabasesTool(),
        QueryDatabaseTool(),
        ListFilesTool(),
        ReadFileTool(),
        BrowseSecureStorageTool(),
        ListDependenciesTool(),
        ListLoadedLibrariesTool(),
        // Actions (7)
        ClearTransactionsTool(),
        ClearCrashesTool(),
        ClearLogsTool(),
        SimulateLocationTool(),
        StopLocationSimulationTool(),
        SendPushNotificationTool(),
        BringAppToFrontTool(),
        // Timeline and events (1)
        GetTimelineTool(),
    )
}
