package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RateLimitConfigTest {

    @Nested
    inner class DefaultFactory {

        @Test
        fun `default config is disabled`() {
            val config = RateLimitConfig.default()

            config.enabled shouldBe false
        }

        @Test
        fun `default config has WIFI-level speeds`() {
            val config = RateLimitConfig.default()

            config.downloadSpeedKbps shouldBe 50_000
            config.uploadSpeedKbps shouldBe 20_000
        }

        @Test
        fun `default config has zero latency`() {
            val config = RateLimitConfig.default()

            config.latencyMs shouldBe 0
        }

        @Test
        fun `default config has zero packet loss`() {
            val config = RateLimitConfig.default()

            config.packetLossPercent shouldBe 0f
        }

        @Test
        fun `default config has null preset`() {
            val config = RateLimitConfig.default()

            config.preset shouldBe null
        }
    }

    @Nested
    inner class FromPreset {

        @Test
        fun `fromPreset enables rate limiting`() {
            val config = RateLimitConfig.fromPreset(RateLimitConfig.NetworkPreset.WIFI)

            config.enabled shouldBe true
        }

        @Test
        fun `fromPreset applies WIFI speeds`() {
            val config = RateLimitConfig.fromPreset(RateLimitConfig.NetworkPreset.WIFI)

            config.downloadSpeedKbps shouldBe 50_000
            config.uploadSpeedKbps shouldBe 20_000
            config.latencyMs shouldBe 10
            config.packetLossPercent shouldBe 0f
            config.preset shouldBe RateLimitConfig.NetworkPreset.WIFI
        }

        @Test
        fun `fromPreset applies OFFLINE with 100 percent packet loss`() {
            val config = RateLimitConfig.fromPreset(RateLimitConfig.NetworkPreset.OFFLINE)

            config.downloadSpeedKbps shouldBe 0
            config.uploadSpeedKbps shouldBe 0
            config.packetLossPercent shouldBe 100f
            config.preset shouldBe RateLimitConfig.NetworkPreset.OFFLINE
        }

        @Test
        fun `fromPreset applies SLOW_3G values`() {
            val config = RateLimitConfig.fromPreset(RateLimitConfig.NetworkPreset.SLOW_3G)

            config.downloadSpeedKbps shouldBe 400
            config.uploadSpeedKbps shouldBe 100
            config.latencyMs shouldBe 400
            config.packetLossPercent shouldBe 2f
        }
    }

    @Nested
    inner class NetworkPresetValues {

        @Test
        fun `should have exactly 8 presets`() {
            RateLimitConfig.NetworkPreset.entries.size shouldBe 8
        }

        @Test
        fun `WIFI preset properties`() {
            val preset = RateLimitConfig.NetworkPreset.WIFI

            preset.displayName shouldBe "Wi-Fi"
            preset.downloadKbps shouldBe 50_000
            preset.uploadKbps shouldBe 20_000
            preset.latencyMs shouldBe 10
            preset.packetLoss shouldBe 0f
        }

        @Test
        fun `GOOD_3G preset properties`() {
            val preset = RateLimitConfig.NetworkPreset.GOOD_3G

            preset.displayName shouldBe "Good 3G"
            preset.downloadKbps shouldBe 2000
        }

        @Test
        fun `EDGE preset properties`() {
            val preset = RateLimitConfig.NetworkPreset.EDGE

            preset.displayName shouldBe "EDGE"
            preset.downloadKbps shouldBe 35
            preset.uploadKbps shouldBe 10
            preset.latencyMs shouldBe 1500
            preset.packetLoss shouldBe 5f
        }

        @Test
        fun `presets are ordered from fastest to offline`() {
            val presets = RateLimitConfig.NetworkPreset.entries
            val downloadSpeeds = presets.map { it.downloadKbps }

            // Verify general trend: speeds decrease (except OFFLINE is 0)
            downloadSpeeds.first() shouldBe 50_000L // WIFI
            downloadSpeeds.last() shouldBe 0L // OFFLINE
        }
    }

    @Nested
    inner class EqualityAndCopy {

        @Test
        fun `equal configs have the same hashCode`() {
            val c1 = RateLimitConfig.default()
            val c2 = RateLimitConfig.default()

            c1 shouldBe c2
            c1.hashCode() shouldBe c2.hashCode()
        }

        @Test
        fun `copy can enable config`() {
            val original = RateLimitConfig.default()
            val enabled = original.copy(enabled = true)

            enabled.enabled shouldBe true
            enabled.downloadSpeedKbps shouldBe original.downloadSpeedKbps
        }

        @Test
        fun `different enabled flag makes instances unequal`() {
            val c1 = RateLimitConfig.default()
            val c2 = c1.copy(enabled = true)

            c1 shouldNotBe c2
        }
    }
}

class ThrottleStatsTest {

    @Nested
    inner class EmptyFactory {

        @Test
        fun `empty returns all zeros`() {
            val stats = ThrottleStats.empty()

            stats.requestsThrottled shouldBe 0
            stats.totalDelayMs shouldBe 0L
            stats.packetsDropped shouldBe 0
            stats.bytesThrottled shouldBe 0L
        }
    }

    @Nested
    inner class Construction {

        @Test
        fun `constructs with non-zero values`() {
            val stats = ThrottleStats(
                requestsThrottled = 10,
                totalDelayMs = 5000L,
                packetsDropped = 2,
                bytesThrottled = 1024L,
            )

            stats.requestsThrottled shouldBe 10
            stats.totalDelayMs shouldBe 5000L
            stats.packetsDropped shouldBe 2
            stats.bytesThrottled shouldBe 1024L
        }
    }

    @Nested
    inner class EqualityAndHashCode {

        @Test
        fun `empty instances are equal`() {
            ThrottleStats.empty() shouldBe ThrottleStats.empty()
        }

        @Test
        fun `equal instances have the same hashCode`() {
            val s1 = ThrottleStats(1, 100L, 0, 50L)
            val s2 = ThrottleStats(1, 100L, 0, 50L)

            s1 shouldBe s2
            s1.hashCode() shouldBe s2.hashCode()
        }

        @Test
        fun `different values make instances unequal`() {
            val s1 = ThrottleStats(1, 100L, 0, 50L)
            val s2 = ThrottleStats(2, 100L, 0, 50L)

            s1 shouldNotBe s2
        }
    }
}
