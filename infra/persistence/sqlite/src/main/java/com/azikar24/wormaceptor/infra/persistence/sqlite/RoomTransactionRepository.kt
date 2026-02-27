package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/** Room-backed [TransactionRepository] that persists network transactions to SQLite. */
class RoomTransactionRepository(
    private val dao: TransactionDao,
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionSummary>> {
        return dao.getAll().map { list ->
            list.map { entity -> entityToSummary(entity) }
        }
    }

    override suspend fun getTransactionById(id: UUID): NetworkTransaction? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun saveTransaction(transaction: NetworkTransaction) {
        dao.insert(TransactionEntity.fromDomain(transaction))
    }

    override suspend fun clearAll() {
        dao.deleteAll()
    }

    override suspend fun getAllTransactionsAsList(): List<NetworkTransaction> {
        return dao.getAllAsList().map { it.toDomain() }
    }

    override suspend fun deleteTransactionsBefore(timestamp: Long) {
        dao.deleteOlderThan(timestamp)
    }

    override suspend fun deleteTransactions(ids: List<UUID>) {
        dao.deleteByIds(ids)
    }

    override fun searchTransactions(query: String): Flow<List<TransactionSummary>> {
        return dao.search(query).map { list ->
            list.map { entity -> entityToSummary(entity) }
        }
    }

    override fun getTransactionsPaged(
        searchQuery: String?,
        filters: TransactionFilters,
        pageSize: Int,
    ): Flow<PagingData<TransactionSummary>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                prefetchDistance = pageSize / 2,
                enablePlaceholders = false,
                initialLoadSize = pageSize,
            ),
            pagingSourceFactory = {
                TransactionPagingSource(
                    transactionDao = dao,
                    searchQuery = searchQuery,
                    filters = filters,
                    entityToSummaryMapper = ::entityToSummary,
                )
            },
        ).flow
    }

    override suspend fun getTransactionCount(searchQuery: String?): Int {
        return dao.getTransactionCount(searchQuery)
    }

    private fun entityToSummary(entity: TransactionEntity): TransactionSummary {
        return TransactionSummary(
            id = entity.id,
            method = entity.reqMethod,
            host = extractHost(entity.reqUrl),
            path = extractPath(entity.reqUrl),
            code = entity.resCode,
            tookMs = entity.durationMs,
            hasRequestBody = entity.reqBodyRef != null,
            hasResponseBody = entity.resBodyRef != null,
            status = entity.status,
            timestamp = entity.timestamp,
        )
    }

    private fun extractHost(url: String): String {
        return try {
            java.net.URI(url).host ?: url
        } catch (_: Exception) {
            url
        }
    }

    private fun extractPath(url: String): String {
        return try {
            java.net.URI(url).path ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
