package com.azikar24.wormaceptor.feature.viewer.ui

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.viewer.export.ExportManager
import com.azikar24.wormaceptor.feature.viewer.ui.components.saveImageToGallery
import com.azikar24.wormaceptor.feature.viewer.ui.components.shareImage
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareAsFile
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Handles one-time side effects emitted by [com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewModel].
 */
internal fun handleTransactionDetailEffect(
    effect: TransactionDetailViewEffect,
    context: Context,
    scope: CoroutineScope,
    snackBarHostState: SnackbarHostState,
) {
    when (effect) {
        // -- Clipboard --
        is TransactionDetailViewEffect.Clipboard.CopyText -> {
            val message = copyToClipboard(context, effect.label, effect.content)
            scope.launch { snackBarHostState.showSnackbar(message) }
        }

        // -- Share --
        is TransactionDetailViewEffect.Share.AsFile -> {
            scope.launch { shareAsFile(context, effect.content, effect.fileName, effect.mimeType, effect.title) }
        }

        is TransactionDetailViewEffect.Share.Image -> {
            val message = shareImage(context, effect.payload.bytes, effect.payload.format)
            if (message != null) {
                scope.launch { snackBarHostState.showSnackbar(message) }
            }
        }

        // -- Save --
        is TransactionDetailViewEffect.Save.ExportTransactions -> {
            val exportManager = ExportManager(context, CoreHolder.queryEngine) { msg ->
                scope.launch { snackBarHostState.showSnackbar(msg) }
            }
            scope.launch { exportManager.exportTransactions(effect.transactions, effect.format) }
        }

        is TransactionDetailViewEffect.Save.ImageToGallery -> {
            val message = saveImageToGallery(context, effect.payload.bytes, effect.payload.format)
            scope.launch { snackBarHostState.showSnackbar(message) }
        }

        is TransactionDetailViewEffect.Save.PdfToDownloads -> {
            val message = savePdfToDownloads(context, effect.bytes)
            scope.launch { snackBarHostState.showSnackbar(message) }
        }

        // -- Message --
        is TransactionDetailViewEffect.ShowSnackBar -> {
            scope.launch { snackBarHostState.showSnackbar(effect.message) }
        }
    }
}
