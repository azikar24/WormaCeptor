package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDateShort
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import com.azikar24.wormaceptor.feature.securestorage.R

@Composable
internal fun SummarySection(
    summary: SecureStorageSummary,
    keystoreAccessible: Boolean,
    encryptedPrefsAccessible: Boolean,
    lastRefreshTime: Long?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        val accessibleText = stringResource(R.string.securestorage_status_accessible)
        val notAccessibleText = stringResource(R.string.securestorage_status_not_accessible)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            SummaryCard(
                count = summary.encryptedPrefsCount,
                label = stringResource(R.string.securestorage_summary_prefs),
                icon = Icons.Default.EnhancedEncryption,
                color = WormaCeptorTokens.Colors.SecureStorage.encryptedPrefs,
                isAccessible = encryptedPrefsAccessible,
                accessibleText = accessibleText,
                notAccessibleText = notAccessibleText,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                count = summary.keystoreAliasCount,
                label = stringResource(R.string.securestorage_summary_keystore),
                icon = Icons.Default.Key,
                color = WormaCeptorTokens.Colors.SecureStorage.keystore,
                isAccessible = keystoreAccessible,
                accessibleText = accessibleText,
                notAccessibleText = notAccessibleText,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                count = summary.dataStoreFileCount,
                label = stringResource(R.string.securestorage_summary_datastore),
                icon = Icons.Default.DataObject,
                color = WormaCeptorTokens.Colors.SecureStorage.datastore,
                isAccessible = true, // DataStore is always accessible if files exist
                accessibleText = accessibleText,
                notAccessibleText = notAccessibleText,
                modifier = Modifier.weight(1f),
            )
        }

        // Last refresh time
        lastRefreshTime?.let { time ->
            Text(
                text = stringResource(R.string.securestorage_last_scanned, formatDateShort(time)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = WormaCeptorTokens.Spacing.xs),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    count: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    isAccessible: Boolean,
    accessibleText: String,
    notAccessibleText: String,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon with status indicator
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
                // Small status dot in corner
                Icon(
                    imageVector = if (isAccessible) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = if (isAccessible) accessibleText else notAccessibleText,
                    tint = if (isAccessible) WormaCeptorTokens.Colors.SecureStorage.encrypted else WormaCeptorTokens.Colors.SecureStorage.unencrypted,
                    modifier = Modifier
                        .size(WormaCeptorTokens.IconSize.xxs)
                        .align(Alignment.TopEnd),
                )
            }
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
