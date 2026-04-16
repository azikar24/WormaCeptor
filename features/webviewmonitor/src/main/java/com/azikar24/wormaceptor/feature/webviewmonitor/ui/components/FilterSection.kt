package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorExpandableCard
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.R
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.getResourceTypeIcon

@Composable
internal fun FilterSection(
    resourceTypeFilter: Set<WebViewResourceType>,
    onToggleResourceTypeFilter: (WebViewResourceType) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    WormaCeptorExpandableCard(
        isExpanded = expanded,
        onToggle = { expanded = !expanded },
        modifier = modifier,
        header = {
            Text(
                text = stringResource(R.string.webviewmonitor_filter_by_type),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            if (resourceTypeFilter.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.webviewmonitor_action_clear_filters),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onClearFilters() }
                        .padding(horizontal = WormaCeptorTokens.Spacing.sm),
                )
            }
        },
    ) {
        WormaCeptorScrollableRow(
            contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.md),
        ) {
            WebViewResourceType.entries
                .filter { it != WebViewResourceType.UNKNOWN }
                .forEach { type ->
                    WormaCeptorChip(
                        label = type.displayName,
                        selected = resourceTypeFilter.contains(type),
                        onClick = { onToggleResourceTypeFilter(type) },
                        leadingIcon = getResourceTypeIcon(type),
                    )
                }
        }
    }
}
