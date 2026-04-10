package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailSection
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.feature.securestorage.R

@Composable
internal fun EntryDetailContent(
    entry: SecureStorageEntry,
    modifier: Modifier = Modifier,
) {
    val typeColor = when (entry.storageType) {
        StorageType.ENCRYPTED_SHARED_PREFS -> WormaCeptorTokens.Colors.SecureStorage.encryptedPrefs
        StorageType.KEYSTORE -> WormaCeptorTokens.Colors.SecureStorage.keystore
        StorageType.DATASTORE -> WormaCeptorTokens.Colors.SecureStorage.datastore
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(WormaCeptorTokens.Spacing.xxxl)
                    .clip(RoundedCornerShape(WormaCeptorTokens.Radius.lg))
                    .background(typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> Icons.Default.EnhancedEncryption
                        StorageType.KEYSTORE -> Icons.Default.Key
                        StorageType.DATASTORE -> Icons.Default.DataObject
                    },
                    contentDescription = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(
                            R.string.securestorage_detail_encrypted_prefs,
                        )
                        StorageType.KEYSTORE -> stringResource(R.string.securestorage_detail_android_keystore)
                        StorageType.DATASTORE -> stringResource(R.string.securestorage_detail_datastore)
                    },
                    tint = typeColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                )
            }
            Column {
                Text(
                    text = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(
                            R.string.securestorage_detail_encrypted_prefs,
                        )
                        StorageType.KEYSTORE -> stringResource(R.string.securestorage_detail_android_keystore)
                        StorageType.DATASTORE -> stringResource(R.string.securestorage_detail_datastore)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (entry.isEncrypted) {
                            stringResource(R.string.securestorage_detail_encrypted)
                        } else {
                            stringResource(R.string.securestorage_detail_not_encrypted)
                        },
                        tint = if (entry.isEncrypted) WormaCeptorTokens.Colors.SecureStorage.encrypted else WormaCeptorTokens.Colors.SecureStorage.unencrypted,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                    )
                    Text(
                        text = if (entry.isEncrypted) {
                            stringResource(R.string.securestorage_detail_encrypted)
                        } else {
                            stringResource(R.string.securestorage_detail_not_encrypted)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isEncrypted) WormaCeptorTokens.Colors.SecureStorage.encrypted else WormaCeptorTokens.Colors.SecureStorage.unencrypted,
                    )
                }
            }
        }

        WormaCeptorDetailSection(
            title = stringResource(R.string.securestorage_detail_label_key),
            value = entry.key,
        )

        WormaCeptorDetailSection(
            title = stringResource(R.string.securestorage_detail_label_value),
            value = entry.value,
        )

        entry.lastModified?.let { timestamp ->
            WormaCeptorDetailSection(
                title = stringResource(R.string.securestorage_detail_label_last_modified),
                value = formatTimestampFull(timestamp),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
    }
}
