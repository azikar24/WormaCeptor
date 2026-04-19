package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

data class TransactionPagerViewState(
    val currentIndex: Int = 0,
    val navigationDirection: Int = 0,
    val transaction: NetworkTransaction? = null,
    val isLoading: Boolean = true,
    val canNavigatePrev: Boolean = false,
    val canNavigateNext: Boolean = false,
)
