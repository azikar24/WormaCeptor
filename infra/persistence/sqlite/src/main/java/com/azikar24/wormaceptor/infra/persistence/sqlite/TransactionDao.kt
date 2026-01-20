package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: UUID): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllAsList(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("DELETE FROM transactions WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<UUID>)

    @Query(
        "SELECT * FROM transactions WHERE reqUrl LIKE '%' || :query || '%' OR reqMethod LIKE '%' || :query || '%' ORDER BY timestamp DESC",
    )
    fun search(query: String): Flow<List<TransactionEntity>>

    // Paged queries for lazy loading
    @Query(
        """
        SELECT * FROM transactions
        WHERE (:searchQuery IS NULL OR reqUrl LIKE '%' || :searchQuery || '%' OR reqMethod LIKE '%' || :searchQuery || '%')
          AND (:statusMin IS NULL OR resCode >= :statusMin)
          AND (:statusMax IS NULL OR resCode <= :statusMax)
          AND (:method IS NULL OR reqMethod = :method)
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """,
    )
    suspend fun getTransactionsPaged(
        offset: Int,
        limit: Int,
        searchQuery: String?,
        statusMin: Int?,
        statusMax: Int?,
        method: String?,
    ): List<TransactionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM transactions
        WHERE (:searchQuery IS NULL OR reqUrl LIKE '%' || :searchQuery || '%' OR reqMethod LIKE '%' || :searchQuery || '%')
    """,
    )
    suspend fun getTransactionCount(searchQuery: String?): Int
}
