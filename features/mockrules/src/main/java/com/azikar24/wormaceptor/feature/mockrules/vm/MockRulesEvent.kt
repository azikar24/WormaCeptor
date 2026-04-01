package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.mock.MockRule

/** User actions dispatched from the mock rules list UI. */
internal sealed class MockRulesEvent {
    data object ToggleMocking : MockRulesEvent()
    data class ToggleRule(val ruleId: String) : MockRulesEvent()
    data class DeleteRule(val ruleId: String) : MockRulesEvent()
    data object DeleteAllRules : MockRulesEvent()
    data class SaveRule(val rule: MockRule) : MockRulesEvent()
}
