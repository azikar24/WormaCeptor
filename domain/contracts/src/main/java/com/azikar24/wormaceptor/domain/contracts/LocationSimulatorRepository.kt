package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing location simulation state and presets.
 */
interface LocationSimulatorRepository {

    /**
     * Observes all saved location presets (both built-in and user-created).
     * Emits updates when presets are added or removed.
     */
    fun getPresets(): Flow<List<LocationPreset>>

    /**
     * Saves a new location preset.
     *
     * @param preset The preset to save
     */
    suspend fun savePreset(preset: LocationPreset)

    /**
     * Deletes a location preset by its ID.
     * Built-in presets cannot be deleted.
     *
     * @param id The ID of the preset to delete
     */
    suspend fun deletePreset(id: String)

    /**
     * Observes the current mock location state.
     * Emits null when mock location is disabled, or the active mock location when enabled.
     */
    fun getCurrentMockLocation(): Flow<MockLocation?>

    /**
     * Sets or clears the current mock location.
     *
     * @param location The mock location to set, or null to disable mock locations
     */
    suspend fun setMockLocation(location: MockLocation?)
}
