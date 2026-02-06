package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.api.Feature

/**
 * Represents a tool item with its display information and feature mapping.
 */
data class ToolItem(
    val feature: Feature,
    val name: String,
    val icon: ImageVector,
    val route: String,
)

/**
 * Represents a category of tools.
 */
data class ToolCategory(
    val name: String,
    val tools: List<ToolItem>,
)

/**
 * Object containing all tool definitions organized by category.
 */
object ToolCategories {

    /** Tools for inspecting app data like SharedPreferences, databases, and files. */
    val inspectionTools = listOf(
        ToolItem(Feature.SHARED_PREFERENCES, "Shared Preferences", Icons.Default.Settings, "preferences"),
        ToolItem(Feature.DATABASE_BROWSER, "Database Browser", Icons.Default.Storage, "database"),
        ToolItem(Feature.FILE_BROWSER, "File Browser", Icons.Default.Folder, "filebrowser"),
        ToolItem(
            Feature.LOADED_LIBRARIES,
            "Loaded Libraries",
            Icons.AutoMirrored.Filled.LibraryBooks,
            "loadedlibraries",
        ),
        ToolItem(Feature.DEPENDENCIES_INSPECTOR, "Dependencies", Icons.Default.Extension, "dependencies"),
        ToolItem(Feature.SECURE_STORAGE, "Secure Storage", Icons.Default.Security, "securestorage"),
        ToolItem(Feature.WEBVIEW_MONITOR, "WebView Monitor", Icons.Default.Language, "webviewmonitor"),
    )

    /** Tools for monitoring performance metrics like memory, FPS, and CPU usage. */
    val performanceTools = listOf(
        ToolItem(Feature.MEMORY_MONITOR, "Memory Monitor", Icons.Default.Memory, "memory"),
        ToolItem(Feature.FPS_MONITOR, "FPS Monitor", Icons.Default.Speed, "fps"),
        ToolItem(Feature.CPU_MONITOR, "CPU Monitor", Icons.Default.DeveloperBoard, "cpu"),
        ToolItem(Feature.LEAK_DETECTION, "Leak Detection", Icons.Default.BugReport, "leakdetection"),
        ToolItem(Feature.THREAD_VIOLATIONS, "Thread Violations", Icons.Default.Warning, "threadviolation"),
    )

    /** Tools for monitoring network activity like WebSocket connections. */
    val networkTools = listOf(
        ToolItem(Feature.WEBSOCKET_MONITOR, "WebSocket Monitor", Icons.Default.Cable, "websocket"),
        ToolItem(Feature.RATE_LIMITER, "Rate Limiter", Icons.Default.NetworkCheck, "ratelimit"),
    )

    /** Tools for simulating device features like location and push notifications. */
    val simulationTools = listOf(
        ToolItem(Feature.LOCATION_SIMULATOR, "Location Simulator", Icons.Default.LocationOn, "location"),
        ToolItem(Feature.PUSH_SIMULATOR, "Push Simulator", Icons.Default.Notifications, "pushsimulator"),
        ToolItem(Feature.PUSH_TOKEN_MANAGER, "Push Token", Icons.Default.Key, "pushtoken"),
        ToolItem(Feature.CRYPTO_TOOL, "Crypto Tool", Icons.Default.Lock, "crypto"),
    )

    /** Core debugging tools like console logs and device information. */
    val coreTools = listOf(
        ToolItem(Feature.CONSOLE_LOGS, "Console Logs", Icons.Default.Terminal, "logs"),
        ToolItem(Feature.DEVICE_INFO, "Device Info", Icons.Default.Info, "deviceinfo"),
    )

    /** All tool categories in display order. */
    val allCategories = listOf(
        ToolCategory("Inspection", inspectionTools),
        ToolCategory("Performance", performanceTools),
        ToolCategory("Network", networkTools),
        ToolCategory("Simulation", simulationTools),
        ToolCategory("Core", coreTools),
    )

    /** Flattened list of all tools across all categories. */
    val allTools: List<ToolItem> = allCategories.flatMap { it.tools }

    /**
     * Get a tool item by its feature.
     */
    fun getToolByFeature(feature: Feature): ToolItem? = allTools.find { it.feature == feature }

    /**
     * Check if any tools are enabled based on the provided enabled features set.
     * Used to determine whether to show the Tools tab.
     */
    fun hasAnyEnabledTools(enabledFeatures: Set<Feature>): Boolean = allTools.any { it.feature in enabledFeatures }
}
