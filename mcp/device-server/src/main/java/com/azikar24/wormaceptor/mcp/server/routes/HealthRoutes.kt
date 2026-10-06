package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.mcp.server.BuildConfig
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable

@Serializable
internal data class HealthResponse(
    val success: Boolean = true,
    val server: String = SERVER_NAME,
    val version: String = BuildConfig.VERSION_NAME,
    val timestamp: Long = System.currentTimeMillis(),
)

private const val SERVER_NAME = "wormaceptor"

internal fun Routing.healthRoutes() {
    get("/api/health") {
        call.respond(HealthResponse())
    }
}
