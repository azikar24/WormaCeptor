package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.R

@Composable
internal fun NativeHeapCard(
    currentMemory: MemoryInfo,
    modifier: Modifier = Modifier,
) {
    val mem = WormaCeptorTokens.Colors.Memory
    val usagePercent = if (currentMemory.nativeHeapSize > 0) {
        currentMemory.nativeHeapAllocated.toFloat() / currentMemory.nativeHeapSize.toFloat() * 100f
    } else {
        0f
    }

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.memory_native_heap),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${formatBytes(
                        currentMemory.nativeHeapAllocated,
                    )} / ${formatBytes(currentMemory.nativeHeapSize)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = mem.nativeHeap,
                )
                Text(
                    text = "${usagePercent.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = mem.nativeHeap,
                )
            }

            LinearProgressIndicator(
                progress = { (usagePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WormaCeptorTokens.Elevation.lg)
                    .clip(RoundedCornerShape(WormaCeptorTokens.Radius.xs)),
                color = mem.nativeHeap,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Preview(name = "NativeHeapCard - Light")
@Composable
private fun NativeHeapCardPreview() {
    WormaCeptorTheme {
        NativeHeapCard(
            currentMemory = MemoryInfo(
                timestamp = System.currentTimeMillis(),
                usedMemory = 45_000_000L,
                freeMemory = 19_000_000L,
                totalMemory = 64_000_000L,
                maxMemory = 128_000_000L,
                heapUsagePercent = 35f,
                nativeHeapSize = 32_000_000L,
                nativeHeapAllocated = 20_000_000L,
            ),
        )
    }
}
