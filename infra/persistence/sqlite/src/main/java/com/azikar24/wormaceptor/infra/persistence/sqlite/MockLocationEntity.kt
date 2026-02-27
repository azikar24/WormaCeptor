package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.MockLocation

/**
 * Room entity representing the currently active mock location (singleton row).
 *
 * @property id Fixed primary key (always 1) ensuring a single active location row.
 * @property latitude Latitude coordinate in decimal degrees.
 * @property longitude Longitude coordinate in decimal degrees.
 * @property altitude Altitude in metres above the WGS-84 ellipsoid.
 * @property accuracy Estimated horizontal accuracy in metres.
 * @property speed Speed in metres per second.
 * @property bearing Bearing in degrees clockwise from north.
 * @property timestamp Epoch millis when this mock location was set.
 * @property name Optional human-readable name for the location.
 */
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
    /** Converts this entity to a domain [MockLocation] model. */
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

    /** Domain-entity conversion factory. */
    companion object {
        /** Creates a [MockLocationEntity] from a domain [MockLocation] model. */
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
