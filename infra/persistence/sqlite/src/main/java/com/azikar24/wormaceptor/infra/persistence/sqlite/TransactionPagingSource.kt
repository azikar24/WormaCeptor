package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.entities.TransactionSummary

/**
 * PagingSource for loading transactions from the database with pagination.
 * Supports search queries and status/method filtering.
 */
class TransactionPagingSource(
    private val transactionDao: TransactionDao,
    private val searchQuery: String?,
    private val filters: TransactionFilters,
    private val entityToSummaryMapper: (TransactionEntity) -> TransactionSummary
) : PagingSource<Int, TransactionSummary>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionSummary> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            // Use consistent offset calculation based on page number
            // Note: Paging 3 may use different load sizes for initial vs append loads,
            // but we track by page number for consistency
            val offset = page * PAGE_SIZE

            val entities = transactionDao.getTransactionsPaged(
                offset = offset,
                limit = pageSize,
                searchQuery = searchQuery,
                statusMin = filters.statusRange?.first,
                statusMax = filters.statusRange?.last,
                method = filters.method
            )

            val transactions = entities.map(entityToSummaryMapper)

            LoadResult.Page(
                data = transactions,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (transactions.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        const val PAGE_SIZE = 30
    }

    override fun getRefreshKey(state: PagingState<Int, TransactionSummary>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
