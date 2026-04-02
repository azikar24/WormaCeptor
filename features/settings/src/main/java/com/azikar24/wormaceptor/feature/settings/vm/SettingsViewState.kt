package com.azikar24.wormaceptor.feature.settings.vm

import com.azikar24.wormaceptor.domain.contracts.FeatureConfig

/**
 * Consolidated UI state for the Feature Toggles settings screen.
 *
 * @property featureConfig The current feature toggle configuration.
 */
data class SettingsViewState(
    val featureConfig: FeatureConfig = FeatureConfig.DEFAULT,
)
