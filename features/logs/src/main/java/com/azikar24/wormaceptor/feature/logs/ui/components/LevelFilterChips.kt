package com.azikar24.wormaceptor.feature.logs.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LogLevel
import com.azikar24.wormaceptor.feature.logs.R

@Composable
internal fun LevelFilterChips(
    selectedLevels: Set<LogLevel>,
    levelCounts: Map<LogLevel, Int>,
    onLevelToggle: (LogLevel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = WormaCeptorTokens.Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        LogLevel.entries.forEach { level ->
            LevelFilterChip(
                level = level,
                isSelected = level in selectedLevels,
                count = levelCounts[level] ?: 0,
                levelColor = logLevelColor(level),
                onToggle = { onLevelToggle(level) },
            )
        }
    }
}

@Composable
private fun LevelFilterChip(
    level: LogLevel,
    isSelected: Boolean,
    count: Int,
    levelColor: Color,
    onToggle: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        modifier = Modifier.semantics { selected = isSelected },
        label = {
            LevelChipLabel(
                tag = level.tag,
                count = count,
                isSelected = isSelected,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = levelColor.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
            selectedLabelColor = levelColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = levelColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
            selectedBorderColor = levelColor.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
            enabled = true,
            selected = isSelected,
        ),
    )
}

@Composable
private fun LevelChipLabel(
    tag: String,
    count: Int,
    isSelected: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        if (count > 0) {
            Text(
                text = if (count > CountOverflowThreshold) {
                    stringResource(R.string.logs_count_overflow)
                } else {
                    count.toString()
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) {
                    WormaCeptorTokens.semantic().accent.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    )
                } else {
                    WormaCeptorTokens.semantic().textSecondary.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    )
                },
            )
        }
    }
}

private const val CountOverflowThreshold = 999

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun LevelFilterChipsPreview(
    @PreviewParameter(LevelFilterChipsPreviewProvider::class) params: LevelFilterChipsPreviewData,
) {
    WormaCeptorTheme {
        LevelFilterChips(
            selectedLevels = params.selectedLevels,
            levelCounts = params.levelCounts,
            onLevelToggle = {},
        )
    }
}

private data class LevelFilterChipsPreviewData(
    val selectedLevels: Set<LogLevel>,
    val levelCounts: Map<LogLevel, Int>,
)

@Suppress("MagicNumber")
private class LevelFilterChipsPreviewProvider : PreviewParameterProvider<LevelFilterChipsPreviewData> {
    private val sampleCounts = mapOf(
        LogLevel.VERBOSE to 5,
        LogLevel.DEBUG to 12,
        LogLevel.INFO to 8,
        LogLevel.WARN to 3,
        LogLevel.ERROR to 1,
        LogLevel.ASSERT to 0,
    )

    override val values: Sequence<LevelFilterChipsPreviewData> = sequenceOf(
        LevelFilterChipsPreviewData(
            selectedLevels = LogLevel.entries.toSet(),
            levelCounts = sampleCounts,
        ),
        LevelFilterChipsPreviewData(
            selectedLevels = setOf(LogLevel.ERROR, LogLevel.WARN),
            levelCounts = sampleCounts,
        ),
    )
}
