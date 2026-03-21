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
            viewModel.fileSearchQuery.value shouldBe ""
        }

        @Test
        fun `no file is selected`() = runTest {
            viewModel.selectedFileName.value shouldBe null
        }

        @Test
        fun `item search query is empty`() = runTest {
            viewModel.itemSearchQuery.value shouldBe ""
        }

        @Test
        fun `type filter is null`() = runTest {
            viewModel.typeFilter.value shouldBe null
        }

        @Test
        fun `is not loading`() = runTest {
            viewModel.isLoading.value shouldBe false
        }
    }

    @Nested
    inner class `preferenceFiles` {

        @Test
        fun `emits sorted files`() = runTest {
            viewModel.preferenceFiles.test {
                val files = awaitUntil { it.size == 3 }
                files.map { it.name } shouldContainExactly listOf("app_settings", "cache_config", "user_prefs")
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters files by search query`() = runTest {
            viewModel.preferenceFiles.test {
                awaitUntil { it.size == 3 }

                viewModel.onFileSearchQueryChanged("user")

                val files = awaitUntil { it.size == 1 }
                files.first().name shouldBe "user_prefs"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank query shows all files`() = runTest {
            viewModel.preferenceFiles.test {
                awaitUntil { it.size == 3 }

                viewModel.onFileSearchQueryChanged("user")
                awaitUntil { it.size == 1 }

                viewModel.onFileSearchQueryChanged("")
                val files = awaitUntil { it.size == 3 }
                files shouldHaveSize 3
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `selectFile` {

        @Test
        fun `sets selected file name`() = runTest {
            viewModel.selectFile("app_settings")

            viewModel.selectedFileName.value shouldBe "app_settings"
        }

        @Test
        fun `resets item search query`() = runTest {
            viewModel.onItemSearchQueryChanged("something")
            viewModel.selectFile("app_settings")

            viewModel.itemSearchQuery.value shouldBe ""
        }

        @Test
        fun `resets type filter`() = runTest {
            viewModel.setTypeFilter("String")
            viewModel.selectFile("app_settings")

            viewModel.typeFilter.value shouldBe null
        }
    }

    @Nested
    inner class `clearFileSelection` {

        @Test
        fun `clears selected file name`() = runTest {
            viewModel.selectFile("app_settings")
            viewModel.clearFileSelection()

            viewModel.selectedFileName.value shouldBe null
        }

        @Test
        fun `resets item search and type filter`() = runTest {
            viewModel.selectFile("app_settings")
            viewModel.onItemSearchQueryChanged("theme")
            viewModel.setTypeFilter("String")

            viewModel.clearFileSelection()

            viewModel.itemSearchQuery.value shouldBe ""
            viewModel.typeFilter.value shouldBe null
        }
    }

    @Nested
    inner class `preferenceItems` {

        @Test
        fun `emits empty list when no file selected`() = runTest {
            viewModel.preferenceItems.test {
                awaitItem().shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits items when file is selected`() = runTest {
            viewModel.preferenceItems.test {
                viewModel.selectFile("app_settings")
                awaitUntil { it.size == 5 }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters items by search query matching key`() = runTest {
            viewModel.preferenceItems.test {
                viewModel.selectFile("app_settings")
                awaitUntil { it.size == 5 }

                viewModel.onItemSearchQueryChanged("theme")
                val items = awaitUntil { it.size == 1 }
                items.first().key shouldBe "theme"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters items by search query matching value`() = runTest {
            viewModel.preferenceItems.test {
                viewModel.selectFile("app_settings")
                awaitUntil { it.size == 5 }

                viewModel.onItemSearchQueryChanged("dark")
                val items = awaitUntil { it.size == 1 }
                items.first().key shouldBe "theme"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters items by type filter`() = runTest {
            viewModel.preferenceItems.test {
                viewModel.selectFile("app_settings")
                awaitUntil { it.size == 5 }

                viewModel.setTypeFilter("String")
                val items = awaitUntil { it.size == 1 }
                items.first().key shouldBe "theme"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `combines search query and type filter`() = runTest {
            viewModel.preferenceItems.test {
                viewModel.selectFile("app_settings")
                awaitUntil { it.size == 5 }

                viewModel.setTypeFilter("Int")
                viewModel.onItemSearchQueryChanged("font")
                val items = awaitUntil { it.size == 1 }
                items.first().key shouldBe "fontSize"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `availableTypes` {

        @Test
        fun `emits empty when no file selected`() = runTest {
            viewModel.availableTypes.test {
                awaitItem().shouldBeEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `emits sorted distinct type names`() = runTest {
            viewModel.availableTypes.test {
                viewModel.selectFile("app_settings")
                val types = awaitUntil { it.size == 5 }
                types shouldContainExactly listOf("Boolean", "Float", "Int", "Long", "String")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `totalItemCount` {

        @Test
        fun `is 0 when no file selected`() = runTest {
            viewModel.totalItemCount.test {
                awaitItem() shouldBe 0
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `reflects raw item count for selected file`() = runTest {
            viewModel.totalItemCount.test {
                viewModel.selectFile("app_settings")
                awaitUntil { it == 5 }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `clearFilters` {

        @Test
        fun `resets item search and type filter`() = runTest {
            viewModel.onItemSearchQueryChanged("theme")
            viewModel.setTypeFilter("String")

            viewModel.clearFilters()

            viewModel.itemSearchQuery.value shouldBe ""
            viewModel.typeFilter.value shouldBe null
        }
    }

    @Nested
    inner class `setPreference` {

        @Test
        fun `delegates to repository with file name and value`() = runTest {
            viewModel.selectFile("app_settings")

            viewModel.setPreference("theme", PreferenceValue.StringValue("light"))

            viewModel.isLoading.test {
                awaitUntil { !it }
                cancelAndIgnoreRemainingEvents()
            }

            coVerify {
                repository.setPreference("app_settings", "theme", PreferenceValue.StringValue("light"))
            }
        }

        @Test
        fun `does nothing when no file selected`() = runTest {
            viewModel.setPreference("theme", PreferenceValue.StringValue("light"))

            coVerify(exactly = 0) { repository.setPreference(any(), any(), any()) }
        }

        @Test
        fun `sets loading to false after operation`() = runTest {
            coEvery { repository.setPreference(any(), any(), any()) } returns Unit
            viewModel.selectFile("app_settings")

            viewModel.setPreference("theme", PreferenceValue.StringValue("light"))

            viewModel.isLoading.test {
                awaitUntil { !it }
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.isLoading.value shouldBe false
        }
    }

    @Nested
    inner class `deletePreference` {

        @Test
        fun `delegates to repository`() = runTest {
            viewModel.selectFile("app_settings")

            viewModel.deletePreference("theme")

            viewModel.isLoading.test {
                awaitUntil { !it }
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { repository.deletePreference("app_settings", "theme") }
        }

        @Test
        fun `does nothing when no file selected`() = runTest {
            viewModel.deletePreference("theme")

            coVerify(exactly = 0) { repository.deletePreference(any(), any()) }
        }
    }

    @Nested
    inner class `clearCurrentFile` {

        @Test
        fun `delegates to repository`() = runTest {
            viewModel.selectFile("app_settings")

            viewModel.clearCurrentFile()

            viewModel.isLoading.test {
                awaitUntil { !it }
                cancelAndIgnoreRemainingEvents()
            }

            coVerify { repository.clearFile("app_settings") }
        }

        @Test
        fun `does nothing when no file selected`() = runTest {
            viewModel.clearCurrentFile()

            coVerify(exactly = 0) { repository.clearFile(any()) }
        }
    }
}
