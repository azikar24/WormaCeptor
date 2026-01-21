/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a mock location for location simulation.
 *
 * @property latitude The latitude coordinate in degrees (-90 to 90)
 * @property longitude The longitude coordinate in degrees (-180 to 180)
 * @property altitude The altitude in meters above sea level (optional)
 * @property accuracy The horizontal accuracy in meters (optional, defaults to 1.0)
 * @property speed The speed in meters per second (optional)
 * @property bearing The bearing in degrees (0-360, optional)
 * @property timestamp The time of the location fix in milliseconds since epoch
 * @property name Optional name for this location (e.g., "New York City")
 */
data class MockLocation(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 1.0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String? = null,
) {
    /**
     * Validates that the coordinates are within valid ranges.
     */
    fun isValid(): Boolean {
        return latitude in -90.0..90.0 &&
            longitude in -180.0..180.0 &&
            accuracy >= 0 &&
            speed >= 0 &&
            bearing in 0f..360f
    }

    /**
     * Returns a formatted string representation of the coordinates.
     */
    fun formatCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return "%.6f%s, %.6f%s".format(
            kotlin.math.abs(latitude),
            latDir,
            kotlin.math.abs(longitude),
            lonDir,
        )
    }

    companion object {
        /**
         * Creates a MockLocation from latitude and longitude with default values.
         */
        fun from(latitude: Double, longitude: Double, name: String? = null): MockLocation {
            return MockLocation(
                latitude = latitude,
                longitude = longitude,
                name = name,
            )
        }
    }
}

/**
 * Represents a saved location preset for quick access.
 *
 * @property id Unique identifier for the preset
 * @property name Display name for the preset
 * @property location The mock location data
 * @property isBuiltIn Whether this is a built-in preset (cannot be deleted)
 */
data class LocationPreset(
    val id: String,
    val name: String,
    val location: MockLocation,
    val isBuiltIn: Boolean = false,
)
