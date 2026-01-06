package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.entities.BlobID
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryBlobStorage : BlobStorage {
    private val storage = ConcurrentHashMap<BlobID, ByteArray>()

    override suspend fun saveBlob(stream: InputStream): BlobID {
        val id = UUID.randomUUID().toString()
        val bytes = stream.readBytes()
        storage[id] = bytes
        return id
    }

    override suspend fun readBlob(id: BlobID): InputStream? {
        val bytes = storage[id] ?: return null
        return ByteArrayInputStream(bytes)
    }

    override suspend fun deleteBlob(id: BlobID) {
        storage.remove(id)
    }
}
