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
        /**
         * Loads [ruleId] into the editor, or a blank draft when null.
         *
         * @property ruleId Id of the rule to edit, or null (or "new") for a blank draft.
         * @property loadKey Identifies one visit to the editor; a repeat with the same key (Activity
         *   recreation) is ignored so in-progress edits survive.
         */
        data class LoadRule(val ruleId: String?, val loadKey: String) : Editor()

        /**
         * Starts a new, unsaved rule pre-filled from a captured transaction.
         *
         * @property transactionId String form of the transaction UUID taken from the route.
         * @property loadKey Same contract as [LoadRule.loadKey].
         */
        data class LoadFromTransaction(val transactionId: String, val loadKey: String) : Editor()

        /** The UI displayed [EditorState.notice]; clears it. */
        data object NoticeShown : Editor()

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
