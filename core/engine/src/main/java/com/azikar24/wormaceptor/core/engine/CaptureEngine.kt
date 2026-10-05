package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.ExtensionContext
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import java.io.InputStream
import java.util.UUID

/** Engine responsible for capturing and storing network request/response data. */
class CaptureEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage,
    private val extensionRegistry: ExtensionRegistry? = null,
) {

    /** Records the start of a network request and returns a tracking UUID. */
    suspend fun startTransaction(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long = 0,
        id: UUID = UUID.randomUUID(),
        timestamp: Long = System.currentTimeMillis(),
    ): UUID {
        val blobId = bodyStream?.let { blobStorage.saveBlob(it) }

        val request = Request(url, method, headers, blobId, bodySize)
        val transaction = NetworkTransaction(id = id, request = request, timestamp = timestamp)

        repository.saveTransaction(transaction)
        return transaction.id
    }

    /**
     * Records the response for a previously started transaction. [durationMs] is the caller's measured
     * request time; when null it falls back to now minus the transaction's start timestamp.
     */
    suspend fun completeTransaction(
        id: UUID,
        code: Int,
        message: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long = 0,
        protocol: String? = null,
        tlsVersion: String? = null,
        error: String? = null,
        durationMs: Long? = null,
    ) {
        val original = repository.getTransactionById(id) ?: return

        val blobId = bodyStream?.let { blobStorage.saveBlob(it) }
        val response = Response(code, message, headers, blobId, error, protocol, tlsVersion, bodySize)

        val status = if (error != null || code >= 400) TransactionStatus.FAILED else TransactionStatus.COMPLETED
        val duration = durationMs ?: (System.currentTimeMillis() - original.timestamp)

        // Extract custom extensions from registered providers
        val extensions = extensionRegistry?.let { registry ->
            val context = ExtensionContext(
                request = original.request,
                response = response,
                durationMs = duration,
                timestamp = original.timestamp,
            )
            registry.extractAll(context)
        } ?: emptyMap()

        val updated = original.copy(
            response = response,
            status = status,
            durationMs = duration,
            extensions = extensions,
        )
        repository.saveTransaction(updated)
    }

    /** Deletes transactions older than the given timestamp threshold, along with their body blobs. */
    suspend fun cleanup(timestampThreshold: Long) {
        val blobIds = repository.getAllTransactionsAsList()
            .filter { it.timestamp < timestampThreshold }
            .flatMap { listOfNotNull(it.request.bodyRef, it.response?.bodyRef) }
        repository.deleteTransactionsBefore(timestampThreshold)
        blobIds.forEach { blobStorage.deleteBlob(it) }
    }
}
