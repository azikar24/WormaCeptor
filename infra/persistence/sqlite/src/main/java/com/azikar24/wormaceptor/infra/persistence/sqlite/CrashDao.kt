package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access object for crash report persistence. */
@Dao
interface CrashDao {
    /** Inserts a crash record into the database. */
    @Insert
    suspend fun insert(crash: CrashEntity)

    /** Observes all crash records ordered by newest first. */
    @Query("SELECT * FROM crashes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CrashEntity>>

    /** Deletes all crash records. */
    @Query("DELETE FROM crashes")
    suspend fun deleteAll()
}
