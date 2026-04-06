package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoOperation
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.feature.crypto.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val InputPreviewLength = 50

@Composable
internal fun HistoryItem(
    result: CryptoResult,
    onLoad: () -> Unit,
    onRemove: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val accentColor = when {
        !result.success -> WormaCeptorTokens.semantic().error
        result.operation == CryptoOperation.ENCRYPT -> WormaCeptorTokens.Colors.Crypto.encrypt
        else -> WormaCeptorTokens.Colors.Crypto.decrypt
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() }
            .padding(vertical = WormaCeptorTokens.Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HistoryItemDetails(result = result, accentColor = accentColor, modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRemove()
            },
        ) {
            Icon(
                Icons.Default.Delete,
                stringResource(R.string.crypto_remove),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HistoryItemDetails(
    result: CryptoResult,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val successText = stringResource(R.string.crypto_success)
    val failedText = stringResource(R.string.crypto_failed)

    Column(modifier = modifier) {
        HistoryItemHeader(result = result, accentColor = accentColor)

        val inputPreview = result.input.take(InputPreviewLength) +
            if (result.input.length > InputPreviewLength) "..." else ""
        Text(
            text = inputPreview,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            "${dateFormat.format(Date(result.timestamp))} | " +
                "${if (result.success) successText else failedText} | ${result.durationMs}ms",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
        )
    }
}

@Composable
private fun HistoryItemHeader(
    result: CryptoResult,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        val icon = if (result.operation == CryptoOperation.ENCRYPT) {
            Icons.Default.Lock
        } else {
            Icons.Default.LockOpen
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
        )
        val label = "${result.operation.displayName} - " +
            "${result.algorithm.displayName}/${result.mode.displayName}"
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryItemSuccessPreview() {
    WormaCeptorTheme {
        HistoryItem(
            result = CryptoResult.encryptSuccess(
                id = "1",
                input = "Hello, World! This is a sample encryption input",
                output = "dGhpcyBpcyBlbmNyeXB0ZWQ=",
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.GCM,
                durationMs = 12,
            ),
            onLoad = {},
            onRemove = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryItemFailurePreview() {
    WormaCeptorTheme {
        HistoryItem(
            result = CryptoResult.failure(
                id = "2",
                operation = CryptoOperation.DECRYPT,
                input = "invalid-ciphertext-data",
                algorithm = CryptoAlgorithm.AES_128,
                mode = CipherMode.CBC,
                errorMessage = "Bad padding",
                durationMs = 5,
            ),
            onLoad = {},
            onRemove = {},
        )
    }
}
