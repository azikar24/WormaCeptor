package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import java.io.InputStream
import java.util.UUID

class CaptureEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage
) {

    suspend fun startTransaction(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long = 0
    ): UUID {
        val blobId = bodyStream?.let { blobStorage.saveBlob(it) }
        
        val request = Request(url, method, headers, blobId, bodySize)
        val transaction = NetworkTransaction(request = request)
        
        repository.saveTransaction(transaction)
        return transaction.id
    }

    suspend fun completeTransaction(
        id: UUID,
        code: Int,
        message: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long = 0,
        protocol: String? = null,
        tlsVersion: String? = null,
        error: String? = null
    ) {
        val original = repository.getTransactionById(id) ?: return
        
        val blobId = bodyStream?.let { blobStorage.saveBlob(it) }
        val response = Response(code, message, headers, blobId, error, protocol, tlsVersion, bodySize)
        
        val status = if (error != null || code >= 400) TransactionStatus.FAILED else TransactionStatus.COMPLETED
        val duration = System.currentTimeMillis() - original.timestamp
        
        val updated = original.copy(
            response = response,
            status = status,
            durationMs = duration
        )
        repository.saveTransaction(updated)
    }
    
    suspend fun cleanup(timestampThreshold: Long) {
        repository.deleteTransactionsBefore(timestampThreshold)
    }
}
