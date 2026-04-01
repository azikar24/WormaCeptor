package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType

enum class DelayType(val displayName: String) {
    NONE("None"),
    FIXED("Fixed"),
    RANGE("Range"),
}

data class MockRuleEditorState(
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
    val methodDropdownExpanded: Boolean = false,
) {
    val isValid: Boolean get() = name.isNotBlank() && urlPattern.isNotBlank()
}
