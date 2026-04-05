package com.azikar24.wormaceptor.feature.viewer.ui

import java.util.UUID

/**
 * Grouped state and callbacks for multi-select behavior in the transaction list.
 *
 * Bundles selection-related parameters that always travel together,
 * reducing the parameter count of [SelectableTransactionListScreen].
 */
data class TransactionSelectionState(
    val selectedIds: Set<UUID> = emptySet(),
    val isSelectionMode: Boolean = false,
    val onSelectionToggle: (UUID) -> Unit = {},
    val onLongClick: (UUID) -> Unit = {},
)
