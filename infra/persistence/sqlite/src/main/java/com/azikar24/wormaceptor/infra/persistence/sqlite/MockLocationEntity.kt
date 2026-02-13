package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.MockLocation

@Entity(tableName = "mock_location")
data class MockLocationEntity(
    @PrimaryKey val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 1.0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val timestamp: Long,
    val name: String? = null,
) {
    fun toDomain() = MockLocation(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        timestamp = timestamp,
        name = name,
    )

    companion object {
        fun fromDomain(location: MockLocation) = MockLocationEntity(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude,
            accuracy = location.accuracy,
            speed = location.speed,
            bearing = location.bearing,
            timestamp = location.timestamp,
            name = location.name,
        )
    }
}
