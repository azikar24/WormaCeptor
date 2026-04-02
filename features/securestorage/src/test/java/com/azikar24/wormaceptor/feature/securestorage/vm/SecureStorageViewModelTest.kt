package com.azikar24.wormaceptor.feature.securestorage.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
class SecureStorageViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val entriesFlow = MutableStateFlow<List<SecureStorageEntry>>(emptyList())
    private val summaryFlow = MutableStateFlow(SecureStorageSummary.empty())
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val keystoreAccessibleFlow = MutableStateFlow(false)
    private val encryptedPrefsAccessibleFlow = MutableStateFlow(false)
    private val lastRefreshTimeFlow = MutableStateFlow<Long?>(null)

    private val engine = mockk<SecureStorageEngine>(relaxed = true) {
        every { entries } returns entriesFlow
        every { summary } returns summaryFlow
        every { isLoading } returns isLoadingFlow
        every { error } returns errorFlow
        every { keystoreAccessible } returns keystoreAccessibleFlow
        every { encryptedPrefsAccessible } returns encryptedPrefsAccessibleFlow
        every { lastRefreshTime } returns lastRefreshTimeFlow
    }

    private lateinit var viewModel: SecureStorageViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SecureStorageViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeEntry(
        key: String = "my_key",
        value: String = "my_value",
        storageType: StorageType = StorageType.ENCRYPTED_SHARED_PREFS,
        isEncrypted: Boolean = true,
    ) = SecureStorageEntry(
        key = key,
        value = value,
        storageType = storageType,
        isEncrypted = isEncrypted,
        lastModified = System.currentTimeMillis(),
    )

    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `selectedType is null`() = runTest {
            viewModel.uiState.value.selectedType shouldBe null
        }

        @Test
        fun `searchQuery is empty`() = runTest {
            viewModel.uiState.value.searchQuery shouldBe ""
        }

        @Test
        fun `selectedEntry is null`() = runTest {
            viewModel.uiState.value.selectedEntry shouldBe null
        }

        @Test
        fun `filteredEntries is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().filteredEntries.shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class `SelectType event` {

        @Test
        fun `updates selected type`() = runTest {
            viewModel.sendEvent(SecureStorageViewEvent.SelectType(StorageType.KEYSTORE))

            viewModel.uiState.value.selectedType shouldBe StorageType.KEYSTORE
        }

        @Test
        fun `setting null clears the type filter`() = runTest {
            viewModel.sendEvent(SecureStorageViewEvent.SelectType(StorageType.KEYSTORE))
            viewModel.sendEvent(SecureStorageViewEvent.SelectType(null))

            viewModel.uiState.value.selectedType shouldBe null
        }

        @Test
        fun `filters entries by type`() = runTest {
            val prefsEntry = makeEntry(
                key = "prefs_key",
                storageType = StorageType.ENCRYPTED_SHARED_PREFS,
            )
            val keystoreEntry = makeEntry(
                key = "keystore_alias",
                value = "Type: Key Entry",
                storageType = StorageType.KEYSTORE,
            )
            entriesFlow.value = listOf(prefsEntry, keystoreEntry)
            viewModel.sendEvent(SecureStorageViewEvent.SelectType(StorageType.KEYSTORE))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredEntries.size == 1 &&
                        it.filteredEntries.first().storageType == StorageType.KEYSTORE
                }
                state.filteredEntries shouldHaveSize 1
                state.filteredEntries.first().key shouldBe "keystore_alias"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `UpdateSearchQuery event` {

        @Test
        fun `updates search query`() = runTest {
            viewModel.sendEvent(SecureStorageViewEvent.UpdateSearchQuery("token"))

            viewModel.uiState.value.searchQuery shouldBe "token"
        }

        @Test
        fun `filters by key case-insensitively`() = runTest {
            val tokenEntry = makeEntry(key = "auth_token", value = "abc123")
            val userEntry = makeEntry(key = "user_name", value = "John")
            entriesFlow.value = listOf(tokenEntry, userEntry)
            viewModel.sendEvent(SecureStorageViewEvent.UpdateSearchQuery("TOKEN"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredEntries.size == 1 && it.filteredEntries.first().key == "auth_token"
                }
                state.filteredEntries shouldHaveSize 1
                state.filteredEntries.first().key shouldBe "auth_token"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by value`() = runTest {
            val tokenEntry = makeEntry(key = "auth_token", value = "secret_abc123")
            val userEntry = makeEntry(key = "user_name", value = "John")
            entriesFlow.value = listOf(tokenEntry, userEntry)
            viewModel.sendEvent(SecureStorageViewEvent.UpdateSearchQuery("secret"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredEntries.size == 1 && it.filteredEntries.first().key == "auth_token"
                }
                state.filteredEntries shouldHaveSize 1
                state.filteredEntries.first().value shouldBe "secret_abc123"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank query returns all entries`() = runTest {
            val entry1 = makeEntry(key = "key1")
            val entry2 = makeEntry(key = "key2", value = "val2")
            entriesFlow.value = listOf(entry1, entry2)
            viewModel.sendEvent(SecureStorageViewEvent.UpdateSearchQuery("key1"))

            viewModel.uiState.test {
                awaitUntil { it.filteredEntries.size == 1 }

                viewModel.sendEvent(SecureStorageViewEvent.UpdateSearchQuery(""))
                val state = awaitUntil { it.filteredEntries.size == 2 }
                state.filteredEntries shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectEntry and DismissDetail events` {

        @Test
        fun `SelectEntry sets the selected entry`() = runTest {
            val entry = makeEntry()

            viewModel.sendEvent(SecureStorageViewEvent.SelectEntry(entry))

            viewModel.uiState.value.selectedEntry shouldBe entry
        }

        @Test
        fun `DismissDetail clears the selected entry`() = runTest {
            viewModel.sendEvent(SecureStorageViewEvent.SelectEntry(makeEntry()))
            viewModel.sendEvent(SecureStorageViewEvent.DismissDetail)

            viewModel.uiState.value.selectedEntry shouldBe null
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `Refresh delegates to engine`() {
            viewModel.sendEvent(SecureStorageViewEvent.Refresh)

            verify { engine.refresh() }
        }

        @Test
        fun `isLoading reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().isLoading shouldBe false
                isLoadingFlow.value = true
                awaitUntil { it.isLoading }.isLoading shouldBe true
            }
        }

        @Test
        fun `error reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().error shouldBe null
                errorFlow.value = "KeyStore inaccessible"
                awaitUntil { it.error != null }.error shouldBe "KeyStore inaccessible"
            }
        }

        @Test
        fun `keystoreAccessible reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().keystoreAccessible shouldBe false
                keystoreAccessibleFlow.value = true
                awaitUntil { it.keystoreAccessible }.keystoreAccessible shouldBe true
            }
        }

        @Test
        fun `encryptedPrefsAccessible reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().encryptedPrefsAccessible shouldBe false
                encryptedPrefsAccessibleFlow.value = true
                awaitUntil { it.encryptedPrefsAccessible }.encryptedPrefsAccessible shouldBe true
            }
        }

        @Test
        fun `lastRefreshTime reflects engine state`() = runTest {
            viewModel.uiState.test {
                awaitItem().lastRefreshTime shouldBe null
                val now = System.currentTimeMillis()
                lastRefreshTimeFlow.value = now
                awaitUntil { it.lastRefreshTime != null }.lastRefreshTime shouldBe now
            }
        }
    }

    @Nested
    inner class `combined filtering` {

        @Test
        fun `type and search query combine`() = runTest {
            val prefsToken = makeEntry(
                key = "auth_token",
                storageType = StorageType.ENCRYPTED_SHARED_PREFS,
            )
            val prefsUser = makeEntry(
                key = "user_name",
                value = "John",
                storageType = StorageType.ENCRYPTED_SHARED_PREFS,
            )
            val keystoreToken = makeEntry(
                key = "token_key",
                value = "Type: Key Entry",
                storageType = StorageType.KEYSTORE,
            )
            entriesFlow.value = listOf(prefsToken, prefsUser, keystoreToken)
            viewModel.sendEvent(SecureStorageViewEvent.SelectType(StorageType.ENCRYPTED_SHARED_PREFS))
            viewModel.sendEvent(SecureStorageViewEvent.UpdateSearchQuery("token"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredEntries.size == 1 && it.filteredEntries.first().key == "auth_token"
                }
                state.filteredEntries shouldHaveSize 1
                state.filteredEntries.first().key shouldBe "auth_token"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
