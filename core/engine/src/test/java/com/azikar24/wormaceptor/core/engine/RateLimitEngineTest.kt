package com.azikar24.wormaceptor.core.engine

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RateLimitEngineTest {

    private lateinit var engine: RateLimitEngine

    @BeforeEach
    fun setUp() {
        engine = RateLimitEngine()
    }

    @Nested
    inner class StateManagement {

        @Test
        fun `initial config should be disabled`() = runTest {
            engine.config.test {
                val initial = awaitItem()
                initial.enabled shouldBe false
                initial shouldBe RateLimitConfig.default()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `enable should set enabled to true`() = runTest {
            engine.config.test {
                awaitItem() // initial (disabled)
                engine.enable()
                val enabled = awaitItem()
                enabled.enabled shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `disable should set enabled to false`() = runTest {
            engine.config.test {
                awaitItem() // initial (disabled)
                engine.enable()
                awaitItem() // enabled
                engine.disable()
                val disabled = awaitItem()
                disabled.enabled shouldBe false
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `initial stats should be empty`() = runTest {
            engine.stats.test {
                val initial = awaitItem()
                initial shouldBe ThrottleStats.empty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class ApplyPreset {

        @Test
        fun `should apply WIFI preset`() = runTest {
            engine.applyPreset(NetworkPreset.WIFI)

            val config = engine.config.value
            config.enabled shouldBe true
            config.downloadSpeedKbps shouldBe NetworkPreset.WIFI.downloadKbps
            config.uploadSpeedKbps shouldBe NetworkPreset.WIFI.uploadKbps
            config.latencyMs shouldBe NetworkPreset.WIFI.latencyMs
            config.packetLossPercent shouldBe NetworkPreset.WIFI.packetLoss
            config.preset shouldBe NetworkPreset.WIFI
        }

        @Test
        fun `should apply SLOW_3G preset`() = runTest {
            engine.applyPreset(NetworkPreset.SLOW_3G)

            val config = engine.config.value
            config.enabled shouldBe true
            config.downloadSpeedKbps shouldBe 400
            config.uploadSpeedKbps shouldBe 100
            config.latencyMs shouldBe 400
            config.packetLossPercent shouldBe 2f
            config.preset shouldBe NetworkPreset.SLOW_3G
        }

        @Test
        fun `should apply OFFLINE preset with 100 percent packet loss`() = runTest {
            engine.applyPreset(NetworkPreset.OFFLINE)

            val config = engine.config.value
            config.enabled shouldBe true
            config.downloadSpeedKbps shouldBe 0
            config.uploadSpeedKbps shouldBe 0
            config.packetLossPercent shouldBe 100f
            config.preset shouldBe NetworkPreset.OFFLINE
        }

        @Test
        fun `should emit config changes via StateFlow`() = runTest {
            engine.config.test {
                awaitItem() // initial
                engine.applyPreset(NetworkPreset.GOOD_3G)
                val preset = awaitItem()
                preset.preset shouldBe NetworkPreset.GOOD_3G
                preset.downloadSpeedKbps shouldBe 2000
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class SetCustomConfig {

        @Test
        fun `should set custom download speed`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 50,
                packetLossPercent = 0f,
            )

            engine.config.value.downloadSpeedKbps shouldBe 5000
        }

        @Test
        fun `should set custom upload speed`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 50,
                packetLossPercent = 0f,
            )

            engine.config.value.uploadSpeedKbps shouldBe 1000
        }

        @Test
        fun `should set custom latency`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 250,
                packetLossPercent = 0f,
            )

            engine.config.value.latencyMs shouldBe 250
        }

        @Test
        fun `should clamp packet loss to 0-100 range`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 0,
                packetLossPercent = 150f,
            )

            engine.config.value.packetLossPercent shouldBe 100f
        }

        @Test
        fun `should clamp negative packet loss to 0`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 0,
                packetLossPercent = -10f,
            )

            engine.config.value.packetLossPercent shouldBe 0f
        }

        @Test
        fun `custom config should enable rate limiting`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 0,
                packetLossPercent = 0f,
            )

            engine.config.value.enabled shouldBe true
        }

        @Test
        fun `custom config should have null preset`() {
            engine.setCustomConfig(
                downloadSpeedKbps = 5000,
                uploadSpeedKbps = 1000,
                latencyMs = 0,
                packetLossPercent = 0f,
            )

            engine.config.value.preset shouldBe null
        }
    }

    @Nested
    inner class ClearStats {

        @Test
        fun `should reset stats to empty`() = runTest {
            // Just verify clearStats resets to empty (since we can't easily
            // trigger actual interceptor activity without MockWebServer)
            engine.clearStats()

            engine.stats.value shouldBe ThrottleStats.empty()
        }

        @Test
        fun `should keep stats at empty after clearStats when already empty`() = runTest {
            engine.stats.test {
                val initial = awaitItem()
                initial shouldBe ThrottleStats.empty()
                // clearStats emits the same empty value, but StateFlow deduplicates
                // identical values, so no new emission expected
                engine.clearStats()
                // Verify value is still empty
                engine.stats.value shouldBe ThrottleStats.empty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class SetConfig {

        @Test
        fun `should directly set config`() {
            val customConfig = RateLimitConfig(
                enabled = true,
                downloadSpeedKbps = 1234,
                uploadSpeedKbps = 567,
                latencyMs = 89,
                packetLossPercent = 1.5f,
                preset = null,
            )

            engine.setConfig(customConfig)

            engine.config.value shouldBe customConfig
        }
    }

    @Nested
    inner class Interceptor {

        @Test
        fun `getInterceptor should return an interceptor instance`() {
            val interceptor = engine.getInterceptor()
            // Verify that the interceptor is the same instance on repeated calls
            interceptor shouldBe engine.getInterceptor()
        }
    }

    @Nested
    inner class CompanionConstants {

        @Test
        fun `MIN_SPEED_KBPS should be 1`() {
            RateLimitEngine.MIN_SPEED_KBPS shouldBe 1L
        }

        @Test
        fun `MAX_SPEED_KBPS should be 100000`() {
            RateLimitEngine.MAX_SPEED_KBPS shouldBe 100_000L
        }

        @Test
        fun `MIN_LATENCY_MS should be 0`() {
            RateLimitEngine.MIN_LATENCY_MS shouldBe 0L
        }

        @Test
        fun `MAX_LATENCY_MS should be 5000`() {
            RateLimitEngine.MAX_LATENCY_MS shouldBe 5000L
        }

        @Test
        fun `MIN_PACKET_LOSS should be 0`() {
            RateLimitEngine.MIN_PACKET_LOSS shouldBe 0f
        }

        @Test
        fun `MAX_PACKET_LOSS should be 100`() {
            RateLimitEngine.MAX_PACKET_LOSS shouldBe 100f
        }
    }
}
