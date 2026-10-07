package com.azikar24.wormaceptor.mcp.bridge.device

import com.azikar24.wormaceptor.mcp.bridge.util.ProtocolJson
import com.azikar24.wormaceptor.mcp.protocol.HealthDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

internal class DeviceApiClient(
    private val baseUrl: String = "http://localhost:8999",
    private val authToken: String? = null,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
        }
        if (authToken != null) {
            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer $authToken")
            }
        }
    }

    suspend fun get(
        path: String,
        params: Map<String, String> = emptyMap(),
    ): JsonElement {
        val response = httpClient.get("$baseUrl$path") {
            params.forEach { (k, v) -> parameter(k, v) }
        }
        return response.body()
    }

    suspend fun post(
        path: String,
        body: JsonElement? = null,
    ): JsonElement {
        val response = httpClient.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            if (body != null) setBody(body)
        }
        return response.body()
    }

    suspend fun delete(path: String): JsonElement {
        val response = httpClient.delete("$baseUrl$path")
        return response.body()
    }

    /** The health payload, or null when the server didn't answer within [HEALTH_TIMEOUT_MS]. */
    suspend fun healthCheck(): HealthDto? {
        return try {
            val response = httpClient.get("$baseUrl/api/health") {
                timeout { requestTimeoutMillis = HEALTH_TIMEOUT_MS }
            }
            ProtocolJson.decodeFromJsonElement(HealthDto.serializer(), response.body<JsonElement>())
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }
    }

    fun close() = httpClient.close()

    companion object {
        // A frozen app accepts the adb-forwarded connection but never answers: fail fast, then diagnose.
        internal const val REQUEST_TIMEOUT_MS = 10_000L
        internal const val CONNECT_TIMEOUT_MS = 5_000L
        internal const val HEALTH_TIMEOUT_MS = 2_000L
    }
}
