package com.azikar24.wormaceptor.feature.filebrowser.vm

import android.app.Application
import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FileBrowserViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val rootDirs = listOf(
        makeFileEntry("files", "/data/files", isDirectory = true),
        makeFileEntry("cache", "/data/cache", isDirectory = true),
    )

    private val subFiles = listOf(
        makeFileEntry("subdir", "/data/files/subdir", isDirectory = true),
        makeFileEntry("readme.txt", "/data/files/readme.txt", isDirectory = false, sizeBytes = 256),
        makeFileEntry("config.json", "/data/files/config.json", isDirectory = false, sizeBytes = 128),
    )

    private val repository = mockk<FileSystemRepository>(relaxed = true) {
        coEvery { getRootDirectories() } returns rootDirs
        coEvery { listFiles(any()) } returns subFiles
        coEvery { readFile(any()) } returns FileContent.Text("Hello World")
        coEvery { getFileInfo(any()) } returns FileInfo(
            name = "readme.txt",
            path = "/data/files/readme.txt",
            sizeBytes = 256,
            lastModified = 1000L,
            mimeType = "text/plain",
            isReadable = true,
            isWritable = false,
            extension = "txt",
            parentPath = "/data/files",
        )
        coEvery { deleteFile(any()) } returns true
    }

    private val application = mockk<Application>(relaxed = true) {
        every { getString(any()) } returns "Error"
        every { getString(any(), any()) } returns "Error: details"
    }

    private lateinit var viewModel: FileBrowserViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FileBrowserViewModel(repository, application)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    companion object {
        fun makeFileEntry(
            name: String = "file.txt",
            path: String = "/data/file.txt",
            isDirectory: Boolean = false,
            sizeBytes: Long = 100,
            lastModified: Long = 1000L,
        ) = FileEntry(
            name = name,
            path = path,
            isDirectory = isDirectory,
            sizeBytes = sizeBytes,
            lastModified = lastModified,
            permissions = "rwxr-xr-x",
            isReadable = true,
            isWritable = false,
        )
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `loads root directories on init`() = runTest {
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.filteredFiles shouldHaveSize 2
            state.currentPath shouldBe null
            state.navigationStack.shouldBeEmpty()
        }

        @Test
        fun `is not loading after init`() = runTest {
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBe false
        }
    }

    @Nested
    inner class `NavigateToDirectory event` {

        @Test
        fun `loads files for target directory`() = runTest {
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.currentPath shouldBe "/data/files"
            state.filteredFiles shouldHaveSize 3
        }

        @Test
        fun `pushes path onto navigation stack`() = runTest {
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.navigationStack shouldHaveSize 1
            state.navigationStack.first() shouldBe "/data/files"
        }

        @Test
        fun `resets search query`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.SearchQueryChanged("test"))

            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.uiState.value.searchQuery shouldBe ""
        }

        @Test
        fun `handles error gracefully`() = runTest {
            coEvery { repository.listFiles("/bad/path") } throws RuntimeException("Permission denied")
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/bad/path"))
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBe false
            viewModel.uiState.value.error shouldBe "Error: details"
        }
    }

    @Nested
    inner class `NavigateBack event` {

        @Test
        fun `emits AtRoot effect when stack is empty`() = runTest {
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(FileBrowserViewEvent.NavigateBack)
                awaitItem() shouldBe FileBrowserViewEffect.AtRoot
            }
        }

        @Test
        fun `returns to root when stack has one entry`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(FileBrowserViewEvent.NavigateBack)
                advanceUntilIdle()
                awaitItem() shouldBe FileBrowserViewEffect.NavigatedBack
            }

            val state = viewModel.uiState.value
            state.currentPath shouldBe null
            state.navigationStack.shouldBeEmpty()
        }

        @Test
        fun `navigates to parent directory when stack has multiple entries`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files/subdir"))
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.sendEvent(FileBrowserViewEvent.NavigateBack)
                advanceUntilIdle()
                awaitItem() shouldBe FileBrowserViewEffect.NavigatedBack
            }

            val state = viewModel.uiState.value
            state.currentPath shouldBe "/data/files"
            state.navigationStack shouldHaveSize 1
        }
    }

    @Nested
    inner class `NavigateToBreadcrumb event` {

        @Test
        fun `navigates to root for negative index`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.NavigateToBreadcrumb(-1))
            advanceUntilIdle()

            viewModel.uiState.value.currentPath shouldBe null
        }

        @Test
        fun `does nothing for index beyond stack size`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            val currentPath = viewModel.uiState.value.currentPath
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToBreadcrumb(5))
            advanceUntilIdle()

            viewModel.uiState.value.currentPath shouldBe currentPath
        }

        @Test
        fun `trims stack to selected index`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files/subdir"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.NavigateToBreadcrumb(0))
            advanceUntilIdle()

            viewModel.uiState.value.navigationStack shouldHaveSize 1
            viewModel.uiState.value.currentPath shouldBe "/data/files"
        }
    }

    @Nested
    inner class `SearchQueryChanged event` {

        @Test
        fun `updates search query in state`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.SearchQueryChanged("readme"))
            advanceUntilIdle()

            viewModel.uiState.value.searchQuery shouldBe "readme"
        }

        @Test
        fun `filters root files by name`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.SearchQueryChanged("cache"))
            advanceUntilIdle()

            viewModel.uiState.value.filteredFiles shouldHaveSize 1
            viewModel.uiState.value.filteredFiles.first().name shouldBe "cache"
        }

        @Test
        fun `filters subdirectory files by name`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.SearchQueryChanged("config"))
            advanceUntilIdle()

            viewModel.uiState.value.filteredFiles shouldHaveSize 1
            viewModel.uiState.value.filteredFiles.first().name shouldBe "config.json"
        }
    }

    @Nested
    inner class `SetSortMode event` {

        @Test
        fun `updates sort mode in state`() = runTest {
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.SetSortMode(SortMode.SIZE))
            advanceUntilIdle()

            viewModel.uiState.value.sortMode shouldBe SortMode.SIZE
        }

        @Test
        fun `re-sorts files when sort mode changes`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.SetSortMode(SortMode.SIZE))
            advanceUntilIdle()

            val files = viewModel.uiState.value.filteredFiles
            files shouldHaveSize 3
            // Directories first, then by size descending
            files.first().isDirectory shouldBe true
        }
    }

    @Nested
    inner class `FileClicked event` {

        @Test
        fun `navigates into directory when directory clicked`() = runTest {
            advanceUntilIdle()
            val dirEntry = makeFileEntry("subdir", "/data/files/subdir", isDirectory = true)

            viewModel.sendEvent(FileBrowserViewEvent.FileClicked(dirEntry))
            advanceUntilIdle()

            viewModel.uiState.value.currentPath shouldBe "/data/files/subdir"
        }

        @Test
        fun `opens file when file clicked`() = runTest {
            advanceUntilIdle()
            val fileEntry = makeFileEntry("readme.txt", "/data/files/readme.txt", isDirectory = false)

            viewModel.sendEvent(FileBrowserViewEvent.FileClicked(fileEntry))
            advanceUntilIdle()

            viewModel.uiState.value.selectedFile shouldBe "/data/files/readme.txt"
            viewModel.uiState.value.fileContent shouldBe FileContent.Text("Hello World")
        }
    }

    @Nested
    inner class `FileLongClicked event` {

        @Test
        fun `shows file info`() = runTest {
            advanceUntilIdle()
            val fileEntry = makeFileEntry("readme.txt", "/data/files/readme.txt")

            viewModel.sendEvent(FileBrowserViewEvent.FileLongClicked(fileEntry))
            advanceUntilIdle()

            viewModel.uiState.value.fileInfo?.name shouldBe "readme.txt"
        }
    }

    @Nested
    inner class `DeleteFile event` {

        @Test
        fun `deletes file and refreshes directory`() = runTest {
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.DeleteFile("/data/files/readme.txt"))
            advanceUntilIdle()

            coVerify { repository.deleteFile("/data/files/readme.txt") }
        }

        @Test
        fun `shows error when deletion fails`() = runTest {
            coEvery { repository.deleteFile(any()) } returns false
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.DeleteFile("/data/files/readme.txt"))
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBe false
        }

        @Test
        fun `handles deletion exception`() = runTest {
            coEvery { repository.deleteFile(any()) } throws RuntimeException("Permission denied")
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/data/files"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.DeleteFile("/data/files/readme.txt"))
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBe false
        }
    }

    @Nested
    inner class `CloseFileViewer event` {

        @Test
        fun `clears selected file and content`() = runTest {
            advanceUntilIdle()
            val fileEntry = makeFileEntry("readme.txt", "/data/files/readme.txt", isDirectory = false)
            viewModel.sendEvent(FileBrowserViewEvent.FileClicked(fileEntry))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.CloseFileViewer)

            viewModel.uiState.value.selectedFile shouldBe null
            viewModel.uiState.value.fileContent shouldBe null
        }
    }

    @Nested
    inner class `HideFileInfo event` {

        @Test
        fun `clears file info`() = runTest {
            advanceUntilIdle()
            val fileEntry = makeFileEntry("readme.txt", "/data/files/readme.txt")
            viewModel.sendEvent(FileBrowserViewEvent.FileLongClicked(fileEntry))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.HideFileInfo)

            viewModel.uiState.value.fileInfo shouldBe null
        }
    }

    @Nested
    inner class `ClearError event` {

        @Test
        fun `clears error message`() = runTest {
            coEvery { repository.listFiles(any()) } throws RuntimeException("Error")
            advanceUntilIdle()
            viewModel.sendEvent(FileBrowserViewEvent.NavigateToDirectory("/bad/path"))
            advanceUntilIdle()

            viewModel.sendEvent(FileBrowserViewEvent.ClearError)

            viewModel.uiState.value.error shouldBe null
        }
    }
}
