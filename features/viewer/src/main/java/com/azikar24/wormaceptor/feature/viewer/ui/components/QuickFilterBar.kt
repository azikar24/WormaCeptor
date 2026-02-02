package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Quick filters that can be applied to the transaction list.
 * Each filter has a label and a predicate function.
 */
enum class QuickFilter(val label: String) {
    ERRORS("Errors"),
    SLOW("Slow (>1s)"),
    LARGE(">100KB"),
    TODAY("Today"),
    JSON("JSON"),
    IMAGES("Images"),
    ;

    /**
     * Returns true if the transaction matches this filter criteria.
     */
    fun matches(transaction: TransactionSummary): Boolean = when (this) {
        ERRORS -> transaction.code != null && transaction.code in 400..599
        SLOW -> transaction.tookMs?.let { it > 1000 } ?: false
        LARGE -> false // Would need response size in TransactionSummary
        TODAY -> isToday(transaction.timestamp)
        JSON -> transaction.path.contains(".json", ignoreCase = true) ||
            transaction.path.contains("json", ignoreCase = true)
        IMAGES -> transaction.path.let { path ->
            path.endsWith(".png", ignoreCase = true) ||
                path.endsWith(".jpg", ignoreCase = true) ||
                path.endsWith(".jpeg", ignoreCase = true) ||
                path.endsWith(".gif", ignoreCase = true) ||
                path.endsWith(".webp", ignoreCase = true) ||
                path.endsWith(".svg", ignoreCase = true)
        }
    }

    private fun isToday(timestampMillis: Long): Boolean {
        val today = LocalDate.now()
        val transactionDate = Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return today == transactionDate
    }
}

/**
 * A horizontal row of quick filter chips.
 * Users can toggle multiple filters, which are applied with AND logic.
 */
@Composable
fun QuickFilterBar(
    activeFilters: Set<QuickFilter>,
    onFilterToggle: (QuickFilter) -> Unit,
    modifier: Modifier = Modifier,
    availableFilters: List<QuickFilter> = QuickFilter.entries,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        contentPadding = PaddingValues(horizontal = WormaCeptorDesignSystem.Spacing.lg),
    ) {
        items(availableFilters) { filter ->
            val isSelected = filter in activeFilters

            FilterChip(
                selected = isSelected,
                onClick = { onFilterToggle(filter) },
                label = {
                    Text(
                        text = filter.label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.viewer_quick_filter_selected),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

/**
 * Applies all active quick filters to a list of transactions.
 * Uses AND logic: a transaction must match ALL active filters.
 */
fun List<TransactionSummary>.applyQuickFilters(activeFilters: Set<QuickFilter>): List<TransactionSummary> {
    if (activeFilters.isEmpty()) return this

    return filter { transaction ->
        activeFilters.all { filter -> filter.matches(transaction) }
    }
}
