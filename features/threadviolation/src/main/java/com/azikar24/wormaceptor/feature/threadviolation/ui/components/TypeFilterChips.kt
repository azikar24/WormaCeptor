package com.azikar24.wormaceptor.feature.threadviolation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.feature.threadviolation.R

@Composable
internal fun TypeFilterChips(
    selectedType: ViolationType?,
    onTypeSelected: (ViolationType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorScrollableRow(modifier = modifier) {
        WormaCeptorChip(
            label = stringResource(R.string.threadviolation_filter_all),
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
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
            WormaCeptorChip(
                label = stringResource(labelRes),
                selected = isSelected,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                leadingIcon = type.icon,
                modifier = Modifier.semantics { selected = isSelected },
                accentColor = type.color,
            )
        }
    }
}
