package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MockRuleDao {
    @Query("SELECT * FROM mock_rules ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MockRuleEntity>>

    @Query("SELECT * FROM mock_rules WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MockRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MockRuleEntity)

    @Query("DELETE FROM mock_rules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM mock_rules")
    suspend fun deleteAll()
}
