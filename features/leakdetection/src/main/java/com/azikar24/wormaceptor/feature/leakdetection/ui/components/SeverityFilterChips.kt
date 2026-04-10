package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.feature.leakdetection.R

@Composable
internal fun SeverityFilterChips(
    selectedSeverity: LeakInfo.LeakSeverity?,
    onSelectSeverity: (LeakInfo.LeakSeverity?) -> Unit,
) {
    WormaCeptorFlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        FilterChip(
            selected = selectedSeverity == null,
            onClick = { onSelectSeverity(null) },
            label = { Text(stringResource(R.string.leakdetection_filter_all)) },
        )
        LeakInfo.LeakSeverity.entries.forEach { severity ->
            val color = when (severity) {
                LeakInfo.LeakSeverity.CRITICAL -> WormaCeptorTokens.Colors.LeakDetection.critical
                LeakInfo.LeakSeverity.HIGH -> WormaCeptorTokens.Colors.LeakDetection.high
                LeakInfo.LeakSeverity.MEDIUM -> WormaCeptorTokens.Colors.LeakDetection.medium
                LeakInfo.LeakSeverity.LOW -> WormaCeptorTokens.Colors.LeakDetection.low
            }
            val label = when (severity) {
                LeakInfo.LeakSeverity.CRITICAL -> stringResource(R.string.leakdetection_severity_critical)
                LeakInfo.LeakSeverity.HIGH -> stringResource(R.string.leakdetection_severity_high)
                LeakInfo.LeakSeverity.MEDIUM -> stringResource(R.string.leakdetection_severity_medium)
                LeakInfo.LeakSeverity.LOW -> stringResource(R.string.leakdetection_severity_low)
            }
            val isSelected = selectedSeverity == severity
            FilterChip(
                selected = isSelected,
                onClick = { onSelectSeverity(if (isSelected) null else severity) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                    selectedLabelColor = color,
                ),
            )
        }
    }
}

@Preview(name = "Severity Filters - Light")
@Composable
private fun SeverityFilterChipsPreview() {
    WormaCeptorTheme {
        Surface {
            SeverityFilterChips(
                selectedSeverity = null,
                onSelectSeverity = {},
            )
        }
    }
}

@Preview(name = "Severity Filters - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SeverityFilterChipsDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            SeverityFilterChips(
                selectedSeverity = LeakInfo.LeakSeverity.HIGH,
                onSelectSeverity = {},
            )
        }
    }
}
