package com.azikar24.wormaceptor.mcp.server.middleware

import com.azikar24.wormaceptor.mcp.server.ErrorResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.response.respond

private val LOCAL_HOSTNAMES = setOf("localhost", "127.0.0.1", "[::1]")

/**
 * Whether a request came from the MCP bridge (via `adb forward`) rather than a browser.
 *
 * Browsers attach `Origin` to cross-origin requests, so any `Origin` is rejected. The `Host`
 * check blocks DNS rebinding, where an attacker's domain resolves to 127.0.0.1 and the
 * browser then treats the server as same-origin.
 */
internal fun isTrustedLocalRequest(
    host: String?,
    origin: String?,
): Boolean {
    if (origin != null) return false
    val hostname = host?.substringBeforeLast(':', missingDelimiterValue = host)?.lowercase() ?: return false
    return hostname in LOCAL_HOSTNAMES
}

/** Rejects requests that don't come from a local, non-browser client. */
internal val LocalRequestGuard = createApplicationPlugin("WormaCeptorLocalRequestGuard") {
    onCall { call ->
        val headers = call.request.headers
        if (!isTrustedLocalRequest(headers[HttpHeaders.Host], headers[HttpHeaders.Origin])) {
            call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Request not allowed"))
        }
    }
}
