package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation

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

    companion object {
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
