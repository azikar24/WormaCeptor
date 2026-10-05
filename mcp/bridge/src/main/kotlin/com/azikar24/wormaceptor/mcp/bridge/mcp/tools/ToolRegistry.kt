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
        // Performance (4)
        GetCpuStatsTool(),
        GetMemoryStatsTool(),
        GetFpsStatsTool(),
        GetPerformanceSnapshotTool(),
        // Storage (8)
        ListPreferencesTool(),
        ListDatabasesTool(),
        QueryDatabaseTool(),
        ListFilesTool(),
        ReadFileTool(),
        BrowseSecureStorageTool(),
        ListDependenciesTool(),
        ListLoadedLibrariesTool(),
        // Actions (6)
        ClearTransactionsTool(),
        ClearCrashesTool(),
        ClearLogsTool(),
        SimulateLocationTool(),
        StopLocationSimulationTool(),
        SendPushNotificationTool(),
    )
}
