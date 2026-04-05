package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.feature.securestorage.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TypeFilterChips(
    selectedType: StorageType?,
    onTypeSelected: (StorageType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorFlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.securestorage_filter_all)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = WormaCeptorTokens.Colors.SecureStorage.primary.copy(
                    alpha = WormaCeptorTokens.Alpha.LIGHT,
                ),
            ),
        )
        StorageType.entries.forEach { type ->
            val (icon, color) = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> Pair(
                    Icons.Default.EnhancedEncryption,
                    WormaCeptorTokens.Colors.SecureStorage.encryptedPrefs,
                )
                StorageType.KEYSTORE -> Pair(Icons.Default.Key, WormaCeptorTokens.Colors.SecureStorage.keystore)
                StorageType.DATASTORE -> Pair(
                    Icons.Default.DataObject,
                    WormaCeptorTokens.Colors.SecureStorage.datastore,
                )
            }
            val label = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(R.string.securestorage_filter_prefs)
                StorageType.KEYSTORE -> stringResource(R.string.securestorage_filter_keystore)
                StorageType.DATASTORE -> stringResource(R.string.securestorage_filter_datastore)
            }
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                    selectedLabelColor = color,
                    selectedLeadingIconColor = color,
                ),
            )
        }
    }
}
