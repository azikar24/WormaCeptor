package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MockRulesViewState(
    val rules: ImmutableList<MockRule> = persistentListOf(),
    val mockingEnabled: Boolean = true,
    val isLoading: Boolean = true,
)
