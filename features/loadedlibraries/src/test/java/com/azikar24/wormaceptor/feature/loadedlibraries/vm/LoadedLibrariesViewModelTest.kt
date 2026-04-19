package com.azikar24.wormaceptor.feature.loadedlibraries.vm

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary.LibraryType
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
class LoadedLibrariesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val librariesFlow = MutableStateFlow<List<LoadedLibrary>>(emptyList())
    private val summaryFlow = MutableStateFlow(LibrarySummary.empty())
    private val isLoadingFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)

    private val engine = mockk<LoadedLibrariesEngine>(relaxed = true) {
        every { libraries } returns librariesFlow
        every { summary } returns summaryFlow
        every { isLoading } returns isLoadingFlow
        every { error } returns errorFlow
    }

    private lateinit var viewModel: LoadedLibrariesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoadedLibrariesViewModel(engine)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun makeLibrary(
        name: String = "libc.so",
        path: String = "/system/lib64/libc.so",
        type: LibraryType = LibraryType.NATIVE_SO,
        isSystemLibrary: Boolean = true,
    ) = LoadedLibrary(
        name = name,
        path = path,
        type = type,
        size = 1024L,
        loadAddress = "0x7f000000",
        version = null,
        isSystemLibrary = isSystemLibrary,
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
        fun `showSystemLibs is true`() = runTest {
            viewModel.uiState.value.showSystemLibs shouldBe true
        }

        @Test
        fun `searchQuery is empty`() = runTest {
            viewModel.uiState.value.searchQuery shouldBe ""
        }

        @Test
        fun `selectedLibrary is null`() = runTest {
            viewModel.uiState.value.selectedLibrary shouldBe null
        }

        @Test
        fun `filteredLibraries is empty`() = runTest {
            viewModel.uiState.test {
                awaitItem().filteredLibraries.shouldBeEmpty()
            }
        }
    }

    @Nested
    inner class `SetSelectedType event` {

        @Test
        fun `updates selected type`() = runTest {
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSelectedType(LibraryType.NATIVE_SO))

            viewModel.uiState.value.selectedType shouldBe LibraryType.NATIVE_SO
        }

        @Test
        fun `setting null clears the type filter`() = runTest {
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSelectedType(LibraryType.DEX))
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSelectedType(null))

            viewModel.uiState.value.selectedType shouldBe null
        }

        @Test
        fun `filters libraries by type`() = runTest {
            val nativeLib = makeLibrary(name = "libc.so", type = LibraryType.NATIVE_SO)
            val dexLib = makeLibrary(
                name = "classes.dex",
                path = "/data/app/classes.dex",
                type = LibraryType.DEX,
                isSystemLibrary = false,
            )
            librariesFlow.value = listOf(nativeLib, dexLib)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSelectedType(LibraryType.NATIVE_SO))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLibraries.size == 1 && it.filteredLibraries.first().name == "libc.so"
                }
                state.filteredLibraries shouldHaveSize 1
                state.filteredLibraries.first().type shouldBe LibraryType.NATIVE_SO
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SetShowSystemLibs event` {

        @Test
        fun `updates showSystemLibs flag`() = runTest {
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetShowSystemLibs(false))

            viewModel.uiState.value.showSystemLibs shouldBe false
        }

        @Test
        fun `hides system libraries when false`() = runTest {
            val systemLib = makeLibrary(name = "libc.so", isSystemLibrary = true)
            val appLib = makeLibrary(
                name = "libapp.so",
                path = "/data/app/libapp.so",
                isSystemLibrary = false,
            )
            librariesFlow.value = listOf(systemLib, appLib)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetShowSystemLibs(false))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLibraries.size == 1 && it.filteredLibraries.first().name == "libapp.so"
                }
                state.filteredLibraries shouldHaveSize 1
                state.filteredLibraries.first().isSystemLibrary shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `shows all libraries when true`() = runTest {
            val systemLib = makeLibrary(name = "libc.so", isSystemLibrary = true)
            val appLib = makeLibrary(
                name = "libapp.so",
                path = "/data/app/libapp.so",
                isSystemLibrary = false,
            )
            librariesFlow.value = listOf(systemLib, appLib)

            viewModel.uiState.test {
                val state = awaitUntil { it.filteredLibraries.size == 2 }
                state.filteredLibraries shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SetSearchQuery event` {

        @Test
        fun `updates search query`() = runTest {
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSearchQuery("libc"))

            viewModel.uiState.value.searchQuery shouldBe "libc"
        }

        @Test
        fun `filters by name case-insensitively`() = runTest {
            val libc = makeLibrary(name = "libc.so", path = "/system/lib64/libc.so")
            val libm = makeLibrary(name = "libm.so", path = "/system/lib64/libm.so")
            librariesFlow.value = listOf(libc, libm)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSearchQuery("LIBC"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLibraries.size == 1 && it.filteredLibraries.first().name == "libc.so"
                }
                state.filteredLibraries shouldHaveSize 1
                state.filteredLibraries.first().name shouldBe "libc.so"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters by path`() = runTest {
            val sysLib = makeLibrary(name = "libc.so", path = "/system/lib64/libc.so")
            val appLib = makeLibrary(
                name = "libapp.so",
                path = "/data/app/libapp.so",
                isSystemLibrary = false,
            )
            librariesFlow.value = listOf(sysLib, appLib)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSearchQuery("/data/app"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLibraries.size == 1 && it.filteredLibraries.first().name == "libapp.so"
                }
                state.filteredLibraries shouldHaveSize 1
                state.filteredLibraries.first().name shouldBe "libapp.so"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank query returns all libraries`() = runTest {
            val lib1 = makeLibrary(name = "libc.so")
            val lib2 = makeLibrary(name = "libm.so", path = "/system/lib64/libm.so")
            librariesFlow.value = listOf(lib1, lib2)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSearchQuery("libc"))

            viewModel.uiState.test {
                awaitUntil { it.filteredLibraries.size == 1 }

                viewModel.sendEvent(LoadedLibrariesViewEvent.SetSearchQuery(""))
                val state = awaitUntil { it.filteredLibraries.size == 2 }
                state.filteredLibraries shouldHaveSize 2
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `SelectLibrary and DismissDetail events` {

        @Test
        fun `SelectLibrary sets the selected library`() = runTest {
            val lib = makeLibrary(name = "libc.so")

            viewModel.sendEvent(LoadedLibrariesViewEvent.SelectLibrary(lib))

            viewModel.uiState.value.selectedLibrary shouldBe lib
        }

        @Test
        fun `DismissDetail clears the selected library`() = runTest {
            viewModel.sendEvent(LoadedLibrariesViewEvent.SelectLibrary(makeLibrary()))
            viewModel.sendEvent(LoadedLibrariesViewEvent.DismissDetail)

            viewModel.uiState.value.selectedLibrary shouldBe null
        }
    }

    @Nested
    inner class `engine delegation` {

        @Test
        fun `Refresh event delegates to engine`() {
            viewModel.sendEvent(LoadedLibrariesViewEvent.Refresh)

            verify { engine.refresh() }
        }

        @Test
        fun `exportAsText delegates to engine`() {
            every { engine.exportAsText() } returns "Libraries Report"

            val result = viewModel.exportAsText()

            result shouldBe "Libraries Report"
            verify { engine.exportAsText() }
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
                errorFlow.value = "Scan failed"
                awaitUntil { it.error == "Scan failed" }.error shouldBe "Scan failed"
            }
        }
    }

    @Nested
    inner class `combined filtering` {

        @Test
        fun `type and search query combine`() = runTest {
            val nativeLibc = makeLibrary(
                name = "libc.so",
                path = "/system/lib64/libc.so",
                type = LibraryType.NATIVE_SO,
            )
            val nativeLibm = makeLibrary(
                name = "libm.so",
                path = "/system/lib64/libm.so",
                type = LibraryType.NATIVE_SO,
            )
            val dexClasses = makeLibrary(
                name = "classes.dex",
                path = "/data/app/classes.dex",
                type = LibraryType.DEX,
                isSystemLibrary = false,
            )
            librariesFlow.value = listOf(nativeLibc, nativeLibm, dexClasses)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSelectedType(LibraryType.NATIVE_SO))
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSearchQuery("libc"))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLibraries.size == 1 && it.filteredLibraries.first().name == "libc.so"
                }
                state.filteredLibraries shouldHaveSize 1
                state.filteredLibraries.first().name shouldBe "libc.so"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `type and showSystemLibs combine`() = runTest {
            val sysNative = makeLibrary(
                name = "libc.so",
                type = LibraryType.NATIVE_SO,
                isSystemLibrary = true,
            )
            val appNative = makeLibrary(
                name = "libapp.so",
                path = "/data/app/libapp.so",
                type = LibraryType.NATIVE_SO,
                isSystemLibrary = false,
            )
            val appDex = makeLibrary(
                name = "classes.dex",
                path = "/data/app/classes.dex",
                type = LibraryType.DEX,
                isSystemLibrary = false,
            )
            librariesFlow.value = listOf(sysNative, appNative, appDex)
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetSelectedType(LibraryType.NATIVE_SO))
            viewModel.sendEvent(LoadedLibrariesViewEvent.SetShowSystemLibs(false))

            viewModel.uiState.test {
                val state = awaitUntil {
                    it.filteredLibraries.size == 1 && it.filteredLibraries.first().name == "libapp.so"
                }
                state.filteredLibraries shouldHaveSize 1
                state.filteredLibraries.first().name shouldBe "libapp.so"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
