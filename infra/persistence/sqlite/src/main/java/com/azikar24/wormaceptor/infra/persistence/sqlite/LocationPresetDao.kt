package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access object for location simulator preset persistence. */
@Dao
interface LocationPresetDao {
    /** Observes all presets with built-in presets listed first, then sorted alphabetically. */
    @Query("SELECT * FROM location_presets ORDER BY isBuiltIn DESC, name ASC")
    fun observeAll(): Flow<List<LocationPresetEntity>>

    /** Inserts or replaces a location preset. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: LocationPresetEntity)

    /** Deletes a user-created preset by ID; built-in presets are protected. */
    @Query("DELETE FROM location_presets WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteById(id: String)

    /** Deletes all user-created presets while preserving built-in ones. */
    @Query("DELETE FROM location_presets WHERE isBuiltIn = 0")
    suspend fun deleteAllUserPresets()
}
