package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 * Hosts all dialogs and bottom sheets owned by [HomeScreen]:
 * filter bottom sheet, clear-transactions dialog, clear-crashes dialog, and delete-selected dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDialogs(
    state: ViewerViewState,
    onEvent: (ViewerViewEvent) -> Unit,
    transactions: ImmutableList<TransactionSummary>,
    allTransactions: ImmutableList<TransactionSummary>,
) {
    // Filter bottom sheet
    if (state.showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val focusManager = LocalFocusManager.current

        val methodCounts = remember(allTransactions) {
            allTransactions.groupBy { it.method }.mapValues { it.value.size }.toImmutableMap()
        }
        val statusCounts = remember(allTransactions) {
            mapOf(
                200..299 to allTransactions.count { (it.code ?: 0) in 200..299 },
                300..399 to allTransactions.count { (it.code ?: 0) in 300..399 },
                400..499 to allTransactions.count { (it.code ?: 0) in 400..499 },
                500..599 to allTransactions.count { (it.code ?: 0) in 500..599 },
            ).toImmutableMap()
        }

        ModalBottomSheet(
            modifier = Modifier.imePadding(),
            onDismissRequest = {
                focusManager.clearFocus()
                onEvent(ViewerViewEvent.FilterSheetVisibilityChanged(false))
            },
            sheetState = sheetState,
            shape = WormaCeptorDesignSystem.Shapes.sheet,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            FilterBottomSheetContent(
                initialSearchQuery = state.searchQuery,
                initialFilterMethods = state.filterMethods,
                initialFilterStatusRanges = state.filterStatusRanges,
                onApply = { query, methods, statusRanges ->
                    onEvent(ViewerViewEvent.SearchQueryChanged(query))
                    onEvent(ViewerViewEvent.MethodFiltersChanged(methods))
                    onEvent(ViewerViewEvent.StatusFiltersChanged(statusRanges))
                    focusManager.clearFocus()
                    onEvent(ViewerViewEvent.FilterSheetVisibilityChanged(false))
                },
                filteredCount = transactions.size,
                totalCount = allTransactions.size,
                methodCounts = methodCounts,
                statusCounts = statusCounts,
            )
        }
    }

    // Clear Transactions Confirmation Dialog
    if (state.showClearTransactionsDialog) {
        AlertDialog(
            onDismissRequest = {
                onEvent(ViewerViewEvent.ClearTransactionsDialogVisibilityChanged(false))
            },
            title = { Text(stringResource(R.string.viewer_dialog_clear_transactions_title)) },
            text = { Text(stringResource(R.string.viewer_dialog_clear_transactions_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ViewerViewEvent.ClearAllTransactions) }) {
                    Text(stringResource(R.string.viewer_dialog_button_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(ViewerViewEvent.ClearTransactionsDialogVisibilityChanged(false))
                    },
                ) {
                    Text(stringResource(R.string.viewer_dialog_button_cancel))
                }
            },
        )
    }

    // Clear Crashes Confirmation Dialog
    if (state.showClearCrashesDialog) {
        AlertDialog(
            onDismissRequest = {
                onEvent(ViewerViewEvent.ClearCrashesDialogVisibilityChanged(false))
            },
            title = { Text(stringResource(R.string.viewer_dialog_clear_crashes_title)) },
            text = { Text(stringResource(R.string.viewer_dialog_clear_crashes_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ViewerViewEvent.ClearAllCrashes) }) {
                    Text(stringResource(R.string.viewer_dialog_button_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(ViewerViewEvent.ClearCrashesDialogVisibilityChanged(false))
                    },
                ) {
                    Text(stringResource(R.string.viewer_dialog_button_cancel))
                }
            },
        )
    }

    // Delete Selected Confirmation Dialog
    if (state.showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = {
                onEvent(ViewerViewEvent.DeleteSelectedDialogVisibilityChanged(false))
            },
            title = {
                Text(
                    stringResource(
                        R.string.viewer_dialog_delete_selected_title,
                        state.selectedIds.size,
                    ),
                )
            },
            text = { Text(stringResource(R.string.viewer_dialog_delete_selected_message)) },
            confirmButton = {
                TextButton(onClick = { onEvent(ViewerViewEvent.DeleteSelectedClicked) }) {
                    Text(
                        stringResource(R.string.viewer_dialog_button_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(ViewerViewEvent.DeleteSelectedDialogVisibilityChanged(false))
                    },
                ) {
                    Text(stringResource(R.string.viewer_dialog_button_cancel))
                }
            },
        )
    }
}
