package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class LeakEntityTest {

    private fun fullLeakInfo() = LeakInfo(
        timestamp = 1_700_000_000_000L,
        objectClass = "com.example.LeakedActivity",
        leakDescription = "Activity was not garbage collected after onDestroy",
        retainedSize = 4096L,
        referencePath = listOf("static field", "handler$0", "activity"),
        severity = LeakSeverity.HIGH,
    )

    private fun fullEntity() = LeakEntity(
        id = 1,
        timestamp = 1_700_000_000_000L,
        objectClass = "com.example.LeakedActivity",
        leakDescription = "Activity was not garbage collected after onDestroy",
        retainedSize = 4096L,
        referencePath = listOf("static field", "handler$0", "activity"),
        severity = "HIGH",
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            domain.timestamp shouldBe 1_700_000_000_000L
            domain.objectClass shouldBe "com.example.LeakedActivity"
            domain.leakDescription shouldBe "Activity was not garbage collected after onDestroy"
            domain.retainedSize shouldBe 4096L
            domain.referencePath shouldBe listOf("static field", "handler$0", "activity")
            domain.severity shouldBe LeakSeverity.HIGH
        }

        @ParameterizedTest
        @EnumSource(LeakSeverity::class)
        fun `maps all severity levels`(severity: LeakSeverity) {
            val entity = fullEntity().copy(severity = severity.name)

            val domain = entity.toDomain()

            domain.severity shouldBe severity
        }

        @Test
        fun `handles empty reference path`() {
            val entity = fullEntity().copy(referencePath = emptyList())

            val domain = entity.toDomain()

            domain.referencePath shouldBe emptyList()
        }

        @Test
        fun `does not include entity ID in domain model`() {
            val entity = fullEntity().copy(id = 99)

            val domain = entity.toDomain()

            // LeakInfo has no id field; the entity id is dropped
            domain.timestamp shouldBe 1_700_000_000_000L
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all fields correctly`() {
            val domain = fullLeakInfo()

            val entity = LeakEntity.fromDomain(domain)

            entity.timestamp shouldBe 1_700_000_000_000L
            entity.objectClass shouldBe "com.example.LeakedActivity"
            entity.leakDescription shouldBe "Activity was not garbage collected after onDestroy"
            entity.retainedSize shouldBe 4096L
            entity.referencePath shouldBe listOf("static field", "handler$0", "activity")
            entity.severity shouldBe "HIGH"
        }

        @Test
        fun `sets auto-generated ID to zero`() {
            val entity = LeakEntity.fromDomain(fullLeakInfo())

            entity.id shouldBe 0
        }

        @ParameterizedTest
        @EnumSource(LeakSeverity::class)
        fun `converts all severity levels to string`(severity: LeakSeverity) {
            val domain = fullLeakInfo().copy(severity = severity)

            val entity = LeakEntity.fromDomain(domain)

            entity.severity shouldBe severity.name
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all domain fields`() {
            val original = fullLeakInfo()

            val entity = LeakEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips with empty reference path`() {
            val original = fullLeakInfo().copy(referencePath = emptyList())

            val entity = LeakEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips with CRITICAL severity`() {
            val original = fullLeakInfo().copy(severity = LeakSeverity.CRITICAL)

            val entity = LeakEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips with LOW severity`() {
            val original = fullLeakInfo().copy(severity = LeakSeverity.LOW)

            val entity = LeakEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }
    }
}
