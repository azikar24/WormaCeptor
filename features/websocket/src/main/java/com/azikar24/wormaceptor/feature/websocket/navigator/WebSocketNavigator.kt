package com.azikar24.wormaceptor.feature.websocket.navigator

import com.azikar24.wormaceptor.common.presentation.FeatureNavigator

interface WebSocketNavigator : FeatureNavigator {
    fun navigateToMessages()
    fun navigateBack()
}
