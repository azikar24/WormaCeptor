package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeakDao {
    @Insert
    suspend fun insert(leak: LeakEntity)

    @Query("SELECT * FROM leaks ORDER BY timestamp DESC")
    fun getAll(): Flow<List<LeakEntity>>

    @Query("DELETE FROM leaks")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM leaks")
    suspend fun getCount(): Int
}
