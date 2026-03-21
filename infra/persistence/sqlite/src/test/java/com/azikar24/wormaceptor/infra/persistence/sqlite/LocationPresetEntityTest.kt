package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LocationPresetEntityTest {

    private fun fullPreset() = LocationPreset(
        id = "preset_nyc",
        name = "New York City",
        location = MockLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            altitude = 10.0,
            accuracy = 5.0f,
            speed = 2.5f,
            bearing = 90.0f,
            name = "Manhattan",
        ),
        isBuiltIn = true,
    )

    private fun fullEntity() = LocationPresetEntity(
        id = "preset_nyc",
        name = "New York City",
        latitude = 40.7128,
        longitude = -74.0060,
        altitude = 10.0,
        accuracy = 5.0f,
        speed = 2.5f,
        bearing = 90.0f,
        locationName = "Manhattan",
        isBuiltIn = true,
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            domain.id shouldBe "preset_nyc"
            domain.name shouldBe "New York City"
            domain.location.latitude shouldBe 40.7128
            domain.location.longitude shouldBe -74.0060
            domain.location.altitude shouldBe 10.0
            domain.location.accuracy shouldBe 5.0f
            domain.location.speed shouldBe 2.5f
            domain.location.bearing shouldBe 90.0f
            domain.location.name shouldBe "Manhattan"
            domain.isBuiltIn shouldBe true
        }

        @Test
        fun `handles null location name`() {
            val entity = fullEntity().copy(locationName = null)

            val domain = entity.toDomain()

            domain.location.name.shouldBeNull()
        }

        @Test
        fun `handles default values`() {
            val entity = LocationPresetEntity(
                id = "minimal",
                name = "Minimal",
                latitude = 0.0,
                longitude = 0.0,
            )

            val domain = entity.toDomain()

            domain.location.altitude shouldBe 0.0
            domain.location.accuracy shouldBe 1.0f
            domain.location.speed shouldBe 0f
            domain.location.bearing shouldBe 0f
            domain.isBuiltIn shouldBe false
        }

        @Test
        fun `maps isBuiltIn false`() {
            val entity = fullEntity().copy(isBuiltIn = false)

            val domain = entity.toDomain()

            domain.isBuiltIn shouldBe false
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all fields correctly`() {
            val domain = fullPreset()

            val entity = LocationPresetEntity.fromDomain(domain)

            entity.id shouldBe "preset_nyc"
            entity.name shouldBe "New York City"
            entity.latitude shouldBe 40.7128
            entity.longitude shouldBe -74.0060
            entity.altitude shouldBe 10.0
            entity.accuracy shouldBe 5.0f
            entity.speed shouldBe 2.5f
            entity.bearing shouldBe 90.0f
            entity.locationName shouldBe "Manhattan"
            entity.isBuiltIn shouldBe true
        }

        @Test
        fun `handles null location name`() {
            val domain = fullPreset().copy(
                location = fullPreset().location.copy(name = null),
            )

            val entity = LocationPresetEntity.fromDomain(domain)

            entity.locationName.shouldBeNull()
        }

        @Test
        fun `handles non-built-in preset`() {
            val domain = fullPreset().copy(isBuiltIn = false)

            val entity = LocationPresetEntity.fromDomain(domain)

            entity.isBuiltIn shouldBe false
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all fields`() {
            val original = fullPreset()

            val entity = LocationPresetEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped.id shouldBe original.id
            roundTripped.name shouldBe original.name
            roundTripped.location.latitude shouldBe original.location.latitude
            roundTripped.location.longitude shouldBe original.location.longitude
            roundTripped.location.altitude shouldBe original.location.altitude
            roundTripped.location.accuracy shouldBe original.location.accuracy
            roundTripped.location.speed shouldBe original.location.speed
            roundTripped.location.bearing shouldBe original.location.bearing
            roundTripped.location.name shouldBe original.location.name
            roundTripped.isBuiltIn shouldBe original.isBuiltIn
        }

        @Test
        fun `round-trips preset with null name`() {
            val original = fullPreset().copy(
                location = fullPreset().location.copy(name = null),
            )

            val entity = LocationPresetEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped.location.name.shouldBeNull()
        }

        @Test
        fun `round-trips preset with negative coordinates`() {
            val original = LocationPreset(
                id = "south_west",
                name = "Southern Hemisphere",
                location = MockLocation(
                    latitude = -33.8688,
                    longitude = -151.2093,
                    name = "Somewhere South",
                ),
            )

            val entity = LocationPresetEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped.location.latitude shouldBe -33.8688
            roundTripped.location.longitude shouldBe -151.2093
        }
    }
}
