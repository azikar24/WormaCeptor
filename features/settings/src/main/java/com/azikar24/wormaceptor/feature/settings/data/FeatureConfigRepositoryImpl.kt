package com.azikar24.wormaceptor.feature.settings.data

import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import com.azikar24.wormaceptor.domain.contracts.FeatureConfigRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of FeatureConfigRepository using DataStore.
 */
class FeatureConfigRepositoryImpl(
    private val dataStore: SettingsDataStore,
) : FeatureConfigRepository {

    override fun observeConfig(): Flow<FeatureConfig> {
        return dataStore.observeFeatureConfig()
    }

    override suspend fun updateConfig(config: FeatureConfig) {
        dataStore.saveFeatureConfig(config)
    }

    override suspend fun resetToDefaults() {
        dataStore.clear()
    }
}
