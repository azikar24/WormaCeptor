package com.azikar24.wormaceptor.api

/**
 * Enum representing all available features in WormaCeptor.
 *
 * Use this enum to selectively enable or disable features at initialization time.
 * By default, all features are enabled. You can create a custom feature set:
 *
 * ```kotlin
 * WormaCeptorApi.init(
 *     context = applicationContext,
 *     features = setOf(Feature.MEMORY_MONITOR, Feature.DATABASE_BROWSER)
 * )
 * ```
 */
enum class Feature {
    // ========== Inspection Features ==========

    /** Browses and edits SharedPreferences files. */
    SHARED_PREFERENCES,

    /** Browses SQLite databases and executes queries. */
    DATABASE_BROWSER,

    /** Browses application files and directories. */
    FILE_BROWSER,

    /** Views loaded native libraries. */
    LOADED_LIBRARIES,

    /** Inspects Gradle dependencies and versions. */
    DEPENDENCIES_INSPECTOR,

    /** Browses encrypted SharedPreferences (EncryptedSharedPreferences). */
    SECURE_STORAGE,

    /** Monitors WebView loading and JavaScript execution. */
    WEBVIEW_MONITOR,

    // ========== Performance Features ==========

    /** Monitors memory usage with real-time charts. */
    MEMORY_MONITOR,

    /** Monitors frame rate and jank detection. */
    FPS_MONITOR,

    /** Monitors CPU usage per core. */
    CPU_MONITOR,

    /** Detects memory leaks using LeakCanary integration. */
    LEAK_DETECTION,

    /** Detects StrictMode violations for disk and network operations on main thread. */
    THREAD_VIOLATIONS,

    // ========== Network Features ==========

    /** Monitors WebSocket connections and messages. */
    WEBSOCKET_MONITOR,

    /** Tests rate limiting behavior by artificially slowing requests. */
    RATE_LIMITER,

    // ========== Simulation Features ==========

    /** Mocks GPS location for testing location-based features. */
    LOCATION_SIMULATOR,

    /** Sends simulated push notifications for testing. */
    PUSH_SIMULATOR,

    /** Views and copies FCM/push notification tokens. */
    PUSH_TOKEN_MANAGER,

    /** Provides cryptographic tools for hashing, encoding, and encryption testing. */
    CRYPTO_TOOL,

    // ========== Core Features ==========

    /** Views application logs (Logcat). */
    CONSOLE_LOGS,

    /** Views device and application information. */
    DEVICE_INFO,
    ;

    /** Predefined feature sets for common configurations. */
    @Suppress("unused")
    companion object {
        /**
         * All available features.
         * Use this to enable every feature in WormaCeptor.
         */
        val ALL: Set<Feature> = entries.toSet()

        /**
         * Default enabled features.
         * Currently includes all features. Modify this to change the default behavior.
         */
        val DEFAULT: Set<Feature> = ALL

        /**
         * Core features that are always available in the overflow menu.
         * These features are lightweight and commonly used for quick access.
         */
        val CORE: Set<Feature> = setOf(CONSOLE_LOGS, DEVICE_INFO)
    }
}
