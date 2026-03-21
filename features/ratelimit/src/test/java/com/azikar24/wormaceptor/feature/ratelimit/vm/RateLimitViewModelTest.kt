package com.azikar24.wormaceptor.feature.ratelimit.vm

import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RateLimitViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val configFlow = MutableStateFlow(RateLimitConfig.default())
    private val statsFlow = MutableStateFlow(ThrottleStats.empty())
    private val engine = mockk<RateLimitEngine>(relaxed = true) {
        every { config } returns configFlow
        every { stats } returns statsFlow
    }

    private lateinit var viewModel: RateLimitViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RateLimitViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `config StateFlow` {

        @Test
        fun `initial value is default config`() = runTest {
            viewModel.config.test {
                awaitItem() shouldBe RateLimitConfig.default()
            }
        }

        @Test
        fun `emits updated config when engine config changes`() = runTest {
            val updated = RateLimitConfig.default().copy(enabled = true)

            viewModel.config.test {
                awaitItem() shouldBe RateLimitConfig.default()
                configFlow.value = updated
                awaitItem() shouldBe updated
            }
        }
    }

    @Nested
    inner class `stats StateFlow` {

        @Test
        fun `initial value is empty stats`() = runTest {
            viewModel.stats.test {
                awaitItem() shouldBe ThrottleStats.empty()
            }
        }

        @Test
        fun `emits updated stats when engine stats change`() = runTest {
            val updated =
                ThrottleStats(requestsThrottled = 5, totalDelayMs = 100, packetsDropped = 1, bytesThrottled = 1024)

            viewModel.stats.test {
                awaitItem() shouldBe ThrottleStats.empty()
                statsFlow.value = updated
                awaitItem() shouldBe updated
            }
        }
    }

    @Nested
    inner class `selectedPreset` {

        @Test
        fun `initial value is null`() = runTest {
            viewModel.selectedPreset.test {
                awaitItem() shouldBe null
            }
        }

        @Test
        fun `emits preset when config has a preset`() = runTest {
            viewModel.selectedPreset.test {
                awaitItem() shouldBe null
                configFlow.value = RateLimitConfig.fromPreset(NetworkPreset.SLOW_3G)
                awaitItem() shouldBe NetworkPreset.SLOW_3G
            }
        }
    }

    @Nested
    inner class `toggleEnabled` {

        @Test
        fun `calls disable when currently enabled`() = runTest {
            configFlow.value = RateLimitConfig.default().copy(enabled = true)

            viewModel.config.test {
                awaitItem() // collect so the stateIn upstream connects
                viewModel.toggleEnabled()
                verify { engine.disable() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `calls enable when currently disabled`() = runTest {
            configFlow.value = RateLimitConfig.default().copy(enabled = false)

            viewModel.config.test {
                awaitItem()
                viewModel.toggleEnabled()
                verify { engine.enable() }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `selectPreset` {

        @Test
        fun `applies preset via engine when non-null`() {
            viewModel.selectPreset(NetworkPreset.GOOD_3G)

            verify { engine.applyPreset(NetworkPreset.GOOD_3G) }
        }

        @Test
        fun `clears preset but keeps current values when null`() = runTest {
            val current = RateLimitConfig.fromPreset(NetworkPreset.GOOD_3G)
            configFlow.value = current

            viewModel.config.test {
                awaitItem()
                viewModel.selectPreset(null)
                verify { engine.setConfig(current.copy(preset = null)) }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `setDownloadSpeed` {

        @Test
        fun `sets config with clamped download speed and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.config.test {
                awaitItem()
                viewModel.setDownloadSpeed(5000)
                verify {
                    engine.setConfig(current.copy(downloadSpeedKbps = 5000, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps speed to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setDownloadSpeed(-100)
                verify {
                    engine.setConfig(
                        match { it.downloadSpeedKbps == RateLimitEngine.MIN_SPEED_KBPS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps speed to maximum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setDownloadSpeed(999_999)
                verify {
                    engine.setConfig(
                        match { it.downloadSpeedKbps == RateLimitEngine.MAX_SPEED_KBPS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `setUploadSpeed` {

        @Test
        fun `sets config with clamped upload speed and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.config.test {
                awaitItem()
                viewModel.setUploadSpeed(3000)
                verify {
                    engine.setConfig(current.copy(uploadSpeedKbps = 3000, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps speed to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setUploadSpeed(-50)
                verify {
                    engine.setConfig(
                        match { it.uploadSpeedKbps == RateLimitEngine.MIN_SPEED_KBPS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps speed to maximum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setUploadSpeed(999_999)
                verify {
                    engine.setConfig(
                        match { it.uploadSpeedKbps == RateLimitEngine.MAX_SPEED_KBPS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `setLatency` {

        @Test
        fun `sets config with clamped latency and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.config.test {
                awaitItem()
                viewModel.setLatency(250)
                verify {
                    engine.setConfig(current.copy(latencyMs = 250, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps latency to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setLatency(-10)
                verify {
                    engine.setConfig(
                        match { it.latencyMs == RateLimitEngine.MIN_LATENCY_MS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps latency to maximum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setLatency(10_000)
                verify {
                    engine.setConfig(
                        match { it.latencyMs == RateLimitEngine.MAX_LATENCY_MS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `setPacketLoss` {

        @Test
        fun `sets config with clamped packet loss and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.config.test {
                awaitItem()
                viewModel.setPacketLoss(10f)
                verify {
                    engine.setConfig(current.copy(packetLossPercent = 10f, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps packet loss to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setPacketLoss(-5f)
                verify {
                    engine.setConfig(
                        match { it.packetLossPercent == RateLimitEngine.MIN_PACKET_LOSS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps packet loss to range`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.config.test {
                awaitItem()
                viewModel.setPacketLoss(150f)
                verify {
                    engine.setConfig(
                        match { it.packetLossPercent == RateLimitEngine.MAX_PACKET_LOSS },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `clearStats` {

        @Test
        fun `delegates to engine`() {
            viewModel.clearStats()

            verify { engine.clearStats() }
        }
    }

    @Nested
    inner class `resetToDefaults` {

        @Test
        fun `resets to default config while preserving enabled state`() = runTest {
            configFlow.value = RateLimitConfig.fromPreset(NetworkPreset.GOOD_3G)

            viewModel.config.test {
                awaitItem()
                viewModel.resetToDefaults()
                verify {
                    engine.setConfig(RateLimitConfig.default().copy(enabled = true))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets to default config preserving disabled state`() = runTest {
            configFlow.value = RateLimitConfig.fromPreset(NetworkPreset.GOOD_3G).copy(enabled = false)

            viewModel.config.test {
                awaitItem()
                viewModel.resetToDefaults()
                verify {
                    engine.setConfig(RateLimitConfig.default().copy(enabled = false))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
