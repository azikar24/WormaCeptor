package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation

/**
 * Room entity representing a saved location preset for the mock location simulator.
 *
 * @property id Unique identifier for the preset.
 * @property name User-visible display name of the preset.
 * @property latitude Latitude coordinate in decimal degrees.
 * @property longitude Longitude coordinate in decimal degrees.
 * @property altitude Altitude in metres above the WGS-84 ellipsoid.
 * @property accuracy Estimated horizontal accuracy in metres.
 * @property speed Speed in metres per second.
 * @property bearing Bearing in degrees clockwise from north.
 * @property locationName Optional human-readable name for the location.
 * @property isBuiltIn Whether this preset is shipped with the app (non-deletable).
 */
@Entity(tableName = "location_presets")
data class LocationPresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 1.0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val locationName: String? = null,
    val isBuiltIn: Boolean = false,
) {
    /** Converts this entity to a domain [LocationPreset] model. */
    fun toDomain() = LocationPreset(
        id = id,
        name = name,
        location = MockLocation(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            name = locationName,
        ),
        isBuiltIn = isBuiltIn,
    )

    /** Domain-entity conversion factory. */
    companion object {
        /** Creates a [LocationPresetEntity] from a domain [LocationPreset] model. */
        fun fromDomain(preset: LocationPreset) = LocationPresetEntity(
            id = preset.id,
            name = preset.name,
            latitude = preset.location.latitude,
            longitude = preset.location.longitude,
            altitude = preset.location.altitude,
            accuracy = preset.location.accuracy,
            speed = preset.location.speed,
            bearing = preset.location.bearing,
            locationName = preset.location.name,
            isBuiltIn = preset.isBuiltIn,
        )
    }
}
