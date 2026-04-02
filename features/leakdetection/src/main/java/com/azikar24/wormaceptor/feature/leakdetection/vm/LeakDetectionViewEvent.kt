package com.azikar24.wormaceptor.feature.leakdetection.vm

import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity

/** User actions dispatched from the Leak Detection UI. */
sealed class LeakDetectionViewEvent {
    data class SelectSeverity(val severity: LeakSeverity?) : LeakDetectionViewEvent()
    data class SelectLeak(val leak: LeakInfo) : LeakDetectionViewEvent()
    data object DismissDetail : LeakDetectionViewEvent()
    data object TriggerCheck : LeakDetectionViewEvent()
    data object ClearLeaks : LeakDetectionViewEvent()
}
