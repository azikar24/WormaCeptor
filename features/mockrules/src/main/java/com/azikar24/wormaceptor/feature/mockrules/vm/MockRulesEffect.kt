package com.azikar24.wormaceptor.feature.mockrules.vm

sealed class MockRulesEffect {
    data object NavigateBack : MockRulesEffect()

    /** Persisting the rule failed; the editor stays open so the user can retry. */
    data object SaveFailed : MockRulesEffect()

    /** The source transaction no longer exists; the editor opened empty. */
    data object TransactionNotFound : MockRulesEffect()

    /** The source response body was binary or too large and was not copied into the draft. */
    data object ResponseBodyOmitted : MockRulesEffect()
}
