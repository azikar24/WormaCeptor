package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.feature.leakdetection.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SeverityFilterChips(
    selectedSeverity: LeakInfo.LeakSeverity?,
    onSeveritySelected: (LeakInfo.LeakSeverity?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorFlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        FilterChip(
            selected = selectedSeverity == null,
            onClick = { onSeveritySelected(null) },
            label = { Text(stringResource(R.string.leakdetection_filter_all)) },
            modifier = Modifier.semantics { selected = selectedSeverity == null },
        )
        LeakInfo.LeakSeverity.entries.forEach { severity ->
            val color = when (severity) {
                LeakInfo.LeakSeverity.CRITICAL -> WormaCeptorTokens.Colors.LeakDetection.critical
                LeakInfo.LeakSeverity.HIGH -> WormaCeptorTokens.Colors.LeakDetection.high
                LeakInfo.LeakSeverity.MEDIUM -> WormaCeptorTokens.Colors.LeakDetection.medium
                LeakInfo.LeakSeverity.LOW -> WormaCeptorTokens.Colors.LeakDetection.low
            }
            val isSelected = selectedSeverity == severity
            FilterChip(
                selected = isSelected,
                onClick = { onSeveritySelected(if (isSelected) null else severity) },
                label = { Text(severity.name) },
                modifier = Modifier.semantics { selected = isSelected },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                    selectedLabelColor = color,
                ),
            )
        }
    }
}
