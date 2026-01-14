package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.azikar24.wormaceptor.domain.contracts.CrashRepository

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.entities.BlobID

class QueryEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage,
    private val crashRepository: CrashRepository? = null
) {

    fun observeTransactions(): Flow<List<TransactionSummary>> {
        return repository.getAllTransactions()
    }

    fun observeCrashes(): Flow<List<com.azikar24.wormaceptor.domain.entities.Crash>> {
        return crashRepository?.observeCrashes() ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    fun search(query: String): Flow<List<TransactionSummary>> {
        if (query.isBlank()) return observeTransactions()
        return repository.searchTransactions(query)
    }
    
    
    suspend fun getDetails(id: UUID) = repository.getTransactionById(id)

    suspend fun getBody(blobId: BlobID): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        blobStorage.readBlob(blobId)?.use { input ->
            String(input.readBytes(), Charsets.UTF_8)
        }
    }

    /**
     * Gets raw bytes from blob storage - useful for binary content like PDFs
     */
    suspend fun getBodyBytes(blobId: BlobID): ByteArray? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        blobStorage.readBlob(blobId)?.use { input ->
            input.readBytes()
        }
    }
    
    
    suspend fun clear() = repository.clearAll()
    
    suspend fun clearCrashes() = crashRepository?.clearCrashes()
    
    suspend fun getAllTransactionsForExport() = repository.getAllTransactionsAsList()
}
