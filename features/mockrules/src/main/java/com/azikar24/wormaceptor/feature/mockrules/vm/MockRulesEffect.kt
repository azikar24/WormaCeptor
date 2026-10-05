package com.azikar24.wormaceptor.feature.mockrules.vm

sealed class MockRulesEffect {
    data object NavigateBack : MockRulesEffect()

    /** Persisting the rule failed; the editor stays open so the user can retry. */
    data object SaveFailed : MockRulesEffect()
}
