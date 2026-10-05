package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.entities.BlobID
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** [BlobStorage] implementation that stores request/response bodies in memory. */
class InMemoryBlobStorage : BlobStorage {
    private val storage = ConcurrentHashMap<BlobID, Entry>()

    override suspend fun saveBlob(stream: InputStream): BlobID {
        val id = UUID.randomUUID().toString()
        val bytes = stream.readBytes()
        storage[id] = Entry(bytes, System.currentTimeMillis())
        return id
    }

    override suspend fun readBlob(id: BlobID): InputStream? {
        val entry = storage[id] ?: return null
        return ByteArrayInputStream(entry.bytes)
    }

    override suspend fun deleteBlob(id: BlobID) {
        storage.remove(id)
    }

    override suspend fun deleteUnreferenced(
        referenced: Set<BlobID>,
        createdBeforeMillis: Long,
    ): Int = storage.entries
        .filter { (id, entry) -> id !in referenced && entry.createdAtMillis < createdBeforeMillis }
        .count { (id, entry) -> storage.remove(id, entry) }

    // Identity equality on purpose: remove(id, entry) must only drop the exact entry that was read.
    @Suppress("UseDataClass")
    private class Entry(val bytes: ByteArray, val createdAtMillis: Long)
}
