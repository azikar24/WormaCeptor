package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MockLocationDao {
    @Query("SELECT * FROM mock_location WHERE id = 1")
    fun observe(): Flow<MockLocationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MockLocationEntity)

    @Query("DELETE FROM mock_location")
    suspend fun clear()
}
