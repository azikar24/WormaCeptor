package com.azikar24.wormaceptor.core.engine

import androidx.paging.PagingData
import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.BlobID
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.util.UUID

class QueryEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage,
    private val crashRepository: CrashRepository? = null,
) {

    fun observeTransactions(): Flow<List<TransactionSummary>> {
        return repository.getAllTransactions()
    }

    fun observeCrashes(): Flow<List<com.azikar24.wormaceptor.domain.entities.Crash>> {
        return crashRepository?.observeCrashes() ?: flowOf(emptyList())
    }

    fun search(query: String): Flow<List<TransactionSummary>> {
        if (query.isBlank()) return observeTransactions()
        return repository.searchTransactions(query)
    }

    /**
     * Observe transactions with database-level pagination.
     * Use this for large datasets with 1000+ transactions for optimal performance.
     */
    fun observeTransactionsPaged(
        searchQuery: String? = null,
        filters: TransactionFilters = TransactionFilters(),
        pageSize: Int = 30,
    ): Flow<PagingData<TransactionSummary>> {
        return repository.getTransactionsPaged(
            searchQuery = searchQuery,
            filters = filters,
            pageSize = pageSize,
        )
    }

    /**
     * Get total count of transactions matching the search query.
     */
    suspend fun getTransactionCount(searchQuery: String? = null): Int {
        return repository.getTransactionCount(searchQuery)
    }

    suspend fun getDetails(id: UUID) = repository.getTransactionById(id)

    suspend fun getBody(blobId: BlobID): String? = withContext(Dispatchers.IO) {
        blobStorage.readBlob(blobId)?.use { input ->
            String(input.readBytes(), Charsets.UTF_8)
        }
    }

    /**
     * Gets raw bytes from blob storage - useful for binary content like PDFs
     */
    suspend fun getBodyBytes(blobId: BlobID): ByteArray? = withContext(Dispatchers.IO) {
        blobStorage.readBlob(blobId)?.use { input ->
            input.readBytes()
        }
    }

    suspend fun clear() = repository.clearAll()

    suspend fun clearCrashes() = crashRepository?.clearCrashes()

    suspend fun getAllTransactionsForExport() = repository.getAllTransactionsAsList()

    suspend fun deleteTransactions(ids: List<UUID>) = repository.deleteTransactions(ids)
}
