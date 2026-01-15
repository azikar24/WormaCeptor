package com.azikar24.wormaceptor.domain.contracts

import androidx.paging.PagingData
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionSummary>>
    suspend fun getTransactionById(id: UUID): NetworkTransaction?
    suspend fun saveTransaction(transaction: NetworkTransaction)
    suspend fun getAllTransactionsAsList(): List<NetworkTransaction>
    suspend fun clearAll()
    suspend fun deleteTransactionsBefore(timestamp: Long)
    suspend fun deleteTransactions(ids: List<UUID>)
    fun searchTransactions(query: String): Flow<List<TransactionSummary>>

    // Paging support
    fun getTransactionsPaged(
        searchQuery: String?,
        filters: TransactionFilters,
        pageSize: Int = 30
    ): Flow<PagingData<TransactionSummary>>

    suspend fun getTransactionCount(searchQuery: String?): Int
}
