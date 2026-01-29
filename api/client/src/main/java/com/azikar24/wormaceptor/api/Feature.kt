package com.azikar24.wormaceptor.api

/**
 * Enum representing all available features in WormaCeptor.
 * Developers can configure which features are enabled at init time.
 */
enum class Feature {
    // Inspection
    SHARED_PREFERENCES,
    DATABASE_BROWSER,
    FILE_BROWSER,
    LOADED_LIBRARIES,
    DEPENDENCIES_INSPECTOR,
    SECURE_STORAGE,
    COOKIES_MANAGER,
    WEBVIEW_MONITOR,

    // Performance
    MEMORY_MONITOR,
    FPS_MONITOR,
    CPU_MONITOR,
    LEAK_DETECTION,
    THREAD_VIOLATIONS,

    // Network
    WEBSOCKET_MONITOR,
    RATE_LIMITER,

    // Simulation
    LOCATION_SIMULATOR,
    PUSH_SIMULATOR,
    PUSH_TOKEN_MANAGER,
    CRYPTO_TOOL,

    // Core (always shown in overflow menu, also available in Tools tab)
    CONSOLE_LOGS,
    DEVICE_INFO,
    ;

    companion object {
        /** All available features */
        val ALL: Set<Feature> = entries.toSet()

        /** Default enabled features (all features enabled by default) */
        val DEFAULT: Set<Feature> = ALL

        /** Core features that are always available in overflow menu */
        val CORE: Set<Feature> = setOf(CONSOLE_LOGS, DEVICE_INFO)
    }
}
