package com.azikar24.wormaceptor.feature.location.data

import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Implementation of LocationSimulatorRepository that coordinates between
 * the data source (persistence) and the engine (actual mock location setting).
 */
class LocationRepositoryImpl(
    private val dataSource: LocationDataSource,
    private val engine: LocationSimulatorEngine,
) : LocationSimulatorRepository {

    override fun getPresets(): Flow<List<LocationPreset>> {
        return dataSource.observePresets().flowOn(Dispatchers.IO)
    }

    override suspend fun savePreset(preset: LocationPreset) {
        withContext(Dispatchers.IO) {
            dataSource.savePreset(preset)
        }
    }

    override suspend fun deletePreset(id: String) {
        withContext(Dispatchers.IO) {
            dataSource.deletePreset(id)
        }
    }

    override fun getCurrentMockLocation(): Flow<MockLocation?> {
        // Combine the persisted state with the engine state
        // Engine state is the source of truth for whether mock is active
        return combine(
            dataSource.observeCurrentMockLocation(),
            engine.currentMockLocation,
        ) { persisted, active ->
            // Return the active mock location from the engine if enabled
            // This ensures UI reflects actual state
            active ?: persisted
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun setMockLocation(location: MockLocation?) {
        withContext(Dispatchers.IO) {
            if (location == null) {
                engine.clearMockLocation()
                dataSource.setCurrentMockLocation(null)
            } else {
                val success = engine.setLocation(location)
                if (success) {
                    dataSource.setCurrentMockLocation(location)
                }
            }
        }
    }
}
