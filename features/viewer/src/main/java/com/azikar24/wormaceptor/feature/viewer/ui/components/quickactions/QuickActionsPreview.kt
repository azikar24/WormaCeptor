/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.quickactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import java.util.UUID

/**
 * Preview composable showcasing all Quick Actions UI components.
 */
@Preview(showBackground = true, widthDp = 400, heightDp = 900)
@Composable
fun QuickActionsPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(WormaCeptorDesignSystem.Spacing.lg)
            ) {
                // Section: Checkboxes
                SectionHeader("Selection Checkboxes")
                CheckboxShowcase()

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                // Section: Quick Filter Bar
                SectionHeader("Quick Filter Bar")
                QuickFilterBarShowcase()

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                // Section: Bulk Action Bar
                SectionHeader("Bulk Action Bar")
                BulkActionBarShowcase()

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                // Section: Context Menu Actions
                SectionHeader("Context Menu Actions")
                ContextMenuActionsShowcase()

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                // Section: Selectable Transaction Items
                SectionHeader("Selectable Transaction Items")
                SelectableItemsShowcase()

                Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for floating bar
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
    }
}

@Composable
private fun CheckboxShowcase() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular checkbox - unselected
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SelectionCheckbox(
                isSelected = false,
                accentColor = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "Unselected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Circular checkbox - selected
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SelectionCheckbox(
                isSelected = true,
                accentColor = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "Selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Square checkbox - unselected
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SquareSelectionCheckbox(
                isSelected = false,
                accentColor = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "Square",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Square checkbox - selected
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SquareSelectionCheckbox(
                isSelected = true,
                accentColor = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "Selected",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickFilterBarShowcase() {
    var activeFilters by remember { mutableStateOf(setOf(QuickFilter.ERRORS, QuickFilter.TODAY)) }

    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)) {
        // Standard filter bar
        Text(
            text = "Standard Filter Bar",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        QuickFilterBar(
            activeFilters = activeFilters,
            onFilterToggle = { filter ->
                activeFilters = if (filter in activeFilters) {
                    activeFilters - filter
                } else {
                    activeFilters + filter
                }
            },
            onClearAll = { activeFilters = emptySet() }
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Individual filter chips
        Text(
            text = "Filter Chip States",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
        ) {
            QuickFilterChip(
                filter = QuickFilter.ERRORS,
                isSelected = false,
                onClick = {}
            )
            QuickFilterChip(
                filter = QuickFilter.ERRORS,
                isSelected = true,
                onClick = {}
            )
            MaterialQuickFilterChip(
                filter = QuickFilter.SLOW,
                isSelected = true,
                onClick = {}
            )
        }
    }
}

@Composable
private fun BulkActionBarShowcase() {
    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg)) {
        // Full bulk action bar
        Text(
            text = "Full Action Bar (3 of 10 selected)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BulkActionBar(
            selectedCount = 3,
            totalCount = 10,
            onSelectAll = {},
            onShare = {},
            onExport = {},
            onDelete = {},
            onCancel = {}
        )

        // All selected state
        Text(
            text = "All Selected State",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BulkActionBar(
            selectedCount = 10,
            totalCount = 10,
            onSelectAll = {},
            onShare = {},
            onExport = {},
            onDelete = {},
            onCancel = {}
        )

        // Compact variant
        Text(
            text = "Compact Variant",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CompactBulkActionBar(
            selectedCount = 5,
            onShare = {},
            onExport = {},
            onDelete = {},
            onCancel = {}
        )

        // Floating variant
        Text(
            text = "Floating Variant",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FloatingBulkActionBar(
                selectedCount = 2,
                onShare = {},
                onExport = {},
                onDelete = {},
                onCancel = {}
            )
        }
    }
}

@Composable
private fun ContextMenuActionsShowcase() {
    val actions = listOf(
        QuickAction(
            id = "copy",
            label = "Copy URL",
            icon = Icons.Outlined.ContentCopy,
            onClick = {}
        ),
        QuickAction(
            id = "share",
            label = "Share",
            icon = Icons.Outlined.Share,
            onClick = {}
        ),
        QuickAction(
            id = "curl",
            label = "Copy as cURL",
            icon = Icons.Outlined.Code,
            onClick = {}
        ),
        QuickAction(
            id = "replay",
            label = "Replay Request",
            icon = Icons.Outlined.Replay,
            onClick = {}
        ),
        QuickAction(
            id = "delete",
            label = "Delete",
            icon = Icons.Outlined.Delete,
            destructive = true,
            onClick = {}
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        Text(
            text = "Custom Context Menu (visible)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CustomContextMenu(
            visible = true,
            actions = actions,
            onDismiss = {}
        )
    }
}

@Composable
private fun SelectableItemsShowcase() {
    val sampleTransactions = listOf(
        createSampleTransaction("GET", "/api/users", 200, 150),
        createSampleTransaction("POST", "/api/orders", 201, 320),
        createSampleTransaction("GET", "/api/products/search", 500, 1200),
        createSampleTransaction("DELETE", "/api/users/123", 404, 89)
    )

    var selectedIds by remember { mutableStateOf(setOf(sampleTransactions[1].id)) }
    var isSelectionMode by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)) {
        Text(
            text = "Selection Mode: ${if (isSelectionMode) "ON" else "OFF"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        sampleTransactions.forEach { transaction ->
            SelectableTransactionItem(
                transaction = transaction,
                isSelected = transaction.id in selectedIds,
                isSelectionMode = isSelectionMode,
                onClick = {
                    if (isSelectionMode) {
                        selectedIds = if (transaction.id in selectedIds) {
                            selectedIds - transaction.id
                        } else {
                            selectedIds + transaction.id
                        }
                    }
                },
                onLongClick = {
                    isSelectionMode = true
                    selectedIds = setOf(transaction.id)
                }
            )
        }
    }
}

private fun createSampleTransaction(
    method: String,
    path: String,
    code: Int,
    duration: Long
): TransactionSummary = TransactionSummary(
    id = UUID.randomUUID(),
    method = method,
    host = "api.example.com",
    path = path,
    code = code,
    status = when {
        code in 200..299 -> TransactionStatus.COMPLETED
        code in 400..599 -> TransactionStatus.COMPLETED
        else -> TransactionStatus.FAILED
    },
    tookMs = duration,
    hasRequestBody = true,
    hasResponseBody = true,
    timestamp = System.currentTimeMillis()
)

/**
 * Preview for individual components in isolation.
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SelectionCheckboxPreview() {
    MaterialTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SelectionCheckbox(
                isSelected = false,
                accentColor = MaterialTheme.colorScheme.primary
            )
            SelectionCheckbox(
                isSelected = true,
                accentColor = MaterialTheme.colorScheme.primary
            )
            SquareSelectionCheckbox(
                isSelected = false,
                accentColor = MaterialTheme.colorScheme.primary
            )
            SquareSelectionCheckbox(
                isSelected = true,
                accentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun QuickFilterBarPreview() {
    MaterialTheme {
        QuickFilterBar(
            activeFilters = setOf(QuickFilter.ERRORS, QuickFilter.SLOW),
            onFilterToggle = {},
            onClearAll = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun BulkActionBarPreview() {
    MaterialTheme {
        BulkActionBar(
            selectedCount = 5,
            totalCount = 20,
            onSelectAll = {},
            onShare = {},
            onExport = {},
            onDelete = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 280)
@Composable
private fun ContextMenuPreview() {
    MaterialTheme {
        CustomContextMenu(
            visible = true,
            actions = listOf(
                QuickAction("copy", "Copy URL", Icons.Outlined.ContentCopy, onClick = {}),
                QuickAction("share", "Share", Icons.Outlined.Share, onClick = {}),
                QuickAction("delete", "Delete", Icons.Outlined.Delete, destructive = true, onClick = {})
            ),
            onDismiss = {}
        )
    }
}
