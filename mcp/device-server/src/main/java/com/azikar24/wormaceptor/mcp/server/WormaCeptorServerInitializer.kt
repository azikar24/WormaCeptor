package com.azikar24.wormaceptor.mcp.server

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.azikar24.wormaceptor.core.engine.McpHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

internal class WormaCeptorServerInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            try {
                var attempts = 0
                while (GlobalContext.getOrNull() == null && attempts < MAX_INIT_ATTEMPTS) {
                    delay(INIT_POLL_INTERVAL_MS)
                    attempts++
                }
                if (GlobalContext.getOrNull() == null) {
                    Log.w(
                        TAG,
                        "Koin not initialized after ${MAX_INIT_ATTEMPTS * INIT_POLL_INTERVAL_MS}ms, skipping MCP server start",
                    )
                    return@launch
                }

                val mcpConfig = McpHolder.config
                if (!mcpConfig.enabled) {
                    Log.i(TAG, "MCP server disabled via config")
                    return@launch
                }

                val serverConfig = ServerConfig(
                    port = mcpConfig.port,
                    enableAuth = mcpConfig.enableAuth,
                    authToken = mcpConfig.authToken,
                    maxBodySize = mcpConfig.maxBodySize,
                )
                val server = WormaCeptorServer(serverConfig)
                server.start()
                Log.i(TAG, "WormaCeptor MCP server started on port ${mcpConfig.port}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start MCP server", e)
            }
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0

    companion object {
        private const val TAG = "WormaCeptorMCP"
        private const val MAX_INIT_ATTEMPTS = 50
        private const val INIT_POLL_INTERVAL_MS = 100L
    }
}
