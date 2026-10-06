package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Key
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.feature.securestorage.R

@Composable
internal fun TypeFilterChips(
    selectedType: StorageType?,
    onTypeSelected: (StorageType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorScrollableRow(modifier = modifier) {
        WormaCeptorChip(
            label = stringResource(R.string.securestorage_filter_all),
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
        )
        StorageType.entries.forEach { type ->
            val icon = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> Icons.Default.EnhancedEncryption
                StorageType.KEYSTORE -> Icons.Default.Key
                StorageType.DATASTORE -> Icons.Default.DataObject
            }
            val label = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(R.string.securestorage_filter_prefs)
                StorageType.KEYSTORE -> stringResource(R.string.securestorage_filter_keystore)
                StorageType.DATASTORE -> stringResource(R.string.securestorage_filter_datastore)
            }
            WormaCeptorChip(
                label = label,
                selected = selectedType == type,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                leadingIcon = icon,
            )
        }
    }
}
