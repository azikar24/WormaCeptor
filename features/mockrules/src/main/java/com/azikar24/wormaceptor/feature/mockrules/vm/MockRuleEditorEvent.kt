package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType

/** User actions dispatched from the mock rule editor UI. */
internal sealed class MockRuleEditorEvent {

    // -- Basic info --
    data class NameChanged(val value: String) : MockRuleEditorEvent()

    // -- Request matching --
    data class UrlPatternChanged(val value: String) : MockRuleEditorEvent()
    data class MatchTypeChanged(val value: UrlMatchType) : MockRuleEditorEvent()
    data class MethodChanged(val value: String) : MockRuleEditorEvent()
    data class MethodDropdownExpandedChanged(val expanded: Boolean) : MockRuleEditorEvent()

    // -- Response --
    data class StatusCodeChanged(val value: String) : MockRuleEditorEvent()
    data class StatusMessageChanged(val value: String) : MockRuleEditorEvent()
    data class ContentTypeChanged(val value: String) : MockRuleEditorEvent()
    data class ResponseBodyChanged(val value: String) : MockRuleEditorEvent()

    // -- Delay --
    data class DelayTypeChanged(val value: DelayType) : MockRuleEditorEvent()
    data class DelayMsChanged(val value: String) : MockRuleEditorEvent()
    data class DelayMinMsChanged(val value: String) : MockRuleEditorEvent()
    data class DelayMaxMsChanged(val value: String) : MockRuleEditorEvent()
}
