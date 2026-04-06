package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.crypto.R

@Composable
internal fun ErrorCard(
    message: String,
    onDismiss: () -> Unit,
) {
    val errorColor = WormaCeptorTokens.semantic().error
    WormaCeptorContainer(
        backgroundColor = errorColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
        borderColor = errorColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Error, null, tint = errorColor)
                Text(
                    message,
                    color = errorColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Delete,
                    stringResource(R.string.crypto_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorCardPreview() {
    WormaCeptorTheme {
        ErrorCard(
            message = "Encryption key must be 32 bytes for AES-256",
            onDismiss = {},
        )
    }
}
