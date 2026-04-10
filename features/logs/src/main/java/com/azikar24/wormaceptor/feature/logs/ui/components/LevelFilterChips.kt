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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
    onToggle: () -> Unit,
) {
    val logColors = WormaCeptorTokens.Colors.LogLevel
    val levelColor = when (level) {
        LogLevel.VERBOSE -> logColors.verbose
        LogLevel.DEBUG -> logColors.debug
        LogLevel.INFO -> logColors.info
        LogLevel.WARN -> logColors.warn
        LogLevel.ERROR -> logColors.error
        LogLevel.ASSERT -> logColors.assert
    }

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
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    )
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    )
                },
            )
        }
    }
}

private const val CountOverflowThreshold = 999
