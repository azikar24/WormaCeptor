package com.azikar24.wormaceptor.infra.persistence.sqlite

import android.content.Context
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File

class FileSystemBlobStorageTest {

    @TempDir
    lateinit var filesDir: File

    private val storage by lazy {
        val context = mockk<Context> { every { filesDir } returns this@FileSystemBlobStorageTest.filesDir }
        FileSystemBlobStorage(context)
    }

    private val blobDir get() = File(filesDir, "wormaceptor_blobs")

    @Test
    fun `deleteUnreferenced removes old orphaned files and keeps referenced ones`() = runTest {
        val kept = storage.saveBlob(ByteArrayInputStream("kept".toByteArray()))
        val orphan = storage.saveBlob(ByteArrayInputStream("orphan".toByteArray()))
        File(blobDir, kept).setLastModified(OldTimestamp)
        File(blobDir, orphan).setLastModified(OldTimestamp)

        val deleted = storage.deleteUnreferenced(setOf(kept), createdBeforeMillis = Cutoff)

        deleted shouldBe 1
        storage.readBlob(kept).shouldNotBeNull().close()
        storage.readBlob(orphan).shouldBeNull()
    }

    @Test
    fun `deleteUnreferenced keeps unreferenced files written after the cutoff`() = runTest {
        val fresh = storage.saveBlob(ByteArrayInputStream("fresh".toByteArray()))
        File(blobDir, fresh).setLastModified(Cutoff + 1_000)

        val deleted = storage.deleteUnreferenced(emptySet(), createdBeforeMillis = Cutoff)

        deleted shouldBe 0
        storage.readBlob(fresh).shouldNotBeNull().close()
    }

    private companion object {
        const val OldTimestamp = 1_000_000_000_000L
        const val Cutoff = 1_500_000_000_000L
    }
}
