package com.azikar24.wormaceptor.core.engine

import androidx.paging.PagingData
import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.BlobID
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.util.UUID

/** Engine for querying captured network transactions, crashes, and body content. */
class QueryEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage,
    private val crashRepository: CrashRepository? = null,
) {

    /** Observes all network transactions as a reactive stream. */
    fun observeTransactions(): Flow<List<TransactionSummary>> {
        return repository.getAllTransactions()
    }

    /** Observes all captured crashes as a reactive stream. */
    fun observeCrashes(): Flow<List<Crash>> {
        return crashRepository?.observeCrashes() ?: flowOf(emptyList())
    }

    /** Searches transactions by URL or HTTP method. */
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

    /** Retrieves full transaction details by ID. */
    suspend fun getDetails(id: UUID) = repository.getTransactionById(id)

    /** Reads the text content of a stored request or response body. */
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

    /** Deletes all stored transactions. */
    suspend fun clear() = repository.clearAll()

    /** Deletes all stored crash reports. */
    suspend fun clearCrashes() = crashRepository?.clearCrashes()

    /** Returns all transactions for export (e.g., HAR, cURL). */
    suspend fun getAllTransactionsForExport() = repository.getAllTransactionsAsList()

    /** Deletes specific transactions by their IDs. */
    suspend fun deleteTransactions(ids: List<UUID>) = repository.deleteTransactions(ids)
}
