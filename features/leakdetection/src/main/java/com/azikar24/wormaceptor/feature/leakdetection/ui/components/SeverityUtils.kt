package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LeakInfo

@Composable
internal fun severityColor(severity: LeakInfo.LeakSeverity): Color = when (severity) {
    LeakInfo.LeakSeverity.CRITICAL -> WormaCeptorTokens.Colors.LeakDetection.critical
    LeakInfo.LeakSeverity.HIGH -> WormaCeptorTokens.Colors.LeakDetection.high
    LeakInfo.LeakSeverity.MEDIUM -> WormaCeptorTokens.Colors.LeakDetection.medium
    LeakInfo.LeakSeverity.LOW -> WormaCeptorTokens.Colors.LeakDetection.low
}
