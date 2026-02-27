package com.azikar24.wormaceptor.domain.contracts

import androidx.paging.PagingData
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Repository contract for CRUD operations and queries on captured network transactions. */
interface TransactionRepository {
    /** Emits all transaction summaries, updating when the underlying data changes. */
    fun getAllTransactions(): Flow<List<TransactionSummary>>

    /** Returns the full [NetworkTransaction] for the given [id], or null if not found. */
    suspend fun getTransactionById(id: UUID): NetworkTransaction?

    /** Persists or updates a [NetworkTransaction]. */
    suspend fun saveTransaction(transaction: NetworkTransaction)

    /** Returns all stored transactions as a one-shot list. */
    suspend fun getAllTransactionsAsList(): List<NetworkTransaction>

    /** Deletes all stored transactions. */
    suspend fun clearAll()

    /** Deletes transactions with a timestamp earlier than the given epoch millis. */
    suspend fun deleteTransactionsBefore(timestamp: Long)

    /** Deletes the transactions matching the given [ids]. */
    suspend fun deleteTransactions(ids: List<UUID>)

    /** Emits transaction summaries matching the given search [query]. */
    fun searchTransactions(query: String): Flow<List<TransactionSummary>>

    /** Returns a paged stream of transaction summaries, filtered by [searchQuery] and [filters]. */
    fun getTransactionsPaged(
        searchQuery: String?,
        filters: TransactionFilters,
        pageSize: Int = 30,
    ): Flow<PagingData<TransactionSummary>>

    /** Returns the total number of transactions matching the optional [searchQuery]. */
    suspend fun getTransactionCount(searchQuery: String?): Int
}
