package com.azikar24.wormaceptor.feature.threadviolation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType

internal val ViolationType.color: Color
    get() = when (this) {
        ViolationType.DISK_READ -> WormaCeptorTokens.Colors.ThreadViolation.diskRead
        ViolationType.DISK_WRITE -> WormaCeptorTokens.Colors.ThreadViolation.diskWrite
        ViolationType.NETWORK -> WormaCeptorTokens.Colors.ThreadViolation.network
        ViolationType.SLOW_CALL -> WormaCeptorTokens.Colors.ThreadViolation.slowCall
        ViolationType.CUSTOM_SLOW_CODE -> WormaCeptorTokens.Colors.ThreadViolation.customSlowCode
    }

internal val ViolationType.icon: ImageVector
    get() = when (this) {
        ViolationType.DISK_READ -> Icons.Default.SaveAlt
        ViolationType.DISK_WRITE -> Icons.Default.Storage
        ViolationType.NETWORK -> Icons.Default.Cloud
        ViolationType.SLOW_CALL -> Icons.Default.SlowMotionVideo
        ViolationType.CUSTOM_SLOW_CODE -> Icons.Default.Speed
    }

internal val ViolationType.abbreviation: String
    get() = when (this) {
        ViolationType.DISK_READ -> "DR"
        ViolationType.DISK_WRITE -> "DW"
        ViolationType.NETWORK -> "N"
        ViolationType.SLOW_CALL -> "SC"
        ViolationType.CUSTOM_SLOW_CODE -> "CS"
    }
