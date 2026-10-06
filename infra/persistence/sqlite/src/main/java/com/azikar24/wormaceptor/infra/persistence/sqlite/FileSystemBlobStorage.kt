package com.azikar24.wormaceptor.infra.persistence.sqlite

import android.content.Context
import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.entities.BlobID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID

/** [BlobStorage] implementation that persists request/response bodies as files on disk. */
class FileSystemBlobStorage(private val context: Context) : BlobStorage {

    private val blobDir: File by lazy {
        File(context.filesDir, "wormaceptor_blobs").apply { mkdirs() }
    }

    override suspend fun saveBlob(stream: InputStream): BlobID = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val file = File(blobDir, id)
        file.outputStream().use { output ->
            stream.copyTo(output)
        }
        id
    }

    override suspend fun readBlob(id: BlobID): InputStream? = withContext(Dispatchers.IO) {
        val file = File(blobDir, id)
        if (file.exists()) file.inputStream() else null
    }

    override suspend fun deleteBlob(id: BlobID) {
        withContext(Dispatchers.IO) {
            val file = File(blobDir, id)
            if (file.exists()) file.delete()
        }
    }

    override suspend fun deleteUnreferenced(
        referenced: Set<BlobID>,
        createdBeforeMillis: Long,
    ): Int = withContext(Dispatchers.IO) {
        blobDir.listFiles().orEmpty()
            .filter { it.isFile && it.name !in referenced && it.lastModified() < createdBeforeMillis }
            .count { it.delete() }
    }
}
