package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.MockLocation
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MockLocationEntityTest {

    private fun fullMockLocation() = MockLocation(
        latitude = 40.7128,
        longitude = -74.0060,
        altitude = 10.0,
        accuracy = 5.0f,
        speed = 2.5f,
        bearing = 90.0f,
        timestamp = 1_700_000_000_000L,
        name = "New York",
    )

    private fun fullEntity() = MockLocationEntity(
        id = 1,
        latitude = 40.7128,
        longitude = -74.0060,
        altitude = 10.0,
        accuracy = 5.0f,
        speed = 2.5f,
        bearing = 90.0f,
        timestamp = 1_700_000_000_000L,
        name = "New York",
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            domain.latitude shouldBe 40.7128
            domain.longitude shouldBe -74.0060
            domain.altitude shouldBe 10.0
            domain.accuracy shouldBe 5.0f
            domain.speed shouldBe 2.5f
            domain.bearing shouldBe 90.0f
            domain.timestamp shouldBe 1_700_000_000_000L
            domain.name shouldBe "New York"
        }

        @Test
        fun `handles null name`() {
            val entity = fullEntity().copy(name = null)

            val domain = entity.toDomain()

            domain.name.shouldBeNull()
        }

        @Test
        fun `handles default values`() {
            val entity = MockLocationEntity(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 0L,
            )

            val domain = entity.toDomain()

            domain.altitude shouldBe 0.0
            domain.accuracy shouldBe 1.0f
            domain.speed shouldBe 0f
            domain.bearing shouldBe 0f
        }

        @Test
        fun `entity id is not part of domain model`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            // MockLocation has no id field; the entity id (always 1) is a Room detail
            domain.latitude shouldBe 40.7128
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all fields correctly`() {
            val domain = fullMockLocation()

            val entity = MockLocationEntity.fromDomain(domain)

            entity.latitude shouldBe 40.7128
            entity.longitude shouldBe -74.0060
            entity.altitude shouldBe 10.0
            entity.accuracy shouldBe 5.0f
            entity.speed shouldBe 2.5f
            entity.bearing shouldBe 90.0f
            entity.timestamp shouldBe 1_700_000_000_000L
            entity.name shouldBe "New York"
        }

        @Test
        fun `handles null name`() {
            val domain = fullMockLocation().copy(name = null)

            val entity = MockLocationEntity.fromDomain(domain)

            entity.name.shouldBeNull()
        }

        @Test
        fun `uses default entity id of 1`() {
            val entity = MockLocationEntity.fromDomain(fullMockLocation())

            entity.id shouldBe 1
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all fields`() {
            val original = fullMockLocation()

            val entity = MockLocationEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips with null name`() {
            val original = fullMockLocation().copy(name = null)

            val entity = MockLocationEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips with zero coordinates`() {
            val original = MockLocation(
                latitude = 0.0,
                longitude = 0.0,
                timestamp = 5000L,
            )

            val entity = MockLocationEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips with negative coordinates`() {
            val original = MockLocation(
                latitude = -33.8688,
                longitude = -151.2093,
                altitude = -5.0,
                timestamp = 1000L,
            )

            val entity = MockLocationEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }
    }
}
