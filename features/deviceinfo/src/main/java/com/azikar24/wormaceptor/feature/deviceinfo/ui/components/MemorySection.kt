package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.MemoryDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R
import java.util.Locale

@Composable
internal fun MemorySection(
    memory: MemoryDetails,
    onCopy: () -> Unit,
) {
    WormaCeptorInfoCard(
        title = stringResource(R.string.deviceinfo_section_memory),
        icon = Icons.Default.Memory,
        iconTint = WormaCeptorTokens.Colors.Status.amber,
        onAction = onCopy,
        actionContentDescription = stringResource(
            R.string.deviceinfo_copy_section,
            stringResource(R.string.deviceinfo_section_memory),
        ),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_total_ram), formatBytes(memory.totalRam))
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_available_ram), formatBytes(memory.availableRam))
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_used_ram), formatBytes(memory.usedRam))

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.deviceinfo_label_usage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", memory.usagePercentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = usageColor(memory.usagePercentage),
                )
            }
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
            LinearProgressIndicator(
                progress = { memory.usagePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WormaCeptorTokens.ComponentSize.progressBarHeight),
                color = usageColor(memory.usagePercentage),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_memory_low_threshold),
            formatBytes(memory.lowMemoryThreshold),
        )

        val lowMemoryLabel = if (memory.isLowMemory) {
            stringResource(R.string.deviceinfo_yes)
        } else {
            stringResource(R.string.deviceinfo_no)
        }

        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_low_memory), lowMemoryLabel)
    }
}
