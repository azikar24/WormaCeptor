package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
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
    WormaCeptorScrollableRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
    ) {
        WormaCeptorChip(
            label = stringResource(R.string.webviewmonitor_filter_all),
            selected = resourceTypeFilter.isEmpty(),
            onClick = {
                if (resourceTypeFilter.isNotEmpty()) onClearFilters()
            },
        )
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

@Preview(showBackground = true)
@Composable
private fun FilterSectionAllSelectedPreview() {
    WormaCeptorTheme {
        FilterSection(
            resourceTypeFilter = emptySet(),
            onToggleResourceTypeFilter = {},
            onClearFilters = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterSectionFilteredPreview() {
    WormaCeptorTheme {
        FilterSection(
            resourceTypeFilter = setOf(
                WebViewResourceType.SCRIPT,
                WebViewResourceType.IMAGE,
            ),
            onToggleResourceTypeFilter = {},
            onClearFilters = {},
        )
    }
}
