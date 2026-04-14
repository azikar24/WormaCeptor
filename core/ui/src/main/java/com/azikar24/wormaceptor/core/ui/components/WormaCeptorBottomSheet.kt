package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Themed modal bottom sheet. Wraps M3 [ModalBottomSheet] with token-driven
 * spacing, shape, and colors so debug inspector flows (filter editors, mock
 * rule dialogs, body detail viewers) render consistently.
 *
 * @param onDismissRequest Called when the user dismisses the sheet by tap/swipe.
 * @param modifier Modifier for the root sheet surface.
 * @param title Optional sheet title rendered as a heading above the content.
 * @param sheetState External sheet state (for programmatic show/hide).
 *                   Defaults to [rememberModalBottomSheetState].
 * @param content Sheet body composed inside a padded [ColumnScope].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WormaCeptorBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = WormaCeptorTokens.Shapes.sheet,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.lg,
                    vertical = WormaCeptorTokens.Spacing.md,
                ),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.md),
                )
            }
            content()
        }
    }
}
