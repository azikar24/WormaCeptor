package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
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
    val isSuccess = result.success
    val accentColor = when {
        !isSuccess -> WormaCeptorDesignSystem.ThemeColors.Error
        result.operation == CryptoOperation.ENCRYPT -> WormaCeptorColors.SecureStorage.EncryptedPrefs
        else -> WormaCeptorColors.SecureStorage.Datastore
    }
    val successText = stringResource(R.string.crypto_success)
    val failedText = stringResource(R.string.crypto_failed)
    val unknownErrorText = stringResource(R.string.crypto_unknown_error)

    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        backgroundColor = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.LIGHT),
        borderColor = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
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
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Delete,
                        stringResource(R.string.crypto_clear_result),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val outputText = result.output
            if (isSuccess && outputText != null) {
                Text(
                    stringResource(R.string.crypto_output_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(WormaCeptorDesignSystem.Spacing.md),
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
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    OutlinedButton(
                        onClick = { onCopy(outputText) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                        Text(stringResource(R.string.crypto_copy))
                    }
                    OutlinedButton(
                        onClick = { onUseAsInput(outputText) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                        Text(stringResource(R.string.crypto_use_as_input))
                    }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.HEAVY),
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
