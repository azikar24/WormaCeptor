package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.StorageDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R

@Composable
internal fun StorageSection(
    storage: StorageDetails,
    onCopy: () -> Unit,
) {
    WormaCeptorInfoCard(
        title = stringResource(R.string.deviceinfo_section_storage),
        icon = Icons.Default.Storage,
        iconTint = WormaCeptorTokens.Colors.Category.simulation,
        onAction = onCopy,
        actionContentDescription = stringResource(
            R.string.deviceinfo_copy_section,
            stringResource(R.string.deviceinfo_section_storage),
        ),
    ) {
        Text(
            text = stringResource(R.string.deviceinfo_storage_internal),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_label_total), formatBytes(storage.internalTotal))
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_label_available),
            formatBytes(storage.internalAvailable),
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_label_used), formatBytes(storage.internalUsed))

        val internalUsagePercent = if (storage.internalTotal > 0) {
            storage.internalUsed.toFloat() / storage.internalTotal.toFloat() * 100f
        } else {
            0f
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
        LinearProgressIndicator(
            progress = { internalUsagePercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(WormaCeptorTokens.ComponentSize.progressBarHeight),
            color = usageColor(internalUsagePercent),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        if (storage.hasExternalStorage && storage.externalTotal != null) {
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))
            WormaCeptorDivider(style = DividerStyle.Subtle)
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            val extTotal = storage.externalTotal ?: 0L
            val extUsed = storage.externalUsed ?: 0L

            Text(
                text = stringResource(R.string.deviceinfo_storage_external),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
            WormaCeptorDetailRow(stringResource(R.string.deviceinfo_label_total), formatBytes(extTotal))
            storage.externalAvailable?.let {
                WormaCeptorDetailRow(stringResource(R.string.deviceinfo_label_available), formatBytes(it))
            }
            storage.externalUsed?.let {
                WormaCeptorDetailRow(
                    stringResource(R.string.deviceinfo_label_used),
                    formatBytes(it),
                )
            }

            val externalUsagePercent = if (extTotal > 0) {
                extUsed.toFloat() / extTotal.toFloat() * 100f
            } else {
                0f
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

            LinearProgressIndicator(
                progress = { externalUsagePercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WormaCeptorTokens.ComponentSize.progressBarHeight),
                color = usageColor(externalUsagePercent),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}
