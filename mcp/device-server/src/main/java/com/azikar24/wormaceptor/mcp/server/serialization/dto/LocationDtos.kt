package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class MockLocationDto(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val speed: Float,
    val bearing: Float,
    val timestamp: Long,
    val name: String?,
)

@Serializable
internal data class LocationPresetDto(
    val id: String,
    val name: String,
    val location: MockLocationDto,
    val isBuiltIn: Boolean,
)
