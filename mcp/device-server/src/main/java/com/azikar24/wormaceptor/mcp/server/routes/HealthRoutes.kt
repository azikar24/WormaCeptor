package com.azikar24.wormaceptor.mcp.server.routes

import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
internal data class HealthResponse(
    val success: Boolean = true,
    val server: String = SERVER_NAME,
    val version: String = SERVER_VERSION,
    val timestamp: Long = System.currentTimeMillis(),
)

private const val SERVER_NAME = "wormaceptor"
private const val SERVER_VERSION = "1.0.0"

internal fun Routing.healthRoutes() {
    get("/api/health") {
        call.respond(HealthResponse())
    }
}
