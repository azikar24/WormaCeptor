package com.azikar24.wormaceptor.feature.filebrowser.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.webkit.MimeTypeMap
import com.azikar24.wormaceptor.domain.entities.FileContent
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import io.kotest.matchers.string.shouldContain as shouldContainString

class FileSystemDataSourceTest {

    private val tmpRoot = File(System.getProperty("java.io.tmpdir"), "fs_test_${System.nanoTime()}")
    private val filesDir = File(tmpRoot, "files").also { it.mkdirs() }
    private val cacheDir = File(tmpRoot, "cache").also { it.mkdirs() }
    private val codeCacheDir = File(tmpRoot, "code_cache").also { it.mkdirs() }
    private val externalFilesDir = File(tmpRoot, "external_files").also { it.mkdirs() }
    private val databasesDir = File(tmpRoot, "databases").also { it.mkdirs() }
    private val sharedPrefsDir = File(tmpRoot, "shared_prefs").also { it.mkdirs() }
    private val dbPlaceholder = File(databasesDir, "placeholder")

    private val appInfo = ApplicationInfo().apply {
        dataDir = tmpRoot.absolutePath
    }

    private val context = mockk<Context>(relaxed = true) {
        every { filesDir } returns this@FileSystemDataSourceTest.filesDir
        every { cacheDir } returns this@FileSystemDataSourceTest.cacheDir
        every { codeCacheDir } returns this@FileSystemDataSourceTest.codeCacheDir
        every { getExternalFilesDir(null) } returns externalFilesDir
        every { applicationInfo } returns appInfo
        every { getDatabasePath("placeholder") } returns dbPlaceholder
    }

    private val mimeTypeMap = mockk<MimeTypeMap> {
        every { getMimeTypeFromExtension(any()) } returns null
        every { getMimeTypeFromExtension("txt") } returns "text/plain"
        every { getMimeTypeFromExtension("json") } returns "application/json"
        every { getMimeTypeFromExtension("xml") } returns "application/xml"
        every { getMimeTypeFromExtension("html") } returns "text/html"
    }

    private lateinit var dataSource: FileSystemDataSource

    @BeforeEach
    fun setUp() {
        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap

        dataSource = FileSystemDataSource(context)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(MimeTypeMap::class)
        tmpRoot.deleteRecursively()
    }

    @Nested
    inner class `getRootDirectories` {

        @Test
        fun `includes all standard directories`() {
            val roots = dataSource.getRootDirectories()

            val names = roots.map { it.name }
            names shouldContain "Internal Files"
            names shouldContain "Cache"
            names shouldContain "Code Cache"
            names shouldContain "External Files"
            names shouldContain "App Data"
            names shouldContain "Databases"
            names shouldContain "SharedPreferences"
        }

        @Test
        fun `returns entries sorted by name`() {
            val roots = dataSource.getRootDirectories()

            val names = roots.map { it.name }
            names shouldBe names.sorted()
        }

        @Test
        fun `marks all entries as directories`() {
            val roots = dataSource.getRootDirectories()

            roots.forEach { it.isDirectory.shouldBeTrue() }
        }

        @Test
        fun `excludes databases directory when it does not exist`() {
            databasesDir.deleteRecursively()
            // getDatabasePath returns a file whose parent no longer exists
            val missingDbDir = File(File(tmpRoot, "no_databases"), "placeholder")
            every { context.getDatabasePath("placeholder") } returns missingDbDir

            val roots = dataSource.getRootDirectories()

            roots.none { it.name == "Databases" }.shouldBeTrue()
        }

        @Test
        fun `excludes shared prefs directory when it does not exist`() {
            sharedPrefsDir.deleteRecursively()

            val roots = dataSource.getRootDirectories()

            roots.none { it.name == "SharedPreferences" }.shouldBeTrue()
        }
    }

    @Nested
    inner class `listFiles` {

        @Test
        fun `returns empty list for non-existent directory`() {
            val result = dataSource.listFiles("/non/existent/path")

            result.shouldBeEmpty()
        }

        @Test
        fun `returns empty list for file path`() {
            val file = File(filesDir, "test.txt")
            file.writeText("content")

            val result = dataSource.listFiles(file.absolutePath)

            result.shouldBeEmpty()
        }

        @Test
        fun `lists directory contents`() {
            File(filesDir, "a.txt").writeText("hello")
            File(filesDir, "b.txt").writeText("world")
            File(filesDir, "subdir").mkdir()

            val result = dataSource.listFiles(filesDir.absolutePath)

            result shouldHaveSize 3
        }

        @Test
        fun `sorts directories first then files alphabetically`() {
            File(filesDir, "z_file.txt").writeText("z")
            File(filesDir, "a_file.txt").writeText("a")
            File(filesDir, "m_dir").mkdir()
            File(filesDir, "b_dir").mkdir()

            val result = dataSource.listFiles(filesDir.absolutePath)

            result shouldHaveSize 4
            // Directories first, sorted by name
            result[0].isDirectory.shouldBeTrue()
            result[0].name shouldBe "b_dir"
            result[1].isDirectory.shouldBeTrue()
            result[1].name shouldBe "m_dir"
            // Files next, sorted by name
            result[2].isDirectory.shouldBeFalse()
            result[2].name shouldBe "a_file.txt"
            result[3].isDirectory.shouldBeFalse()
            result[3].name shouldBe "z_file.txt"
        }

        @Test
        fun `populates permissions string`() {
            val file = File(filesDir, "test.txt")
            file.writeText("test")

            val result = dataSource.listFiles(filesDir.absolutePath)

            val entry = result.first { it.name == "test.txt" }
            entry.permissions.length shouldBe 3
            entry.permissions shouldContainString "r"
        }

        @Test
        fun `reports file size for files and zero for directories`() {
            File(filesDir, "data.txt").writeText("12345")
            File(filesDir, "subdir").mkdir()

            val result = dataSource.listFiles(filesDir.absolutePath)

            val fileEntry = result.first { it.name == "data.txt" }
            fileEntry.sizeBytes shouldBe 5L
            val dirEntry = result.first { it.name == "subdir" }
            dirEntry.sizeBytes shouldBe 0L
        }
    }

    @Nested
    inner class `readFile` {

        @Test
        fun `returns Error for non-existent file`() {
            val result = dataSource.readFile("/non/existent/file.txt")

            result.shouldBeInstanceOf<FileContent.Error>()
            (result as FileContent.Error).message shouldContainString "does not exist"
        }

        @Test
        fun `returns Error for directory`() {
            val dir = File(filesDir, "subdir")
            dir.mkdir()

            val result = dataSource.readFile(dir.absolutePath)

            result.shouldBeInstanceOf<FileContent.Error>()
            (result as FileContent.Error).message shouldContainString "directory"
        }

        @Test
        fun `reads text file`() {
            val file = File(filesDir, "readme.txt")
            file.writeText("Hello\nWorld")

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Text>()
            val text = result as FileContent.Text
            text.content shouldBe "Hello\nWorld"
            text.encoding shouldBe "UTF-8"
            text.lineCount shouldBe 2
        }

        @Test
        fun `reads various text extensions`() {
            val extensions = listOf("log", "html", "css", "js", "kt", "java", "md", "csv", "sql", "yml", "yaml")
            extensions.forEach { ext ->
                val file = File(filesDir, "test.$ext")
                file.writeText("content for $ext")

                val result = dataSource.readFile(file.absolutePath)

                result.shouldBeInstanceOf<FileContent.Text>()
                (result as FileContent.Text).content shouldBe "content for $ext"
                file.delete()
            }
        }

        @Test
        fun `reads json file and returns Json content`() {
            val file = File(filesDir, "data.json")
            file.writeText("""{"name":"test","value":42}""")

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Json>()
            val json = result as FileContent.Json
            json.rawContent shouldBe """{"name":"test","value":42}"""
            // org.json classes are Android stubs in JVM unit tests;
            // formatJson catches the exception and returns raw content with isValid=false
            json.formattedContent shouldContainString "name"
        }

        @Test
        fun `reads json array file and returns Json content`() {
            val file = File(filesDir, "array.json")
            file.writeText("""[1,2,3]""")

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Json>()
            val json = result as FileContent.Json
            json.rawContent shouldBe """[1,2,3]"""
        }

        @Test
        fun `reads invalid json file gracefully`() {
            val file = File(filesDir, "bad.json")
            file.writeText("{broken json")

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Json>()
            val json = result as FileContent.Json
            json.isValid.shouldBeFalse()
            json.formattedContent shouldBe "{broken json"
        }

        @Test
        fun `reads valid xml file`() {
            val file = File(filesDir, "config.xml")
            file.writeText("<root><item>hello</item></root>")

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Xml>()
            val xml = result as FileContent.Xml
            xml.isValid.shouldBeTrue()
            xml.rawContent shouldBe "<root><item>hello</item></root>"
        }

        @Test
        fun `reads invalid xml file gracefully`() {
            val file = File(filesDir, "bad.xml")
            file.writeText("<broken>xml")

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Xml>()
            val xml = result as FileContent.Xml
            xml.isValid.shouldBeFalse()
            xml.formattedContent shouldBe "<broken>xml"
        }

        @Test
        fun `returns TooLarge for text file exceeding size limit`() {
            val file = File(filesDir, "huge.txt")
            // Write just over 1MB
            val content = "x".repeat(1_048_577)
            file.writeText(content)

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.TooLarge>()
            val tooLarge = result as FileContent.TooLarge
            tooLarge.maxSize shouldBe 1_048_576L
        }

        @Test
        fun `returns TooLarge for json file exceeding size limit`() {
            val file = File(filesDir, "huge.json")
            val content = "x".repeat(1_048_577)
            file.writeText(content)

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.TooLarge>()
        }

        @Test
        fun `returns TooLarge for xml file exceeding size limit`() {
            val file = File(filesDir, "huge.xml")
            val content = "x".repeat(1_048_577)
            file.writeText(content)

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.TooLarge>()
        }

        @Test
        fun `returns TooLarge for any file exceeding binary limit`() {
            val file = File(filesDir, "huge.dat")
            // Write just over 5MB
            val bytes = ByteArray(5_242_881)
            file.writeBytes(bytes)

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.TooLarge>()
            val tooLarge = result as FileContent.TooLarge
            tooLarge.maxSize shouldBe 5_242_880L
        }

        @Test
        fun `reads unknown extension as binary`() {
            val file = File(filesDir, "data.dat")
            val bytes = byteArrayOf(0x00, 0x01, 0x02, 0x03)
            file.writeBytes(bytes)

            val result = dataSource.readFile(file.absolutePath)

            result.shouldBeInstanceOf<FileContent.Binary>()
            val binary = result as FileContent.Binary
            binary.bytes.size shouldBe 4
            binary.displaySize shouldBe 4
        }
    }

    @Nested
    inner class `getFileInfo` {

        @Test
        fun `returns correct metadata for a file`() {
            val file = File(filesDir, "info_test.txt")
            file.writeText("hello world")

            val result = dataSource.getFileInfo(file.absolutePath)

            result.name shouldBe "info_test.txt"
            result.path shouldBe file.absolutePath
            result.sizeBytes shouldBe 11L
            result.isReadable.shouldBeTrue()
            result.extension shouldBe "txt"
            result.parentPath shouldBe filesDir.absolutePath
        }

        @Test
        fun `returns zero size for directories`() {
            val dir = File(filesDir, "sub")
            dir.mkdir()

            val result = dataSource.getFileInfo(dir.absolutePath)

            result.sizeBytes shouldBe 0L
            result.name shouldBe "sub"
        }

        @Test
        fun `returns null extension for files without extension`() {
            val file = File(filesDir, "noext")
            file.writeText("test")

            val result = dataSource.getFileInfo(file.absolutePath)

            result.extension.shouldBeNull()
        }

        @Test
        fun `returns last modified timestamp`() {
            val file = File(filesDir, "timed.txt")
            file.writeText("time test")

            val result = dataSource.getFileInfo(file.absolutePath)

            result.lastModified shouldBe file.lastModified()
        }
    }

    @Nested
    inner class `deleteFile` {

        @Test
        fun `deletes existing file and returns true`() {
            val file = File(filesDir, "to_delete.txt")
            file.writeText("delete me")

            val result = dataSource.deleteFile(file.absolutePath)

            result.shouldBeTrue()
            file.exists().shouldBeFalse()
        }

        @Test
        fun `returns false for non-existent file`() {
            val result = dataSource.deleteFile("/non/existent/file.txt")

            result.shouldBeFalse()
        }

        @Test
        fun `prevents deletion of root directories`() {
            // filesDir is a root directory
            val result = dataSource.deleteFile(filesDir.absolutePath)

            result.shouldBeFalse()
            filesDir.exists().shouldBeTrue()
        }

        @Test
        fun `deletes empty directory`() {
            val emptyDir = File(filesDir, "empty_dir")
            emptyDir.mkdir()

            val result = dataSource.deleteFile(emptyDir.absolutePath)

            result.shouldBeTrue()
            emptyDir.exists().shouldBeFalse()
        }

        @Test
        fun `returns false for non-empty directory`() {
            val dir = File(filesDir, "non_empty")
            dir.mkdir()
            File(dir, "child.txt").writeText("child")

            val result = dataSource.deleteFile(dir.absolutePath)

            // File.delete() returns false for non-empty directories
            result.shouldBeFalse()
            dir.exists().shouldBeTrue()
        }
    }
}
