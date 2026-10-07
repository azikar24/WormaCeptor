package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.mcp.protocol.HealthDto
import com.azikar24.wormaceptor.mcp.server.BuildConfig
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

private const val ServerName = "wormaceptor"

internal fun Routing.healthRoutes(packageName: String? = null) {
    get("/api/health") {
        call.respond(HealthDto(server = ServerName, version = BuildConfig.VERSION_NAME, packageName = packageName))
    }
}
