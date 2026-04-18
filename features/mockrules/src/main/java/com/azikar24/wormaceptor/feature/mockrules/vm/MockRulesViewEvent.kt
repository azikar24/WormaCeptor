package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType

sealed class MockRulesViewEvent {

    sealed class List : MockRulesViewEvent() {
        data object ToggleMocking : List()
        data class ToggleRule(val ruleId: String) : List()
        data class DeleteRule(val ruleId: String) : List()
        data object DeleteAllRules : List()
        data object ShowDeleteAllDialog : List()
        data object DismissDeleteAllDialog : List()
        data class RequestDeleteRule(val rule: MockRule) : List()
        data object DismissDeleteRuleDialog : List()
    }

    sealed class Editor : MockRulesViewEvent() {
        data class LoadRule(val ruleId: String?) : Editor()
        data object SaveRule : Editor()

        // Basic info
        data class NameChanged(val value: String) : Editor()

        // Request matching
        data class UrlPatternChanged(val value: String) : Editor()
        data class MatchTypeChanged(val value: UrlMatchType) : Editor()
        data class MethodChanged(val value: String) : Editor()
        data class MethodDropdownExpandedChanged(val expanded: Boolean) : Editor()

        // Response
        data class StatusCodeChanged(val value: String) : Editor()
        data class StatusMessageChanged(val value: String) : Editor()
        data class ContentTypeChanged(val value: String) : Editor()
        data class ResponseBodyChanged(val value: String) : Editor()

        // Delay
        data class DelayTypeChanged(val value: DelayType) : Editor()
        data class DelayMsChanged(val value: String) : Editor()
        data class DelayMinMsChanged(val value: String) : Editor()
        data class DelayMaxMsChanged(val value: String) : Editor()
    }
}
