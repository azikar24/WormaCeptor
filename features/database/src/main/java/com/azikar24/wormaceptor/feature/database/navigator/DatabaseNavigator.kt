package com.azikar24.wormaceptor.feature.database.navigator

import com.azikar24.wormaceptor.common.presentation.FeatureNavigator

interface DatabaseNavigator : FeatureNavigator {
    fun navigateToTables()
    fun navigateToTableData()
    fun navigateToQuery()
    fun navigateBack()
}
