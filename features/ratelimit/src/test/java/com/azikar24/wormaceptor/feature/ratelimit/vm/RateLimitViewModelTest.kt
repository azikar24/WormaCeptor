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
    inner class `config state` {

        @Test
        fun `initial value is default config`() = runTest {
            viewModel.uiState.test {
                awaitItem().config shouldBe RateLimitConfig.default()
            }
        }

        @Test
        fun `emits updated config when engine config changes`() = runTest {
            val updated = RateLimitConfig.default().copy(enabled = true)

            viewModel.uiState.test {
                awaitItem().config shouldBe RateLimitConfig.default()
                configFlow.value = updated
                awaitItem().config shouldBe updated
            }
        }
    }

    @Nested
    inner class `stats state` {

        @Test
        fun `initial value is empty stats`() = runTest {
            viewModel.uiState.test {
                awaitItem().stats shouldBe ThrottleStats.empty()
            }
        }

        @Test
        fun `emits updated stats when engine stats change`() = runTest {
            val updated =
                ThrottleStats(requestsThrottled = 5, totalDelayMs = 100, packetsDropped = 1, bytesThrottled = 1024)

            viewModel.uiState.test {
                awaitItem().stats shouldBe ThrottleStats.empty()
                statsFlow.value = updated
                awaitItem().stats shouldBe updated
            }
        }
    }

    @Nested
    inner class `selectedPreset` {

        @Test
        fun `initial value is null`() = runTest {
            viewModel.uiState.test {
                awaitItem().selectedPreset shouldBe null
            }
        }

        @Test
        fun `emits preset when config has a preset`() = runTest {
            viewModel.uiState.test {
                awaitItem().selectedPreset shouldBe null
                configFlow.value = RateLimitConfig.fromPreset(NetworkPreset.SLOW_3G)
                awaitItem().selectedPreset shouldBe NetworkPreset.SLOW_3G
            }
        }
    }

    @Nested
    inner class `ToggleEnabled` {

        @Test
        fun `calls disable when currently enabled`() = runTest {
            configFlow.value = RateLimitConfig.default().copy(enabled = true)

            viewModel.uiState.test {
                awaitItem() // collect so the combine upstream connects
                viewModel.sendEvent(RateLimitViewEvent.ToggleEnabled)
                verify { engine.disable() }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `calls enable when currently disabled`() = runTest {
            configFlow.value = RateLimitConfig.default().copy(enabled = false)

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.ToggleEnabled)
                verify { engine.enable() }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectPreset` {

        @Test
        fun `applies preset via engine when non-null`() {
            viewModel.sendEvent(RateLimitViewEvent.SelectPreset(NetworkPreset.GOOD_3G))

            verify { engine.applyPreset(NetworkPreset.GOOD_3G) }
        }

        @Test
        fun `clears preset but keeps current values when null`() = runTest {
            val current = RateLimitConfig.fromPreset(NetworkPreset.GOOD_3G)
            configFlow.value = current

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SelectPreset(null))
                verify { engine.setConfig(current.copy(preset = null)) }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SetDownloadSpeed` {

        @Test
        fun `sets config with clamped download speed and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetDownloadSpeed(5000))
                verify {
                    engine.setConfig(current.copy(downloadSpeedKbps = 5000, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps speed to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetDownloadSpeed(-100))
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

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetDownloadSpeed(999_999))
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
    inner class `SetUploadSpeed` {

        @Test
        fun `sets config with clamped upload speed and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetUploadSpeed(3000))
                verify {
                    engine.setConfig(current.copy(uploadSpeedKbps = 3000, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps speed to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetUploadSpeed(-50))
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

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetUploadSpeed(999_999))
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
    inner class `SetLatency` {

        @Test
        fun `sets config with clamped latency and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetLatency(250))
                verify {
                    engine.setConfig(current.copy(latencyMs = 250, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps latency to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetLatency(-10))
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

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetLatency(10_000))
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
    inner class `SetPacketLoss` {

        @Test
        fun `sets config with clamped packet loss and clears preset`() = runTest {
            val current = RateLimitConfig.default()
            configFlow.value = current

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetPacketLoss(10f))
                verify {
                    engine.setConfig(current.copy(packetLossPercent = 10f, preset = null))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `clamps packet loss to minimum`() = runTest {
            configFlow.value = RateLimitConfig.default()

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetPacketLoss(-5f))
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

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.SetPacketLoss(150f))
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
    inner class `ClearStats` {

        @Test
        fun `delegates to engine`() {
            viewModel.sendEvent(RateLimitViewEvent.ClearStats)

            verify { engine.clearStats() }
        }
    }

    @Nested
    inner class `ResetToDefaults` {

        @Test
        fun `resets to default config while preserving enabled state`() = runTest {
            configFlow.value = RateLimitConfig.fromPreset(NetworkPreset.GOOD_3G)

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.ResetToDefaults)
                verify {
                    engine.setConfig(RateLimitConfig.default().copy(enabled = true))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets to default config preserving disabled state`() = runTest {
            configFlow.value = RateLimitConfig.fromPreset(NetworkPreset.GOOD_3G).copy(enabled = false)

            viewModel.uiState.test {
                awaitItem()
                viewModel.sendEvent(RateLimitViewEvent.ResetToDefaults)
                verify {
                    engine.setConfig(RateLimitConfig.default().copy(enabled = false))
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
