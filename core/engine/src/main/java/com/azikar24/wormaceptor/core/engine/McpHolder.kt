package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.McpConfig
import java.util.concurrent.atomic.AtomicReference

/** Holds the MCP server configuration and the running server, so the host API can control it. */
object McpHolder {
    private val _config = AtomicReference(McpConfig())
    private val _server = AtomicReference<McpServerHandle?>(null)

    /** Current server configuration. */
    val config: McpConfig
        get() = _config.get()

    /** Replaces the server configuration; takes effect on the next start. */
    fun configure(config: McpConfig) {
        _config.set(config)
    }

    /** Registers the server implementation, called by the debug-only device-server module. */
    fun registerServer(handle: McpServerHandle) {
        _server.set(handle)
    }

    /** Whether a registered server is running. */
    fun isRunning(): Boolean = _server.get()?.isRunning() ?: false

    /** Starts the registered server, if any. */
    fun start() {
        _server.get()?.start()
    }

    /** Stops the registered server, if any. */
    fun stop() {
        _server.get()?.stop()
    }

    /** Stops the server, then starts it again with the current [config] unless that config is disabled. */
    fun restart() {
        stop()
        if (config.enabled) start()
    }
}

/** Control surface the device-server module exposes to [McpHolder]. */
interface McpServerHandle {
    /** Whether the server is running. */
    fun isRunning(): Boolean

    /** Starts the server. */
    fun start()

    /** Stops the server. */
    fun stop()
}
