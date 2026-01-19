package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    override suspend fun deleteTransactions(ids: List<UUID>) {
        ids.forEach { transactions.remove(it) }
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

    override fun getTransactionsPaged(
        searchQuery: String?,
        filters: TransactionFilters,
        pageSize: Int
    ): Flow<PagingData<TransactionSummary>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize / 2,
                enablePlaceholders = false,
                initialLoadSize = pageSize
            ),
            pagingSourceFactory = {
                InMemoryPagingSource(
                    transactions = transactions.values.toList(),
                    searchQuery = searchQuery,
                    filters = filters
                )
            }
        ).flow
    }

    override suspend fun getTransactionCount(searchQuery: String?): Int {
        return if (searchQuery.isNullOrBlank()) {
            transactions.size
        } else {
            transactions.values.count { tx ->
                tx.request.url.contains(searchQuery, ignoreCase = true) ||
                tx.request.method.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    private fun NetworkTransaction.toSummary(): TransactionSummary {
        return TransactionSummary(
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

    private inner class InMemoryPagingSource(
        private val transactions: List<NetworkTransaction>,
        private val searchQuery: String?,
        private val filters: TransactionFilters
    ) : PagingSource<Int, TransactionSummary>() {

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionSummary> {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            return try {
                val filteredList = transactions
                    .sortedByDescending { it.timestamp }
                    .filter { tx ->
                        val matchesSearch = searchQuery.isNullOrBlank() ||
                            tx.request.url.contains(searchQuery, ignoreCase = true) ||
                            tx.request.method.contains(searchQuery, ignoreCase = true)

                        val statusRange = filters.statusRange
                        val matchesStatus = statusRange == null ||
                            (tx.response?.code?.let { it in statusRange } ?: false)

                        val matchesMethod = filters.method == null ||
                            tx.request.method.equals(filters.method, ignoreCase = true)

                        matchesSearch && matchesStatus && matchesMethod
                    }

                val start = page * pageSize
                val end = minOf(start + pageSize, filteredList.size)

                val pagedItems = if (start < filteredList.size) {
                    filteredList.subList(start, end).map { it.toSummary() }
                } else {
                    emptyList()
                }

                LoadResult.Page(
                    data = pagedItems,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (pagedItems.size < pageSize) null else page + 1
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, TransactionSummary>): Int? {
            return state.anchorPosition?.let { anchor ->
                state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                    ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
            }
        }
    }
}
