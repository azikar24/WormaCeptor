package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CrashDao {
    @Insert
    suspend fun insert(crash: CrashEntity)

    @Query("SELECT * FROM crashes ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CrashEntity>>

    @Query("DELETE FROM crashes")
    suspend fun deleteAll()
}
