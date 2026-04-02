package com.azikar24.wormaceptor.feature.threadviolation.vm

import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType

/** User actions dispatched from the Thread Violation UI. */
sealed class ThreadViolationViewEvent {
    data class SelectType(val type: ViolationType?) : ThreadViolationViewEvent()
    data class SelectViolation(val violation: ThreadViolation) : ThreadViolationViewEvent()
    data object DismissDetail : ThreadViolationViewEvent()
    data object ToggleMonitoring : ThreadViolationViewEvent()
    data object ClearViolations : ThreadViolationViewEvent()
}
