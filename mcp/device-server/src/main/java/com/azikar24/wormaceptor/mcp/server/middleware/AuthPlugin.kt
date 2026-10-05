package com.azikar24.wormaceptor.mcp.server.middleware

import com.azikar24.wormaceptor.mcp.server.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.response.respond

internal class AuthConfig {
    var token: String? = null
    var enabled: Boolean = false
}

internal val AuthPlugin = createRouteScopedPlugin("WormaCeptorAuth", ::AuthConfig) {
    val token = pluginConfig.token
    val enabled = pluginConfig.enabled

    if (enabled && token != null) {
        onCall { call ->
            if (call.request.local.uri == "/api/health") return@onCall

            val authHeader = call.request.headers[HttpHeaders.Authorization]
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(error = "Missing or invalid Authorization header"),
                )
                return@onCall
            }
            val providedToken = authHeader.removePrefix("Bearer ").trim()
            if (providedToken != token) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(error = "Invalid token"),
                )
                return@onCall
            }
        }
    }
}
