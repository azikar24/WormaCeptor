package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDateShort
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.feature.securestorage.R

@Composable
internal fun EntryCard(
    entry: SecureStorageEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeColor = when (entry.storageType) {
        StorageType.ENCRYPTED_SHARED_PREFS -> WormaCeptorTokens.Colors.SecureStorage.encryptedPrefs
        StorageType.KEYSTORE -> WormaCeptorTokens.Colors.SecureStorage.keystore
        StorageType.DATASTORE -> WormaCeptorTokens.Colors.SecureStorage.datastore
    }

    WormaCeptorCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type indicator
            val encryptionState = if (entry.isEncrypted) {
                stringResource(R.string.securestorage_detail_encrypted)
            } else {
                stringResource(R.string.securestorage_detail_not_encrypted)
            }
            val encryptionColor = if (entry.isEncrypted) {
                WormaCeptorTokens.Colors.SecureStorage.encrypted
            } else {
                WormaCeptorTokens.Colors.SecureStorage.unencrypted
            }

            Box(
                modifier = Modifier
                    .size(WormaCeptorTokens.TouchTarget.minimum)
                    .clip(WormaCeptorTokens.Shapes.card)
                    .background(encryptionColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT))
                    .semantics { stateDescription = encryptionState },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = encryptionState,
                    tint = encryptionColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = entry.key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.value.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.INTENSE),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                entry.lastModified?.let { timestamp ->
                    Text(
                        text = formatDateShort(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

            // Type badge
            Surface(
                shape = WormaCeptorTokens.Shapes.chip,
                color = typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
            ) {
                Text(
                    text = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(R.string.securestorage_badge_prefs)
                        StorageType.KEYSTORE -> stringResource(R.string.securestorage_badge_keystore)
                        StorageType.DATASTORE -> stringResource(R.string.securestorage_badge_datastore)
                    },
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.xs,
                        vertical = WormaCeptorTokens.Spacing.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = typeColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EntryCardEncryptedPrefsPreview() {
    WormaCeptorTheme {
        EntryCard(
            entry = SecureStorageEntry(
                key = "user_token",
                value = "eyJhbGciOiJIUzI1NiJ9.test",
                storageType = StorageType.ENCRYPTED_SHARED_PREFS,
                isEncrypted = true,
                lastModified = 1_712_000_000_000L,
            ),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EntryCardKeystorePreview() {
    WormaCeptorTheme {
        EntryCard(
            entry = SecureStorageEntry(
                key = "test_signing_key",
                value = "AES-256",
                storageType = StorageType.KEYSTORE,
                isEncrypted = true,
                lastModified = null,
            ),
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EntryCardDataStoreUnencryptedPreview() {
    WormaCeptorTheme {
        EntryCard(
            entry = SecureStorageEntry(
                key = "feature_flags",
                value = "{\"dark_mode\":true}",
                storageType = StorageType.DATASTORE,
                isEncrypted = false,
                lastModified = 1_712_000_000_000L,
            ),
            onClick = {},
        )
    }
}
