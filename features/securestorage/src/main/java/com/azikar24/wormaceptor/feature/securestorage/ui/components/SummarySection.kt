package com.azikar24.wormaceptor.feature.securestorage.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
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
    val contentAlpha = if (isAccessible) {
        WormaCeptorTokens.Alpha.PROMINENT
    } else {
        WormaCeptorTokens.Alpha.MODERATE
    }
    val statusDescription = if (isAccessible) accessibleText else notAccessibleText
    WormaCeptorCard(
        modifier = modifier.semantics { contentDescription = statusDescription },
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = color.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color.copy(alpha = contentAlpha),
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
            )
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = contentAlpha),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
