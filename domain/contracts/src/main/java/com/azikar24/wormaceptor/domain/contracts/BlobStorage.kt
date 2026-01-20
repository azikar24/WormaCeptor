package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.BlobID
import java.io.InputStream

interface BlobStorage {
    suspend fun saveBlob(stream: InputStream): BlobID
    suspend fun readBlob(id: BlobID): InputStream?
    suspend fun deleteBlob(id: BlobID)
}
