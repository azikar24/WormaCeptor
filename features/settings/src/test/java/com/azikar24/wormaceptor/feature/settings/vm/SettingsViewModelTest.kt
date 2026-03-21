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
        fun `featureConfig starts with default values`() = runTest {
            viewModel.featureConfig.test {
                awaitItem() shouldBe FeatureConfig.DEFAULT
            }
        }
    }

    @Nested
    inner class `toggleNetworkTab` {

        @Test
        fun `calls updateConfig with toggled showNetworkTab`() = runTest {
            viewModel.toggleNetworkTab()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showNetworkTab = false),
                )
            }
        }

        @Test
        fun `toggles from false to true`() = runTest {
            configFlow.value = FeatureConfig.DEFAULT.copy(showNetworkTab = false)

            viewModel.featureConfig.test {
                awaitUntil { !it.showNetworkTab }

                viewModel.toggleNetworkTab()

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
    inner class `toggleCrashesTab` {

        @Test
        fun `calls updateConfig with toggled showCrashesTab`() = runTest {
            viewModel.toggleCrashesTab()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showCrashesTab = false),
                )
            }
        }
    }

    @Nested
    inner class `togglePreferences` {

        @Test
        fun `calls updateConfig with toggled showPreferences`() = runTest {
            viewModel.togglePreferences()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showPreferences = false),
                )
            }
        }
    }

    @Nested
    inner class `toggleConsoleLogs` {

        @Test
        fun `calls updateConfig with toggled showConsoleLogs`() = runTest {
            viewModel.toggleConsoleLogs()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showConsoleLogs = false),
                )
            }
        }
    }

    @Nested
    inner class `toggleDeviceInfo` {

        @Test
        fun `calls updateConfig with toggled showDeviceInfo`() = runTest {
            viewModel.toggleDeviceInfo()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showDeviceInfo = false),
                )
            }
        }
    }

    @Nested
    inner class `toggleSqliteBrowser` {

        @Test
        fun `calls updateConfig with toggled showSqliteBrowser`() = runTest {
            viewModel.toggleSqliteBrowser()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showSqliteBrowser = false),
                )
            }
        }
    }

    @Nested
    inner class `toggleFileBrowser` {

        @Test
        fun `calls updateConfig with toggled showFileBrowser`() = runTest {
            viewModel.toggleFileBrowser()

            coVerify {
                repository.updateConfig(
                    FeatureConfig.DEFAULT.copy(showFileBrowser = false),
                )
            }
        }
    }

    @Nested
    inner class `resetToDefaults` {

        @Test
        fun `delegates to repository`() = runTest {
            viewModel.resetToDefaults()

            coVerify { repository.resetToDefaults() }
        }
    }

    @Nested
    inner class `reactive pipeline` {

        @Test
        fun `featureConfig emits updated config from repository`() = runTest {
            val updated = FeatureConfig.DEFAULT.copy(
                showNetworkTab = false,
                showConsoleLogs = false,
            )

            viewModel.featureConfig.test {
                awaitItem() shouldBe FeatureConfig.DEFAULT
                configFlow.value = updated
                awaitItem() shouldBe updated
            }
        }

        @Test
        fun `featureConfig reflects multiple sequential updates`() = runTest {
            viewModel.featureConfig.test {
                awaitItem() shouldBe FeatureConfig.DEFAULT

                configFlow.value = FeatureConfig.DEFAULT.copy(showNetworkTab = false)
                awaitItem().showNetworkTab shouldBe false

                configFlow.value = FeatureConfig.DEFAULT.copy(
                    showNetworkTab = false,
                    showCrashesTab = false,
                )
                val latest = awaitItem()
                latest.showNetworkTab shouldBe false
                latest.showCrashesTab shouldBe false
            }
        }
    }
}
