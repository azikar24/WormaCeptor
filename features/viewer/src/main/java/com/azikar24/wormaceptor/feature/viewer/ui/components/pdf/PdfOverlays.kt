package com.azikar24.wormaceptor.feature.viewer.ui.components.pdf

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun PdfLoadingOverlay() {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
        ) {
            CircularProgressIndicator(
                color = darkColors.textPrimary,
                modifier = Modifier.size(WormaCeptorTokens.TouchTarget.comfortable),
                strokeWidth = WormaCeptorTokens.BorderWidth.bold,
            )
            Text(
                text = stringResource(R.string.viewer_pdf_loading),
                style = MaterialTheme.typography.bodyLarge,
                color = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
            )
        }
    }
}

@Composable
internal fun PdfErrorOverlay(
    message: String,
    onDismiss: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.xl),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.viewer_pdf_error),
                modifier = Modifier.size(WormaCeptorTokens.TouchTarget.large),
                tint = MaterialTheme.colorScheme.error,
            )

            Text(
                text = stringResource(R.string.viewer_pdf_failed),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = darkColors.textPrimary,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            WormaCeptorButton(
                text = stringResource(R.string.viewer_pdf_close),
                onClick = onDismiss,
                variant = ButtonVariant.Primary,
                containerColor = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                contentColor = darkColors.textPrimary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfLoadingOverlayPreview() {
    WormaCeptorTheme {
        Box(modifier = Modifier.fillMaxSize().background(WormaCeptorTokens.semantic(darkTheme = true).background)) {
            PdfLoadingOverlay()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfErrorOverlayPreview() {
    WormaCeptorTheme {
        Box(modifier = Modifier.fillMaxSize().background(WormaCeptorTokens.semantic(darkTheme = true).background)) {
            PdfErrorOverlay(
                message = "The file appears to be corrupted or is not a valid PDF document.",
                onDismiss = {},
            )
        }
    }
}
