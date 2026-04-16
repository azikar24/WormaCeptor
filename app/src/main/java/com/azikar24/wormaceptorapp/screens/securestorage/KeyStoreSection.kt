package com.azikar24.wormaceptorapp.screens.securestorage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptorapp.R

@Composable
internal fun KeyStoreSection(
    entries: List<KeyStoreEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        SecureStorageEmptyState(
            icon = Icons.Default.Key,
            title = stringResource(R.string.keystore_empty_title),
            subtitle = stringResource(R.string.keystore_empty_subtitle),
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(WormaCeptorTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            items(entries, key = { it.alias }) { entry ->
                KeyStoreItem(
                    entry = entry,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun KeyStoreItem(
    entry: KeyStoreEntry,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            KeyStoreIcon()
            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
            KeyStoreDetails(
                entry = entry,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun KeyStoreIcon() {
    Surface(
        shape = WormaCeptorTokens.Shapes.button,
        color = WormaCeptorTokens.Colors.SecureStorage.encrypted.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
        modifier = Modifier.size(WormaCeptorTokens.IconSize.xxl),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = Icons.Default.VpnKey,
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                tint = WormaCeptorTokens.Colors.SecureStorage.encrypted,
            )
        }
    }
}

@Composable
private fun KeyStoreDetails(
    entry: KeyStoreEntry,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = entry.alias,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))
        Row {
            KeyStoreChip(text = entry.algorithm)
            entry.keySize?.let { size ->
                KeyStoreChip(text = stringResource(R.string.keystore_key_size_format, size))
            }
        }
    }
}

@Composable
private fun KeyStoreChip(text: String) {
    Surface(
        shape = WormaCeptorTokens.Shapes.chip,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xxs,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyStoreSectionEmptyPreview() {
    WormaCeptorTheme {
        KeyStoreSection(entries = emptyList())
    }
}

@Preview(showBackground = true)
@Composable
private fun KeyStoreSectionPreview() {
    WormaCeptorTheme {
        KeyStoreSection(
            entries = listOf(
                KeyStoreEntry(
                    alias = "wormaceptor_aes_key",
                    algorithm = "AES",
                    keySize = 256,
                    creationDate = "2026-04-05",
                ),
                KeyStoreEntry(
                    alias = "wormaceptor_rsa_key",
                    algorithm = "RSA",
                    keySize = 2048,
                    creationDate = "2026-04-05",
                ),
                KeyStoreEntry(
                    alias = "wormaceptor_ec_key",
                    algorithm = "EC",
                    keySize = null,
                    creationDate = null,
                ),
            ),
        )
    }
}
