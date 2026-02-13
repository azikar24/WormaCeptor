package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PushTemplateDao {
    @Query("SELECT * FROM push_templates ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<PushTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: PushTemplateEntity)

    @Query("DELETE FROM push_templates WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM push_templates")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM push_templates")
    suspend fun count(): Int
}
