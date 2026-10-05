package com.azikar24.wormaceptor.mcp.server.serialization

import kotlinx.serialization.json.Json

internal object JsonConfig {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        isLenient = true
        coerceInputValues = true
    }
}
