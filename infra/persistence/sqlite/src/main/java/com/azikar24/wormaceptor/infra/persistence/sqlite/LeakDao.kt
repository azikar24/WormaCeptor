package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access object for memory leak record persistence. */
@Dao
interface LeakDao {
    /** Inserts a leak record into the database. */
    @Insert
    suspend fun insert(leak: LeakEntity)

    /** Observes all leak records ordered by newest first. */
    @Query("SELECT * FROM leaks ORDER BY timestamp DESC")
    fun getAll(): Flow<List<LeakEntity>>

    /** Deletes all leak records. */
    @Query("DELETE FROM leaks")
    suspend fun deleteAll()

    /** Returns the total number of stored leak records. */
    @Query("SELECT COUNT(*) FROM leaks")
    suspend fun getCount(): Int
}
