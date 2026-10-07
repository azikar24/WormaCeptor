package com.azikar24.wormaceptor.mcp.bridge.device

internal sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Reconnecting(val attempt: Int, val nextRetryMs: Long) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
