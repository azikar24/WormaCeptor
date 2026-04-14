package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewState
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewState
import kotlinx.collections.immutable.toImmutableMap

/**
 * Hosts all dialogs and bottom sheets owned by [HomeScreen]:
 * filter bottom sheet, clear-transactions dialog, clear-crashes dialog, and delete-selected dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDialogs(
    transactionState: TransactionListViewState,
    crashState: CrashListViewState,
    onTransactionEvent: (TransactionListViewEvent) -> Unit,
    onCrashEvent: (CrashListViewEvent) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    // Filter bottom sheet
    if (transactionState.showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val focusManager = LocalFocusManager.current

        val methodCounts = remember(transactionState.allTransactions) {
            transactionState.allTransactions.groupBy { it.method }
                .mapValues { it.value.size }
                .toImmutableMap()
        }
        val statusCounts = remember(transactionState.allTransactions) {
            mapOf(
                200..299 to transactionState.allTransactions.count { (it.code ?: 0) in 200..299 },
                300..399 to transactionState.allTransactions.count { (it.code ?: 0) in 300..399 },
                400..499 to transactionState.allTransactions.count { (it.code ?: 0) in 400..499 },
                500..599 to transactionState.allTransactions.count { (it.code ?: 0) in 500..599 },
            ).toImmutableMap()
        }

        ModalBottomSheet(
            modifier = Modifier.imePadding(),
            onDismissRequest = {
                focusManager.clearFocus()
                onTransactionEvent(TransactionListViewEvent.FilterSheetVisibilityChanged(false))
            },
            sheetState = sheetState,
            shape = WormaCeptorTokens.Shapes.sheet,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            FilterBottomSheetContent(
                state = transactionState,
                onEvent = onTransactionEvent,
                filteredCount = transactionState.transactions.size,
                totalCount = transactionState.allTransactions.size,
                methodCounts = methodCounts,
                statusCounts = statusCounts,
            )
        }
    }

    // Clear Transactions Confirmation Dialog
    if (transactionState.showClearTransactionsDialog) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.viewer_dialog_clear_transactions_title),
            message = stringResource(R.string.viewer_dialog_clear_transactions_message),
            confirmLabel = stringResource(R.string.viewer_dialog_button_clear),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onTransactionEvent(TransactionListViewEvent.ClearAllTransactions)
            },
            dismissLabel = stringResource(R.string.viewer_dialog_button_cancel),
            onDismiss = {
                onTransactionEvent(
                    TransactionListViewEvent.ClearTransactionsDialogVisibilityChanged(false),
                )
            },
        )
    }

    // Clear Crashes Confirmation Dialog
    if (crashState.showClearCrashesDialog) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.viewer_dialog_clear_crashes_title),
            message = stringResource(R.string.viewer_dialog_clear_crashes_message),
            confirmLabel = stringResource(R.string.viewer_dialog_button_clear),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCrashEvent(CrashListViewEvent.ClearAllCrashes)
            },
            dismissLabel = stringResource(R.string.viewer_dialog_button_cancel),
            onDismiss = {
                onCrashEvent(CrashListViewEvent.ClearCrashesDialogVisibilityChanged(false))
            },
        )
    }

    // Delete Selected Confirmation Dialog
    if (transactionState.showDeleteSelectedDialog) {
        WormaCeptorAlertDialog(
            title = stringResource(
                R.string.viewer_dialog_delete_selected_title,
                transactionState.selectedIds.size,
            ),
            message = stringResource(R.string.viewer_dialog_delete_selected_message),
            confirmLabel = stringResource(R.string.viewer_dialog_button_delete),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onTransactionEvent(TransactionListViewEvent.DeleteSelectedClicked)
            },
            dismissLabel = stringResource(R.string.viewer_dialog_button_cancel),
            onDismiss = {
                onTransactionEvent(
                    TransactionListViewEvent.DeleteSelectedDialogVisibilityChanged(false),
                )
            },
            destructive = true,
        )
    }
}
