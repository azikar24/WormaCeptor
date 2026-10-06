package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.BlobID
import java.io.InputStream

/** Storage abstraction for persisting and retrieving binary blobs (request/response bodies). */
interface BlobStorage {
    /** Persists a binary stream and returns the generated [BlobID] for later retrieval. */
    suspend fun saveBlob(stream: InputStream): BlobID

    /** Reads the blob identified by [id], returning null if it no longer exists. */
    suspend fun readBlob(id: BlobID): InputStream?

    /** Permanently deletes the blob identified by [id]. */
    suspend fun deleteBlob(id: BlobID)

    /**
     * Deletes every blob created before [createdBeforeMillis] whose id is not in [referenced],
     * reclaiming bodies orphaned by rows deleted without their blobs. Blobs created at or after
     * the cutoff are kept so in-flight captures are never swept.
     *
     * @return The number of blobs deleted.
     */
    suspend fun deleteUnreferenced(
        referenced: Set<BlobID>,
        createdBeforeMillis: Long,
    ): Int
}
