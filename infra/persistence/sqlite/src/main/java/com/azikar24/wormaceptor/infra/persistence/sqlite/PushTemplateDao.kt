package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access object for push notification template persistence. */
@Dao
interface PushTemplateDao {
    /** Observes all push templates ordered by newest first. */
    @Query("SELECT * FROM push_templates ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PushTemplateEntity>>

    /** Inserts or replaces a push notification template. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: PushTemplateEntity)

    /** Deletes a push template by its ID. */
    @Query("DELETE FROM push_templates WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Deletes all push templates. */
    @Query("DELETE FROM push_templates")
    suspend fun deleteAll()

    /** Returns the total number of stored push templates. */
    @Query("SELECT COUNT(*) FROM push_templates")
    suspend fun count(): Int
}
