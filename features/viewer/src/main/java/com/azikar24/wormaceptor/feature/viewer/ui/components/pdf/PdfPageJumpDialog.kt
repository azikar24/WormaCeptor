package com.azikar24.wormaceptor.feature.viewer.ui.components.pdf

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun PdfPageJumpDialog(
    currentPage: Int,
    totalPages: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit,
) {
    var pageInput by remember { mutableStateOf(currentPage.toString()) }
    var isError by remember { mutableStateOf(false) }

    val submitPage = {
        val page = pageInput.toIntOrNull()
        if (page != null && page in 1..totalPages) {
            onPageSelected(page)
        } else {
            isError = true
        }
    }

    WormaCeptorAlertDialog(
        title = stringResource(R.string.viewer_pdf_go_to_page),
        confirmLabel = stringResource(R.string.viewer_pdf_go),
        onConfirm = { submitPage() },
        dismissLabel = stringResource(R.string.viewer_pdf_cancel),
        onDismiss = onDismiss,
    ) {
        Text(
            stringResource(R.string.viewer_pdf_page_prompt, totalPages),
            style = MaterialTheme.typography.bodyMedium,
            color = WormaCeptorTokens.semantic().textSecondary,
        )

        WormaCeptorTextField(
            value = pageInput,
            onValueChange = { value ->
                pageInput = value.filter { it.isDigit() }
                isError = false
            },
            label = { Text(stringResource(R.string.viewer_pdf_page_number)) },
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { submitPage() }),
            modifier = Modifier.fillMaxWidth(),
        )

        if (isError) {
            Text(
                text = stringResource(R.string.viewer_pdf_invalid_page),
                style = MaterialTheme.typography.bodySmall,
                color = WormaCeptorTokens.semantic().error,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PdfPageJumpDialogPreview() {
    WormaCeptorTheme {
        PdfPageJumpDialog(
            currentPage = 5,
            totalPages = 20,
            onDismiss = {},
            onPageSelected = {},
        )
    }
}
