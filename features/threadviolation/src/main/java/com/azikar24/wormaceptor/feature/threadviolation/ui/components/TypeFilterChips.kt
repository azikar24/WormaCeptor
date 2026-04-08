package com.azikar24.wormaceptor.feature.threadviolation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.feature.threadviolation.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TypeFilterChips(
    selectedType: ViolationType?,
    onTypeSelected: (ViolationType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorFlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.threadviolation_filter_all)) },
            modifier = Modifier.semantics { selected = selectedType == null },
        )
        ViolationType.entries.forEach { type ->
            val labelRes = when (type) {
                ViolationType.DISK_READ -> R.string.threadviolation_filter_read
                ViolationType.DISK_WRITE -> R.string.threadviolation_filter_write
                ViolationType.NETWORK -> R.string.threadviolation_filter_network
                ViolationType.SLOW_CALL -> R.string.threadviolation_filter_slow
                ViolationType.CUSTOM_SLOW_CODE -> R.string.threadviolation_filter_custom
            }
            val isSelected = selectedType == type
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                label = { Text(stringResource(labelRes)) },
                leadingIcon = {
                    Icon(
                        type.icon,
                        contentDescription = null,
                        Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                },
                modifier = Modifier.semantics { selected = isSelected },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = type.color.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                    selectedLabelColor = type.color,
                    selectedLeadingIconColor = type.color,
                ),
            )
        }
    }
}
