package com.azikar24.wormaceptor.mcp.bridge.device

import com.azikar24.wormaceptor.mcp.bridge.adb.AdbClient
import com.azikar24.wormaceptor.mcp.bridge.config.BridgeConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class DeviceConnection(
    private val config: BridgeConfig,
    private val adbClient: AdbClient,
) {
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    val apiClient = DeviceApiClient(
        baseUrl = "http://localhost:${config.port}",
        authToken = config.authToken,
    )

    suspend fun connect() {
        _state.value = ConnectionState.Connecting

        var attempts = 0
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                if (apiClient.healthCheck()) {
                    _state.value = ConnectionState.Connected
                    return
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            attempts++
            delay(HEALTH_CHECK_TIMEOUT_MS)
        }

        val errorMsg = "Could not connect to WormaCeptor server on port ${config.port}. " +
            "Ensure the app is running with mcp-device-server dependency."
        _state.value = ConnectionState.Error(errorMsg)
        throw IllegalStateException(errorMsg)
    }

    suspend fun reconnect(maxRetries: Int = MAX_RETRY_ATTEMPTS) {
        var retryDelay = INITIAL_RETRY_DELAY_MS
        var attempt = 1
        while (attempt <= maxRetries) {
            _state.value = ConnectionState.Reconnecting(attempt, retryDelay)
            System.err.println("Reconnecting (attempt $attempt/$maxRetries, next retry in ${retryDelay}ms)...")
            delay(retryDelay)
            try {
                adbClient.forwardPort(config.deviceSerial, config.port, config.port)
                connect()
                return
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                retryDelay = (retryDelay * 2).coerceAtMost(MAX_RETRY_DELAY_MS)
                attempt++
            }
        }

        val errorMsg = "Reconnection failed after $maxRetries attempts"
        System.err.println(errorMsg)
        _state.value = ConnectionState.Error(errorMsg)
    }

    fun close() {
        apiClient.close()
        adbClient.removeForward(config.port)
        _state.value = ConnectionState.Disconnected
    }

    companion object {
        internal const val INITIAL_RETRY_DELAY_MS = 1_000L
        internal const val MAX_RETRY_DELAY_MS = 30_000L
        internal const val MAX_RETRY_ATTEMPTS = 20
        internal const val HEALTH_CHECK_TIMEOUT_MS = 500L
    }
}
