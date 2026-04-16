package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@Composable
internal fun ListContent(
    requests: List<WebViewRequest>,
    totalCount: Int,
    stats: WebViewRequestStats,
    resourceTypeFilter: Set<WebViewResourceType>,
    onToggleResourceTypeFilter: (WebViewResourceType) -> Unit,
    onClearFilters: () -> Unit,
    onRequestClick: (WebViewRequest) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    WormaCeptorLoadableContent(
        isLoading = isLoading,
        isEmpty = requests.isEmpty() && stats.totalRequests == 0,
        loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
        empty = {
            WormaCeptorEmptyState(
                title = stringResource(R.string.webviewmonitor_empty_state_enabled),
                subtitle = stringResource(R.string.webviewmonitor_empty_subtitle),
                icon = Icons.Default.Language,
                modifier = Modifier.fillMaxSize(),
            )
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = WormaCeptorTokens.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
            ) {
                if (stats.totalRequests > 0) {
                    item {
                        StatsRow(
                            stats = stats,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.lg,
                                vertical = WormaCeptorTokens.Spacing.sm,
                            ),
                        )
                    }
                }
                item {
                    FilterSection(
                        resourceTypeFilter = resourceTypeFilter,
                        onToggleResourceTypeFilter = onToggleResourceTypeFilter,
                        onClearFilters = onClearFilters,
                        modifier = Modifier.padding(horizontal = WormaCeptorTokens.Spacing.lg),
                    )
                }
                item {
                    CountText(
                        filteredCount = requests.size,
                        totalCount = totalCount,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.lg,
                            vertical = WormaCeptorTokens.Spacing.xs,
                        ),
                    )
                }
                items(requests, key = { it.id }) { request ->
                    WebViewRequestItem(
                        request = request,
                        onClick = { onRequestClick(request) },
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}
