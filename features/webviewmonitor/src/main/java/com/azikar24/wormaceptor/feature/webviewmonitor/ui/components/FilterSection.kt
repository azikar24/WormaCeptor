package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorExpandableCard
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.R
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.getResourceTypeIcon

@OptIn(ExperimentalLayoutApi::class)
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
        WormaCeptorFlowRow(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            WebViewResourceType.entries
                .filter { it != WebViewResourceType.UNKNOWN }
                .forEach { type ->
                    FilterChip(
                        selected = resourceTypeFilter.contains(type),
                        onClick = { onToggleResourceTypeFilter(type) },
                        label = {
                            Text(type.displayName, style = MaterialTheme.typography.labelSmall)
                        },
                        leadingIcon = {
                            Icon(
                                getResourceTypeIcon(type),
                                contentDescription = null,
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                            )
                        },
                    )
                }
        }
    }
}
