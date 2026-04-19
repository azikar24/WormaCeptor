package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailHeader
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailSection
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
        val storageTitle = when (entry.storageType) {
            StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(R.string.securestorage_detail_encrypted_prefs)
            StorageType.KEYSTORE -> stringResource(R.string.securestorage_detail_android_keystore)
            StorageType.DATASTORE -> stringResource(R.string.securestorage_detail_datastore)
        }

        WormaCeptorDetailHeader(
            icon = when (entry.storageType) {
                StorageType.ENCRYPTED_SHARED_PREFS -> Icons.Default.EnhancedEncryption
                StorageType.KEYSTORE -> Icons.Default.Key
                StorageType.DATASTORE -> Icons.Default.DataObject
            },
            iconTint = typeColor,
            iconBackgroundColor = typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
            title = storageTitle,
            iconContentDescription = storageTitle,
            subtitle = {
                val encryptionColor = if (entry.isEncrypted) {
                    WormaCeptorTokens.Colors.SecureStorage.encrypted
                } else {
                    WormaCeptorTokens.Colors.SecureStorage.unencrypted
                }
                val encryptionLabel = if (entry.isEncrypted) {
                    stringResource(R.string.securestorage_detail_encrypted)
                } else {
                    stringResource(R.string.securestorage_detail_not_encrypted)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = encryptionLabel,
                        tint = encryptionColor,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                    )
                    Text(
                        text = encryptionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = encryptionColor,
                    )
                }
            },
        )

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
