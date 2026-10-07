package com.azikar24.wormaceptor.mcp.bridge.util

import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Decodes device-server responses into the shared DTOs and encodes request DTOs.
 * Defaults aren't encoded, so request bodies carry only the fields a tool sets
 * and the device server fills in the rest.
 */
internal val ProtocolJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = false
}

internal fun JsonElement.toApiResponse(): ApiResponse =
    ProtocolJson.decodeFromJsonElement(ApiResponse.serializer(), this)

/** The typed `data` payload, or null when the server sent none. A shape mismatch throws. */
internal inline fun <reified T> ApiResponse.dataAs(): T? =
    data?.takeUnless { it is JsonNull }?.let { ProtocolJson.decodeFromJsonElement<T>(it) }

/** The device server's `error` message, so a failed call says why instead of "not found". */
internal fun ApiResponse.errorText(): String? = error?.let { "Error: $it" }

internal inline fun <reified T> T.toRequestBody(): JsonElement = ProtocolJson.encodeToJsonElement(this)
