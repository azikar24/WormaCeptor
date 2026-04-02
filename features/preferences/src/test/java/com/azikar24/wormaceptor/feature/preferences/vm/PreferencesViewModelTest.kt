package com.azikar24.wormaceptor.feature.preferences.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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
class PreferencesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val filesFlow = MutableStateFlow(
        listOf(
            PreferenceFile("app_settings", 5),
            PreferenceFile("user_prefs", 3),
            PreferenceFile("cache_config", 2),
        ),
    )

    private val itemsFlow = MutableStateFlow(
        listOf(
            PreferenceItem("theme", PreferenceValue.StringValue("dark")),
            PreferenceItem("fontSize", PreferenceValue.IntValue(14)),
            PreferenceItem("notifications", PreferenceValue.BooleanValue(true)),
            PreferenceItem("syncInterval", PreferenceValue.LongValue(3600L)),
            PreferenceItem("volume", PreferenceValue.FloatValue(0.8f)),
        ),
    )

    private val repository = mockk<PreferencesRepository>(relaxed = true) {
        every { observePreferenceFiles() } returns filesFlow
        every { observePreferenceItems(any()) } returns itemsFlow
    }

    private lateinit var viewModel: PreferencesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PreferencesViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Helper that keeps consuming items from the turbine until [predicate] is satisfied.
     * Needed because debounce + flowOn(Dispatchers.Default) may emit intermediate states.
     */
    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `file search query is empty`() = runTest {
            viewModel.uiState.value.fileSearchQuery shouldBe ""
        }

        @Test
        fun `no file is selected`() = runTest {
            viewModel.uiState.value.selectedFileName shouldBe null
        }

        @Test
        fun `item search query is empty`() = runTest {
            viewModel.uiState.value.itemSearchQuery shouldBe ""
        }

        @Test
        fun `type filter is null`() = runTest {
            viewModel.uiState.value.typeFilter shouldBe null
        }

        @Test
        fun `is not loading`() = runTest {
            viewModel.uiState.value.isLoading shouldBe false
        }
    }

    @Nested
    inner class `preferenceFiles` {

        @Test
        fun `emits sorted files`() = runTest {
            viewModel.uiState.test {
                val state = awaitUntil { it.preferenceFiles.size == 3 }
                state.preferenceFiles.map { it.name } shouldContainExactly listOf(
                    "app_settings",
                    "cache_config",
                    "user_prefs",
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters files by search query`() = runTest {
            viewModel.uiState.test {
                awaitUntil { it.preferenceFiles.size == 3 }

                viewModel.sendEvent(PreferencesViewEvent.FileSearchQueryChanged("user"))

                val state = awaitUntil { it.preferenceFiles.size == 1 }
                state.preferenceFiles.first().name shouldBe "user_prefs"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank query shows all files`() = runTest {
            viewModel.uiState.test {
                awaitUntil { it.preferenceFiles.size == 3 }

                viewModel.sendEvent(PreferencesViewEvent.FileSearchQueryChanged("user"))
                awaitUntil { it.preferenceFiles.size == 1 }

                viewModel.sendEvent(PreferencesViewEvent.FileSearchQueryChanged(""))
                val state = awaitUntil { it.preferenceFiles.size == 3 }
                state.preferenceFiles shouldHaveSize 3
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectFile event` {

        @Test
        fun `sets selected file name`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.uiState.value.selectedFileName shouldBe "app_settings"
        }

        @Test
        fun `resets item search query`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.ItemSearchQueryChanged("something"))
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.uiState.value.itemSearchQuery shouldBe ""
        }

        @Test
        fun `resets type filter`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SetTypeFilter("String"))
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.uiState.value.typeFilter shouldBe null
        }
    }

    @Nested
    inner class `ClearFileSelection event` {

        @Test
        fun `clears selected file name`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
            viewModel.sendEvent(PreferencesViewEvent.ClearFileSelection)

            viewModel.uiState.value.selectedFileName shouldBe null
        }

        @Test
        fun `resets item search and type filter`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
            viewModel.sendEvent(PreferencesViewEvent.ItemSearchQueryChanged("theme"))
            viewModel.sendEvent(PreferencesViewEvent.SetTypeFilter("String"))

            viewModel.sendEvent(PreferencesViewEvent.ClearFileSelection)

            viewModel.uiState.value.itemSearchQuery shouldBe ""
            viewModel.uiState.value.typeFilter shouldBe null
        }
    }

    @Nested
    inner class `preferenceItems` {

        @Test
        fun `emits empty list when no file selected`() = runTest {
            viewModel.uiState.test {
                awaitItem().preferenceItems.shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits items when file is selected`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                awaitUntil { it.preferenceItems.size == 5 }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters items by search query matching key`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                awaitUntil { it.preferenceItems.size == 5 }

                viewModel.sendEvent(PreferencesViewEvent.ItemSearchQueryChanged("theme"))
                val state = awaitUntil { it.preferenceItems.size == 1 }
                state.preferenceItems.first().key shouldBe "theme"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters items by search query matching value`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                awaitUntil { it.preferenceItems.size == 5 }

                viewModel.sendEvent(PreferencesViewEvent.ItemSearchQueryChanged("dark"))
                val state = awaitUntil { it.preferenceItems.size == 1 }
                state.preferenceItems.first().key shouldBe "theme"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters items by type filter`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                awaitUntil { it.preferenceItems.size == 5 }

                viewModel.sendEvent(PreferencesViewEvent.SetTypeFilter("String"))
                val state = awaitUntil { it.preferenceItems.size == 1 }
                state.preferenceItems.first().key shouldBe "theme"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `combines search query and type filter`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                awaitUntil { it.preferenceItems.size == 5 }

                viewModel.sendEvent(PreferencesViewEvent.SetTypeFilter("Int"))
                viewModel.sendEvent(PreferencesViewEvent.ItemSearchQueryChanged("font"))
                val state = awaitUntil { it.preferenceItems.size == 1 }
                state.preferenceItems.first().key shouldBe "fontSize"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `availableTypes` {

        @Test
        fun `emits empty when no file selected`() = runTest {
            viewModel.uiState.test {
                awaitItem().availableTypes.shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits sorted distinct type names`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                val state = awaitUntil { it.availableTypes.size == 5 }
                state.availableTypes shouldContainExactly listOf("Boolean", "Float", "Int", "Long", "String")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `totalItemCount` {

        @Test
        fun `is 0 when no file selected`() = runTest {
            viewModel.uiState.test {
                awaitItem().totalItemCount shouldBe 0
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `reflects raw item count for selected file`() = runTest {
            viewModel.uiState.test {
                viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))
                awaitUntil { it.totalItemCount == 5 }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `ClearFilters event` {

        @Test
        fun `resets item search and type filter`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.ItemSearchQueryChanged("theme"))
            viewModel.sendEvent(PreferencesViewEvent.SetTypeFilter("String"))

            viewModel.sendEvent(PreferencesViewEvent.ClearFilters)

            viewModel.uiState.value.itemSearchQuery shouldBe ""
            viewModel.uiState.value.typeFilter shouldBe null
        }
    }

    @Nested
    inner class `SetPreference event` {

        @Test
        fun `delegates to repository with file name and value`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.sendEvent(PreferencesViewEvent.SetPreference("theme", PreferenceValue.StringValue("light")))

            viewModel.uiState.test {
                awaitUntil { !it.isLoading }
                cancelAndIgnoreRemainingEvents()
            }

            coVerify {
                repository.setPreference("app_settings", "theme", PreferenceValue.StringValue("light"))
            }
        }

        @Test
        fun `does nothing when no file selected`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SetPreference("theme", PreferenceValue.StringValue("light")))

            coVerify(exactly = 0) { repository.setPreference(any(), any(), any()) }
        }

        @Test
        fun `sets loading to false after operation`() = runTest {
            coEvery { repository.setPreference(any(), any(), any()) } returns Unit
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.sendEvent(PreferencesViewEvent.SetPreference("theme", PreferenceValue.StringValue("light")))

            viewModel.uiState.test {
                awaitUntil { !it.isLoading }
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.value.isLoading shouldBe false
        }
    }

    @Nested
    inner class `DeletePreference event` {

        @Test
        fun `delegates to repository`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.sendEvent(PreferencesViewEvent.DeletePreference("theme"))

            viewModel.uiState.test {
                awaitUntil { !it.isLoading }
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { repository.deletePreference("app_settings", "theme") }
        }

        @Test
        fun `does nothing when no file selected`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.DeletePreference("theme"))

            coVerify(exactly = 0) { repository.deletePreference(any(), any()) }
        }
    }

    @Nested
    inner class `ClearCurrentFile event` {

        @Test
        fun `delegates to repository`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.SelectFile("app_settings"))

            viewModel.sendEvent(PreferencesViewEvent.ClearCurrentFile)

            viewModel.uiState.test {
                awaitUntil { !it.isLoading }
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { repository.clearFile("app_settings") }
        }

        @Test
        fun `does nothing when no file selected`() = runTest {
            viewModel.sendEvent(PreferencesViewEvent.ClearCurrentFile)

            coVerify(exactly = 0) { repository.clearFile(any()) }
        }
    }
}
