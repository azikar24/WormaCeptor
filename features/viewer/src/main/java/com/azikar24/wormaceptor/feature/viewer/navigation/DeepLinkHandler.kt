package com.azikar24.wormaceptor.feature.viewer.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Handles deep link navigation for the WormaCeptor viewer.
 *
 * Supported deep links:
 * - wormaceptor://tools - Opens the Tools tab
 * - wormaceptor://tools/memory - Opens the Memory Monitor screen
 * - wormaceptor://tools/fps - Opens the FPS Monitor screen
 * - wormaceptor://tools/cpu - Opens the CPU Monitor screen
 * - wormaceptor://transactions - Opens the Transactions tab
 * - wormaceptor://crashes - Opens the Crashes tab
 */
object DeepLinkHandler {

    const val SCHEME = "wormaceptor"
    private const val HOST_TOOLS = "tools"
    private const val HOST_TRANSACTIONS = "transactions"
    private const val HOST_CRASHES = "crashes"

    /**
     * Represents a navigation destination parsed from a deep link.
     */
    sealed class DeepLinkDestination {
        /** Navigate to the home screen with a specific tab selected */
        data class Tab(val tabIndex: Int) : DeepLinkDestination()

        /** Navigate directly to a tool screen */
        data class Tool(val route: String) : DeepLinkDestination()

        /** Invalid or unrecognized deep link */
        data object Invalid : DeepLinkDestination()

        companion object {
            const val TAB_TRANSACTIONS = 0
            const val TAB_CRASHES = 1
            const val TAB_TOOLS = 2
        }
    }

    /**
     * Parses a deep link URI and returns the corresponding navigation destination.
     *
     * @param uri The deep link URI to parse
     * @return The navigation destination, or [DeepLinkDestination.Invalid] if unrecognized
     */
    fun parseDeepLink(uri: Uri?): DeepLinkDestination {
        if (uri == null) return DeepLinkDestination.Invalid
        if (uri.scheme != SCHEME) return DeepLinkDestination.Invalid

        val host = uri.host ?: return DeepLinkDestination.Invalid
        val pathSegments = uri.pathSegments

        return when (host) {
            HOST_TRANSACTIONS -> DeepLinkDestination.Tab(DeepLinkDestination.TAB_TRANSACTIONS)
            HOST_CRASHES -> DeepLinkDestination.Tab(DeepLinkDestination.TAB_CRASHES)
            HOST_TOOLS -> parseToolsDeepLink(pathSegments)
            else -> DeepLinkDestination.Invalid
        }
    }

    /**
     * Parses a deep link from an Intent.
     *
     * @param intent The intent containing the deep link data
     * @return The navigation destination, or [DeepLinkDestination.Invalid] if unrecognized
     */
    fun parseDeepLink(intent: Intent?): DeepLinkDestination {
        return parseDeepLink(intent?.data)
    }

    private fun parseToolsDeepLink(pathSegments: List<String>): DeepLinkDestination {
        if (pathSegments.isEmpty()) {
            // wormaceptor://tools - just open the Tools tab
            return DeepLinkDestination.Tab(DeepLinkDestination.TAB_TOOLS)
        }

        // Map deep link paths to navigation routes
        val route = when (pathSegments[0].lowercase()) {
            "memory" -> "memory"
            "fps" -> "fps"
            "cpu" -> "cpu"
            "preferences", "sharedpreferences" -> "preferences"
            "database" -> "database"
            "filebrowser", "files" -> "filebrowser"
            "websocket" -> "websocket"
            "cookies" -> "cookies"
            "touchviz", "touch" -> "touchviz"
            "viewborders", "borders" -> "viewborders"
            "location" -> "location"
            "pushsimulator", "push" -> "pushsimulator"
            "leakdetection", "leaks" -> "leakdetection"
            "threadviolation", "threads" -> "threadviolation"
            "webviewmonitor", "webview" -> "webviewmonitor"
            "crypto" -> "crypto"
            "gridoverlay", "grid" -> "gridoverlay"
            "measurement", "measure" -> "measurement"
            "securestorage", "secure" -> "securestorage"
            "ratelimit", "rate" -> "ratelimit"
            "pushtoken", "token" -> "pushtoken"
            "loadedlibraries", "libraries" -> "loadedlibraries"
            "dependencies", "deps" -> "dependencies"
            "logs", "console" -> "logs"
            "deviceinfo", "device" -> "deviceinfo"
            else -> null
        }

        return if (route != null) {
            DeepLinkDestination.Tool(route)
        } else {
            // Unknown tool path - fall back to Tools tab
            DeepLinkDestination.Tab(DeepLinkDestination.TAB_TOOLS)
        }
    }

    /**
     * Creates an Intent to open the ViewerActivity with a specific deep link.
     *
     * @param context The context to use for creating the intent
     * @param deepLinkUri The deep link URI string (e.g., "wormaceptor://tools/memory")
     * @return An Intent configured to open the ViewerActivity with the deep link
     */
    fun createIntent(context: Context, deepLinkUri: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUri)).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates a deep link URI for a specific tool.
     *
     * @param toolPath The tool path (e.g., "memory", "fps", "cpu")
     * @return The deep link URI string
     */
    fun createToolDeepLink(toolPath: String): String {
        return "$SCHEME://$HOST_TOOLS/$toolPath"
    }

    /**
     * Creates a deep link URI to open the Tools tab.
     */
    fun createToolsTabDeepLink(): String {
        return "$SCHEME://$HOST_TOOLS"
    }

    /**
     * Creates a deep link URI to open the Transactions tab.
     */
    fun createTransactionsTabDeepLink(): String {
        return "$SCHEME://$HOST_TRANSACTIONS"
    }

    /**
     * Creates a deep link URI to open the Crashes tab.
     */
    fun createCrashesTabDeepLink(): String {
        return "$SCHEME://$HOST_CRASHES"
    }
}
