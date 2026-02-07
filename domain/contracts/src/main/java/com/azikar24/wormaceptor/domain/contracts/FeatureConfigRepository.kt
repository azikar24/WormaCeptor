package com.azikar24.wormaceptor.domain.contracts

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and modifying feature configuration.
 * Allows runtime toggling of WormaCeptor features.
 */
interface FeatureConfigRepository {

    /**
     * Observes the current feature configuration.
     * Emits updates when the configuration changes.
     *
     * @return Flow of FeatureConfig that emits the default config on first read
     */
    fun observeConfig(): Flow<FeatureConfig>

    /**
     * Updates the feature configuration.
     *
     * @param config The new configuration to persist
     */
    suspend fun updateConfig(config: FeatureConfig)

    /**
     * Resets all feature toggles to their default values.
     */
    suspend fun resetToDefaults()
}
