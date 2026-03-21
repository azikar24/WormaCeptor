package com.azikar24.wormaceptor.feature.filebrowser.data

import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FileSystemRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dataSource = mockk<FileSystemDataSource>(relaxed = true)
    private lateinit var repository: FileSystemRepositoryImpl

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FileSystemRepositoryImpl(dataSource)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    companion object {
        private fun makeEntry(
            name: String = "file.txt",
            path: String = "/data/file.txt",
            isDirectory: Boolean = false,
        ) = FileEntry(
            name = name,
            path = path,
            isDirectory = isDirectory,
            sizeBytes = 100L,
            lastModified = 1000L,
            permissions = "rw-",
            isReadable = true,
            isWritable = false,
        )
    }

    @Nested
    inner class `listFiles` {

        @Test
        fun `delegates to data source`() = runTest {
            val entries = listOf(
                makeEntry("a.txt", "/data/a.txt"),
                makeEntry("b.txt", "/data/b.txt"),
            )
            every { dataSource.listFiles("/data") } returns entries

            val result = repository.listFiles("/data")

            result shouldHaveSize 2
            result[0].name shouldBe "a.txt"
            result[1].name shouldBe "b.txt"
            verify { dataSource.listFiles("/data") }
        }
    }

    @Nested
    inner class `readFile` {

        @Test
        fun `delegates to data source for text file`() = runTest {
            every { dataSource.readFile("/data/test.txt") } returns FileContent.Text("hello")

            val result = repository.readFile("/data/test.txt")

            result.shouldBeInstanceOf<FileContent.Text>()
            (result as FileContent.Text).content shouldBe "hello"
            verify { dataSource.readFile("/data/test.txt") }
        }

        @Test
        fun `delegates to data source for error case`() = runTest {
            every { dataSource.readFile("/data/missing.txt") } returns FileContent.Error("not found")

            val result = repository.readFile("/data/missing.txt")

            result.shouldBeInstanceOf<FileContent.Error>()
            (result as FileContent.Error).message shouldBe "not found"
        }
    }

    @Nested
    inner class `getFileInfo` {

        @Test
        fun `delegates to data source`() = runTest {
            val info = FileInfo(
                name = "test.txt",
                path = "/data/test.txt",
                sizeBytes = 256L,
                lastModified = 5000L,
                mimeType = "text/plain",
                isReadable = true,
                isWritable = false,
                extension = "txt",
                parentPath = "/data",
            )
            every { dataSource.getFileInfo("/data/test.txt") } returns info

            val result = repository.getFileInfo("/data/test.txt")

            result.name shouldBe "test.txt"
            result.sizeBytes shouldBe 256L
            result.mimeType shouldBe "text/plain"
            verify { dataSource.getFileInfo("/data/test.txt") }
        }
    }

    @Nested
    inner class `getRootDirectories` {

        @Test
        fun `delegates to data source`() = runTest {
            val roots = listOf(
                makeEntry("files", "/data/files", isDirectory = true),
                makeEntry("cache", "/data/cache", isDirectory = true),
            )
            every { dataSource.getRootDirectories() } returns roots

            val result = repository.getRootDirectories()

            result shouldHaveSize 2
            result[0].name shouldBe "files"
            result[1].name shouldBe "cache"
            verify { dataSource.getRootDirectories() }
        }
    }

    @Nested
    inner class `deleteFile` {

        @Test
        fun `returns true when data source succeeds`() = runTest {
            every { dataSource.deleteFile("/data/file.txt") } returns true

            val result = repository.deleteFile("/data/file.txt")

            result.shouldBeTrue()
            verify { dataSource.deleteFile("/data/file.txt") }
        }

        @Test
        fun `returns false when data source fails`() = runTest {
            every { dataSource.deleteFile("/data/file.txt") } returns false

            val result = repository.deleteFile("/data/file.txt")

            result.shouldBeFalse()
            verify { dataSource.deleteFile("/data/file.txt") }
        }
    }
}
