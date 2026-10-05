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
    /** Raw status code input; kept as text so the field can be cleared while editing. */
    val statusCodeText: String = "200",
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
    /** True while a save is in flight; further save requests are ignored. */
    val isSaving: Boolean = false,
) {
    /** Whether [statusCodeText] is an HTTP status code in the 100..599 range. */
    val isStatusCodeValid: Boolean
        get() = statusCodeText.toIntOrNull()?.let { it in MinStatusCode..MaxStatusCode } == true

    /** Whether the editor holds enough valid input to save the rule. */
    val isValid: Boolean get() = name.isNotBlank() && urlPattern.isNotBlank() && isStatusCodeValid
}

private const val MinStatusCode = 100
private const val MaxStatusCode = 599
