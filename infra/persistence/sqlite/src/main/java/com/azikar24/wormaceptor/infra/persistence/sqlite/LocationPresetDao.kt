package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationPresetDao {
    @Query("SELECT * FROM location_presets ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<LocationPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: LocationPresetEntity)

    @Query("DELETE FROM location_presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM location_presets WHERE isBuiltIn = 0")
    suspend fun deleteAllUserPresets()
}
