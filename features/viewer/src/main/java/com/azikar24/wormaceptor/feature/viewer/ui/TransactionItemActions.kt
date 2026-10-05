package com.azikar24.wormaceptor.feature.viewer.ui

import com.azikar24.wormaceptor.domain.entities.TransactionSummary

/**
 * Grouped callbacks for context-menu actions on a single transaction item.
 *
 * Reduces the parameter count of [SelectableTransactionListScreen] by bundling
 * per-item action callbacks into a single object.
 */
data class TransactionItemActions(
    val onCopyUrl: (TransactionSummary) -> Unit = {},
    val onShare: (TransactionSummary) -> Unit = {},
    val onShareAsHar: (TransactionSummary) -> Unit = {},
    val onDelete: (TransactionSummary) -> Unit = {},
    val onCopyAsCurl: (TransactionSummary) -> Unit = {},
    /** Opens a new mock rule pre-filled from the transaction. */
    val onAddToMock: (TransactionSummary) -> Unit = {},
)
