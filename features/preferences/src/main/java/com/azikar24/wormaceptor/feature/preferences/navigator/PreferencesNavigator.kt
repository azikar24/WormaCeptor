package com.azikar24.wormaceptor.feature.preferences.navigator

import com.azikar24.wormaceptor.common.presentation.FeatureNavigator

interface PreferencesNavigator : FeatureNavigator {
    fun navigateToDetail()
    fun navigateBack()
}
