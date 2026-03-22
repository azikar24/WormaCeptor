package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access object for the currently active mock location. */
@Dao
interface MockLocationDao {
    /** Observes the current mock location state (singleton row). */
    @Query("SELECT * FROM mock_location WHERE id = 1")
    fun observe(): Flow<MockLocationEntity?>

    /** Inserts or updates the current mock location. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MockLocationEntity)

    /** Clears the mock location, effectively disabling location mocking. */
    @Query("DELETE FROM mock_location")
    suspend fun clear()
}
