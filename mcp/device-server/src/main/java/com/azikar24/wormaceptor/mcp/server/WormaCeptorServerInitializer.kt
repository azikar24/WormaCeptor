package com.azikar24.wormaceptor.mcp.server

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.azikar24.wormaceptor.core.engine.McpHolder

/**
 * Registers the server with [McpHolder] and auto-starts it when [McpHolder.config] allows.
 *
 * Runs before `Application.onCreate`, so engines may not exist yet: routes and the stream
 * collector resolve them lazily.
 */
internal class WormaCeptorServerInitializer : ContentProvider() {

    override fun onCreate(): Boolean {
        val server = WormaCeptorServer()
        McpHolder.registerServer(server)
        // Off the main thread; reading the config there also picks up a configureMcpServer() from Application.onCreate.
        Thread({
            if (McpHolder.config.enabled) {
                server.start()
            } else {
                Log.i(TAG, "MCP server auto-start disabled via McpConfig")
            }
        }, "WormaCeptorMCP-start").start()
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
    }
}
