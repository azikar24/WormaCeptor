package com.azikar24.wormaceptor.mcp.server

import com.azikar24.wormaceptor.core.engine.McpHolder
import com.azikar24.wormaceptor.core.engine.McpServerHandle
import com.azikar24.wormaceptor.mcp.server.middleware.AuthPlugin
import com.azikar24.wormaceptor.mcp.server.middleware.LocalRequestGuard
import com.azikar24.wormaceptor.mcp.server.routes.actionRoutes
import com.azikar24.wormaceptor.mcp.server.routes.crashRoutes
import com.azikar24.wormaceptor.mcp.server.routes.diagnosticRoutes
import com.azikar24.wormaceptor.mcp.server.routes.healthRoutes
import com.azikar24.wormaceptor.mcp.server.routes.inspectionRoutes
import com.azikar24.wormaceptor.mcp.server.routes.logRoutes
import com.azikar24.wormaceptor.mcp.server.routes.networkRoutes
import com.azikar24.wormaceptor.mcp.server.routes.performanceRoutes
import com.azikar24.wormaceptor.mcp.server.routes.storageRoutes
import com.azikar24.wormaceptor.mcp.server.routes.transactionRoutes
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.streaming.EngineCollector
import com.azikar24.wormaceptor.mcp.server.streaming.EventStreamManager
import com.azikar24.wormaceptor.mcp.server.streaming.streamRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@kotlinx.serialization.Serializable
internal data class ErrorResponse(
    val success: Boolean = false,
    val error: String,
)

internal class WormaCeptorServer(
    private val config: ServerConfig = ServerConfig(),
) : McpServerHandle {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val startTimeMs = MutableStateFlow(0L)
    val uptimeMs: Long
        get() {
            val start = startTimeMs.value
            return if (start > 0) System.currentTimeMillis() - start else 0
        }

    private val collectorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var engineCollector: EngineCollector? = null

    override fun isRunning(): Boolean = _isRunning.value

    override fun start() {
        if (_isRunning.value) return
        try {
            server = embeddedServer(Netty, port = config.port, host = LOCALHOST) {
                configureServer()
            }.start(wait = false)
            startTimeMs.value = System.currentTimeMillis()
            _isRunning.value = true
            McpHolder.registerServer(this)
            startEngineCollector()
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Failed to start MCP server", e)
        }
    }

    override fun stop() {
        try {
            server?.stop(GRACE_PERIOD_MS, TIMEOUT_MS)
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Error stopping server", e)
        }
        server = null
        engineCollector = null
        _isRunning.value = false
        startTimeMs.value = 0
    }

    private fun startEngineCollector() {
        if (ApiCategory.STREAMING !in config.enabledCategories) return
        val eventManager = EventStreamManager()
        engineCollector = EngineCollector(eventManager, collectorScope)
        engineCollector?.startCollecting()
    }

    private fun Application.configureServer() {
        install(ContentNegotiation) {
            json(JsonConfig.instance)
        }

        install(WebSockets) {
            pingPeriodMillis = WEBSOCKET_PING_SECONDS * 1000
            timeoutMillis = WEBSOCKET_TIMEOUT_SECONDS * 1000
            maxFrameSize = Long.MAX_VALUE
        }

        install(StatusPages) {
            exception<Throwable> { call, cause ->
                android.util.Log.w(TAG, "Unhandled route error", cause)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(error = cause.message ?: "Internal error"),
                )
            }
        }

        // No CORS: the only client is the MCP bridge over adb, never a browser.
        install(LocalRequestGuard)

        if (config.enableAuth && config.authToken != null) {
            install(AuthPlugin) {
                enabled = true
                token = config.authToken
            }
        }

        routing {
            healthRoutes()

            if (ApiCategory.NETWORK in config.enabledCategories) {
                transactionRoutes(maxBodySize = config.maxBodySize)
                networkRoutes()
            }

            if (ApiCategory.DIAGNOSTICS in config.enabledCategories) {
                crashRoutes()
                logRoutes()
                diagnosticRoutes()
                inspectionRoutes()
            }

            if (ApiCategory.PERFORMANCE in config.enabledCategories) {
                performanceRoutes()
            }

            if (ApiCategory.STORAGE in config.enabledCategories) {
                storageRoutes()
            }

            if (ApiCategory.ACTIONS in config.enabledCategories) {
                actionRoutes()
            }

            if (ApiCategory.STREAMING in config.enabledCategories) {
                streamRoutes()
            }
        }
    }

    companion object {
        private const val TAG = "WormaCeptorServer"
        private const val LOCALHOST = "127.0.0.1"
        private const val GRACE_PERIOD_MS = 1000L
        private const val TIMEOUT_MS = 2000L
        private const val WEBSOCKET_PING_SECONDS = 15L
        private const val WEBSOCKET_TIMEOUT_SECONDS = 15L
    }
}
