package com.azikar24.wormaceptor.core.ui.navigation

/**
 * Centralized route constants for all navigation destinations.
 * Multi-screen features have both a graph route and individual screen routes.
 */
object WormaCeptorNavKeys {

    /** Home screen with tabs (transactions, crashes, tools). */
    object Home {
        /** Route string for the home screen. */
        const val route = "home"
    }

    /** Transaction detail pager screen. */
    object TransactionDetail {
        /** Route template with `{id}` placeholder. */
        const val route = "transactions/detail/{id}"

        /** Builds a concrete route for the given transaction [id]. */
        fun createRoute(id: String) = "transactions/detail/$id"
    }

    /** Crash detail pager screen. */
    object CrashDetail {
        /** Route template with `{timestamp}` placeholder. */
        const val route = "crashes/detail/{timestamp}"

        /** Builds a concrete route for the given crash [timestamp]. */
        fun createRoute(timestamp: Long) = "crashes/detail/$timestamp"
    }

    /** Preferences Inspector nested graph. */
    object Preferences {
        /** Graph route for the preferences feature. */
        const val route = "preferences"
    }

    /** Preferences file list screen. */
    object PreferencesList {
        /** Route string for the preferences file list. */
        const val route = "preferences/list"
    }

    /** Preference items detail screen. */
    object PreferencesDetail {
        /** Route string for the preference items detail. */
        const val route = "preferences/detail"
    }

    /** Database Browser nested graph. */
    object Database {
        /** Graph route for the database browser feature. */
        const val route = "database"
    }

    /** Database list screen. */
    object DatabaseList {
        /** Route string for the database list. */
        const val route = "database/list"
    }

    /** Table list screen within a database. */
    object DatabaseTables {
        /** Route string for the table list. */
        const val route = "database/tables"
    }

    /** Table data screen. */
    object DatabaseTableData {
        /** Route string for the table data viewer. */
        const val route = "database/data"
    }

    /** SQL query editor screen. */
    object DatabaseQuery {
        /** Route string for the SQL query editor. */
        const val route = "database/query"
    }

    /** WebSocket Monitor nested graph. */
    object WebSocket {
        /** Graph route for the WebSocket monitor feature. */
        const val route = "websocket"
    }

    /** WebSocket connections list screen. */
    object WebSocketConnections {
        /** Route string for the WebSocket connections list. */
        const val route = "websocket/connections"
    }

    /** WebSocket messages detail screen. */
    object WebSocketMessages {
        /** Route string for the WebSocket messages detail. */
        const val route = "websocket/messages"
    }

    /** Console log viewer. */
    object Logs {
        /** Route string for the console log viewer. */
        const val route = "logs"
    }

    /** Device information screen. */
    object DeviceInfo {
        /** Route string for the device information screen. */
        const val route = "deviceinfo"
    }

    /** File browser screen. */
    object FileBrowser {
        /** Route string for the file browser. */
        const val route = "filebrowser"
    }

    /** Memory monitor screen. */
    object Memory {
        /** Route string for the memory monitor. */
        const val route = "memory"
    }

    /** FPS monitor screen. */
    object Fps {
        /** Route string for the FPS monitor. */
        const val route = "fps"
    }

    /** CPU monitor screen. */
    object Cpu {
        /** Route string for the CPU monitor. */
        const val route = "cpu"
    }

    /** Location simulator screen. */
    object Location {
        /** Route string for the location simulator. */
        const val route = "location"
    }

    /** Push notification simulator screen. */
    object PushSimulator {
        /** Route string for the push notification simulator. */
        const val route = "pushsimulator"
    }

    /** Leak detection screen. */
    object LeakDetection {
        /** Route string for the leak detection screen. */
        const val route = "leakdetection"
    }

    /** Thread violation monitor screen. */
    object ThreadViolation {
        /** Route string for the thread violation monitor. */
        const val route = "threadviolation"
    }

    /** WebView monitor screen. */
    object WebViewMonitor {
        /** Route string for the WebView monitor. */
        const val route = "webviewmonitor"
    }

    /** Crypto tool screen. */
    object Crypto {
        /** Route string for the crypto tool. */
        const val route = "crypto"
    }

    /** Secure storage viewer screen. */
    object SecureStorage {
        /** Route string for the secure storage viewer. */
        const val route = "securestorage"
    }

    /** Rate limiter screen. */
    object RateLimit {
        /** Route string for the rate limiter. */
        const val route = "ratelimit"
    }

    /** Push token manager screen. */
    object PushToken {
        /** Route string for the push token manager. */
        const val route = "pushtoken"
    }

    /** Loaded libraries inspector screen. */
    object LoadedLibraries {
        /** Route string for the loaded libraries inspector. */
        const val route = "loadedlibraries"
    }

    /** Dependencies inspector screen. */
    object Dependencies {
        /** Route string for the dependencies inspector. */
        const val route = "dependencies"
    }
}
