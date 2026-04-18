package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MockRulesViewState(
    val rules: ImmutableList<MockRule> = persistentListOf(),
    val mockingEnabled: Boolean = true,
    val isLoading: Boolean = true,
    val editor: EditorState = EditorState(),
    val showDeleteAllDialog: Boolean = false,
    val pendingDeleteRule: MockRule? = null,
)

data class EditorState(
    val name: String = "",
    val urlPattern: String = "",
    val matchType: UrlMatchType = UrlMatchType.PREFIX,
    val method: String = "",
    val statusCode: Int = 200,
    val statusMessage: String = "OK",
    val contentType: String = "application/json",
    val responseBody: String = "",
    val delayType: DelayType = DelayType.NONE,
    val delayMs: String = "0",
    val delayMinMs: String = "0",
    val delayMaxMs: String = "1000",
    val isEditing: Boolean = false,
    val isLoaded: Boolean = false,
    val methodDropdownExpanded: Boolean = false,
) {
    val isValid: Boolean get() = name.isNotBlank() && urlPattern.isNotBlank()
}
