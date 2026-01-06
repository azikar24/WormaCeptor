package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryTransactionRepository : TransactionRepository {
    private val transactions = ConcurrentHashMap<UUID, NetworkTransaction>()
    private val _transactionsFlow = MutableStateFlow<List<NetworkTransaction>>(emptyList())

    override fun getAllTransactions(): Flow<List<TransactionSummary>> {
        return _transactionsFlow.map { list ->
            list.map { it.toSummary() }.reversed()
        }
    }

    override suspend fun getTransactionById(id: UUID): NetworkTransaction? {
        return transactions[id]
    }

    override suspend fun saveTransaction(transaction: NetworkTransaction) {
        transactions[transaction.id] = transaction
        _transactionsFlow.value = transactions.values.toList()
    }

    override suspend fun getAllTransactionsAsList(): List<NetworkTransaction> {
        return transactions.values.toList()
    }

    override suspend fun clearAll() {
        transactions.clear()
        _transactionsFlow.value = emptyList()
    }

    override suspend fun deleteTransactionsBefore(timestamp: Long) {
        transactions.values.removeIf { it.timestamp < timestamp }
        _transactionsFlow.value = transactions.values.toList()
    }

    override fun searchTransactions(query: String): Flow<List<TransactionSummary>> {
        return getAllTransactions().map { list ->
            list.filter { 
                it.path.contains(query, ignoreCase = true) || 
                it.method.contains(query, ignoreCase = true) 
            }
        }
    }

    private fun NetworkTransaction.toSummary() = TransactionSummary(
        id = id,
        timestamp = timestamp,
        method = request.method,
        host = try { java.net.URI(request.url).host ?: "" } catch (e: Exception) { "" },
        path = try { java.net.URI(request.url).path ?: request.url } catch (e: Exception) { request.url },
        code = response?.code,
        tookMs = durationMs,
        hasRequestBody = request.bodySize > 0,
        hasResponseBody = (response?.bodySize ?: 0) > 0,
        status = status
    )
}
