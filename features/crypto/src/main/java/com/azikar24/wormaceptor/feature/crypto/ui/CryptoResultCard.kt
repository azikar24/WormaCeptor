package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoOperation
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.feature.crypto.R

@Composable
internal fun ResultCard(
    result: CryptoResult,
    onCopy: (String) -> Unit,
    onClear: () -> Unit,
    onUseAsInput: (String) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val isSuccess = result.success
    val accentColor = when {
        !isSuccess -> WormaCeptorTokens.semantic().error
        result.operation == CryptoOperation.ENCRYPT -> WormaCeptorTokens.Colors.Crypto.encrypt
        else -> WormaCeptorTokens.Colors.Crypto.decrypt
    }
    val successText = stringResource(R.string.crypto_success)
    val failedText = stringResource(R.string.crypto_failed)
    val unknownErrorText = stringResource(R.string.crypto_unknown_error)

    WormaCeptorContainer(
        backgroundColor = accentColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
        borderColor = accentColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                ) {
                    Icon(
                        if (isSuccess) Icons.Default.Check else Icons.Default.Error,
                        null,
                        tint = accentColor,
                    )
                    Text(
                        "${result.operation.displayName} ${if (isSuccess) successText else failedText}",
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                    )
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClear()
                    },
                ) {
                    Icon(
                        Icons.Default.Delete,
                        stringResource(R.string.crypto_clear_result),
                        tint = WormaCeptorTokens.semantic().textSecondary,
                    )
                }
            }

            val outputText = result.output
            if (isSuccess && outputText != null) {
                Text(
                    stringResource(R.string.crypto_output_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = WormaCeptorTokens.semantic().textSecondary,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(WormaCeptorTokens.Shapes.button)
                        .background(WormaCeptorTokens.semantic().surface)
                        .padding(WormaCeptorTokens.Spacing.md),
                ) {
                    Text(
                        outputText,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                ) {
                    WormaCeptorButton(
                        text = stringResource(R.string.crypto_copy),
                        onClick = { onCopy(outputText) },
                        variant = ButtonVariant.Outlined,
                        leadingIcon = {
                            Icon(
                                Icons.Default.ContentCopy,
                                null,
                                Modifier.size(WormaCeptorTokens.IconSize.md),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                    WormaCeptorButton(
                        text = stringResource(R.string.crypto_use_as_input),
                        onClick = { onUseAsInput(outputText) },
                        variant = ButtonVariant.Outlined,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Refresh,
                                null,
                                Modifier.size(WormaCeptorTokens.IconSize.md),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else if (!isSuccess) {
                Text(
                    result.errorMessage ?: unknownErrorText,
                    color = accentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                "${result.algorithm.displayName}/${result.mode.displayName} | ${result.durationMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = WormaCeptorTokens.semantic().textSecondary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultCardSuccessPreview() {
    WormaCeptorTheme {
        ResultCard(
            result = CryptoResult.encryptSuccess(
                id = "1",
                input = "Hello, World!",
                output = "dGhpcyBpcyBlbmNyeXB0ZWQ=",
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.GCM,
                durationMs = 12,
            ),
            onCopy = {},
            onClear = {},
            onUseAsInput = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultCardFailurePreview() {
    WormaCeptorTheme {
        ResultCard(
            result = CryptoResult.failure(
                id = "2",
                operation = CryptoOperation.DECRYPT,
                input = "invalid-ciphertext",
                algorithm = CryptoAlgorithm.AES_256,
                mode = CipherMode.CBC,
                errorMessage = "Invalid key length: expected 32 bytes",
                durationMs = 3,
            ),
            onCopy = {},
            onClear = {},
            onUseAsInput = {},
        )
    }
}
