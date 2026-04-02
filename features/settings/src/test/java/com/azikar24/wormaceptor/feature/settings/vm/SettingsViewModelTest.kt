package com.azikar24.wormaceptor.feature.settings.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import com.azikar24.wormaceptor.domain.contracts.FeatureConfigRepository
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val configFlow = MutableStateFlow(FeatureConfig.DEFAULT)

    private val repository = mockk<FeatureConfigRepository>(relaxed = true) {
        every { observeConfig() } returns configFlow
    }

    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `uiState starts with default feature config`() = runTest {
            viewModel.uiState.test {
                awaitItem().featureConfig shouldBe FeatureConfig.DEFAULT
            }
        }
    }

    @Nested
    inner class `ToggleNetworkTab event` {

        @Test
        fun `calls updateConfig with toggled showNetworkTab`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ToggleNetworkTab)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showNetworkTab = false),
                )
            }
        }

        @Test
        fun `toggles from false to true`() = runTest {
            configFlow.value = FeatureConfig.DEFAULT.copy(showNetworkTab = false)

            viewModel.uiState.test {
                awaitUntil { !it.featureConfig.showNetworkTab }

                viewModel.sendEvent(SettingsViewEvent.ToggleNetworkTab)

                coVerify {
                    repository.updateConfig(
                        match { it.showNetworkTab },
                    )
                }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `ToggleCrashesTab event` {

        @Test
        fun `calls updateConfig with toggled showCrashesTab`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ToggleCrashesTab)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showCrashesTab = false),
                )
            }
        }
    }

    @Nested
    inner class `TogglePreferences event` {

        @Test
        fun `calls updateConfig with toggled showPreferences`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.TogglePreferences)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showPreferences = false),
                )
            }
        }
    }

    @Nested
    inner class `ToggleConsoleLogs event` {

        @Test
        fun `calls updateConfig with toggled showConsoleLogs`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ToggleConsoleLogs)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showConsoleLogs = false),
                )
            }
        }
    }

    @Nested
    inner class `ToggleDeviceInfo event` {

        @Test
        fun `calls updateConfig with toggled showDeviceInfo`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ToggleDeviceInfo)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showDeviceInfo = false),
                )
            }
        }
    }

    @Nested
    inner class `ToggleSqliteBrowser event` {

        @Test
        fun `calls updateConfig with toggled showSqliteBrowser`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ToggleSqliteBrowser)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showSqliteBrowser = false),
                )
            }
        }
    }

    @Nested
    inner class `ToggleFileBrowser event` {

        @Test
        fun `calls updateConfig with toggled showFileBrowser`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ToggleFileBrowser)

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showFileBrowser = false),
                )
            }
        }
    }

    @Nested
    inner class `ResetToDefaults event` {

        @Test
        fun `delegates to repository`() = runTest {
            viewModel.sendEvent(SettingsViewEvent.ResetToDefaults)

            coVerify { repository.resetToDefaults() }
        }
    }

    @Nested
    inner class `reactive pipeline` {

        @Test
        fun `uiState emits updated config from repository`() = runTest {
            val updated = FeatureConfig.DEFAULT.copy(
                showNetworkTab = false,
                showConsoleLogs = false,
            )

            viewModel.uiState.test {
                awaitItem().featureConfig shouldBe FeatureConfig.DEFAULT
                configFlow.value = updated
                awaitItem().featureConfig shouldBe updated
            }
        }

        @Test
        fun `uiState reflects multiple sequential updates`() = runTest {
            viewModel.uiState.test {
                awaitItem().featureConfig shouldBe FeatureConfig.DEFAULT

                configFlow.value = FeatureConfig.DEFAULT.copy(showNetworkTab = false)
                awaitItem().featureConfig.showNetworkTab shouldBe false

                configFlow.value = FeatureConfig.DEFAULT.copy(
                    showNetworkTab = false,
                    showCrashesTab = false,
                )
                val latest = awaitItem().featureConfig
                latest.showNetworkTab shouldBe false
                latest.showCrashesTab shouldBe false
            }
        }
    }
}
