package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class MockLocationDto(
    val latitude: Double,
    val longitude: Double,
    // Defaults mirror MockLocation; the bridge only sends latitude, longitude, altitude and name.
    val altitude: Double = 0.0,
    val accuracy: Float = 1.0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String? = null,
)

@Serializable
internal data class LocationPresetDto(
    val id: String,
    val name: String,
    val location: MockLocationDto,
    val isBuiltIn: Boolean,
)
