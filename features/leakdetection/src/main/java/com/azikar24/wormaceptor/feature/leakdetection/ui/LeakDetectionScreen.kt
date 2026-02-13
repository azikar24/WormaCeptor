package com.azikar24.wormaceptor.feature.leakdetection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringIndicator
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import com.azikar24.wormaceptor.feature.leakdetection.R
import com.azikar24.wormaceptor.feature.leakdetection.ui.theme.leakDetectionColors
import kotlinx.collections.immutable.ImmutableList

/**
 * Main screen for Memory Leak Detection.
 *
 * Features:
 * - Summary cards showing leak counts by severity
 * - Severity filter chips
 * - Leak list with detail sheet
 * - Manual trigger check button
 * - Monitoring status indicator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeakDetectionScreen(
    leaks: ImmutableList<LeakInfo>,
    summary: LeakSummary,
    isRunning: Boolean,
    selectedSeverity: LeakSeverity?,
    selectedLeak: LeakInfo?,
    onSeveritySelected: (LeakSeverity?) -> Unit,
    onLeakSelected: (LeakInfo) -> Unit,
    onDismissDetail: () -> Unit,
    onTriggerCheck: () -> Unit,
    onClearLeaks: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = leakDetectionColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.leakdetection_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        WormaCeptorMonitoringIndicator(
                            isActive = isRunning,
                            activeColor = colors.monitoring,
                            inactiveColor = colors.idle,
                        )
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.leakdetection_back),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onTriggerCheck) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.leakdetection_trigger_check),
                        )
                    }
                    IconButton(onClick = onClearLeaks) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.leakdetection_clear),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Summary cards
            SummarySection(
                summary = summary,
                colors = colors,
            )

            // Severity filter chips
            SeverityFilterChips(
                selectedSeverity = selectedSeverity,
                onSeveritySelected = onSeveritySelected,
                colors = colors,
            )

            // Leak list
            if (leaks.isEmpty()) {
                WormaCeptorEmptyState(
                    title = stringResource(
                        if (isRunning) R.string.leakdetection_empty_monitoring else R.string.leakdetection_empty_no_leaks,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    subtitle = stringResource(
                        if (isRunning) {
                            R.string.leakdetection_empty_hint_monitoring
                        } else {
                            R.string.leakdetection_empty_hint_start
                        },
                    ),
                    icon = Icons.Default.BugReport,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    items(leaks, key = { "${it.timestamp}_${it.objectClass}" }) { leak ->
                        LeakCard(
                            leak = leak,
                            onClick = { onLeakSelected(leak) },
                            colors = colors,
                        )
                    }
                }
            }
        }

        // Detail sheet
        selectedLeak?.let { leak ->
            ModalBottomSheet(
                onDismissRequest = onDismissDetail,
                sheetState = sheetState,
            ) {
                LeakDetailContent(
                    leak = leak,
                    colors = colors,
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    summary: LeakSummary,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        WormaCeptorSummaryCard(
            count = summary.criticalCount.toString(),
            label = stringResource(R.string.leakdetection_severity_critical),
            color = colors.critical,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.criticalBackground,
            labelColor = colors.critical.copy(alpha = 1f - WormaCeptorDesignSystem.Alpha.medium),
        )
        WormaCeptorSummaryCard(
            count = summary.highCount.toString(),
            label = stringResource(R.string.leakdetection_severity_high),
            color = colors.high,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.highBackground,
            labelColor = colors.high.copy(alpha = 1f - WormaCeptorDesignSystem.Alpha.medium),
        )
        WormaCeptorSummaryCard(
            count = summary.mediumCount.toString(),
            label = stringResource(R.string.leakdetection_severity_medium),
            color = colors.medium,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.mediumBackground,
            labelColor = colors.medium.copy(alpha = 1f - WormaCeptorDesignSystem.Alpha.medium),
        )
        WormaCeptorSummaryCard(
            count = summary.lowCount.toString(),
            label = stringResource(R.string.leakdetection_severity_low),
            color = colors.low,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.lowBackground,
            labelColor = colors.low.copy(alpha = 1f - WormaCeptorDesignSystem.Alpha.medium),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeverityFilterChips(
    selectedSeverity: LeakSeverity?,
    onSeveritySelected: (LeakSeverity?) -> Unit,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        FilterChip(
            selected = selectedSeverity == null,
            onClick = { onSeveritySelected(null) },
            label = { Text(stringResource(R.string.leakdetection_filter_all)) },
            modifier = Modifier.semantics { selected = selectedSeverity == null },
        )
        LeakSeverity.entries.forEach { severity ->
            val color = colors.colorForSeverity(severity)
            val isSelected = selectedSeverity == severity
            FilterChip(
                selected = isSelected,
                onClick = { onSeveritySelected(if (isSelected) null else severity) },
                label = { Text(severity.name) },
                modifier = Modifier.semantics { selected = isSelected },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                    selectedLabelColor = color,
                ),
            )
        }
    }
}

@Composable
private fun LeakCard(
    leak: LeakInfo,
    onClick: () -> Unit,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    val severityColor = colors.colorForSeverity(leak.severity)
    val severityBackground = colors.backgroundForSeverity(leak.severity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Severity indicator
            Box(
                modifier = Modifier
                    .size(WormaCeptorDesignSystem.TouchTarget.minimum)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .background(severityBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = leak.severity.name,
                    tint = severityColor,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = leak.objectClass.substringAfterLast('.'),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = leak.leakDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.labelSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTimestamp(leak.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.labelSecondary,
                    )
                    Text(
                        text = formatBytes(leak.retainedSize),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = severityColor,
                    )
                }
            }

            // Severity badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                color = severityBackground,
            ) {
                Text(
                    text = leak.severity.name.take(1),
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = severityColor,
                )
            }
        }
    }
}

@Composable
private fun LeakDetailContent(
    leak: LeakInfo,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    val severityColor = colors.colorForSeverity(leak.severity)
    val severityBackground = colors.backgroundForSeverity(leak.severity)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorDesignSystem.Spacing.xxxl)
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg))
                        .background(severityBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = stringResource(R.string.leakdetection_title),
                        tint = severityColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                    )
                }
                Column {
                    Text(
                        text = leak.objectClass.substringAfterLast('.'),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                            color = severityBackground,
                        ) {
                            Text(
                                text = leak.severity.name,
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = severityColor,
                            )
                        }
                        Text(
                            text = formatBytes(leak.retainedSize),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = severityColor,
                        )
                    }
                }
            }
        }

        // Details
        item {
            DetailSection(
                title = stringResource(R.string.leakdetection_section_details),
                items = listOf(
                    stringResource(R.string.leakdetection_detail_class) to leak.objectClass,
                    stringResource(R.string.leakdetection_detail_description) to leak.leakDescription,
                    stringResource(R.string.leakdetection_detail_retained_size) to formatBytes(leak.retainedSize),
                    stringResource(R.string.leakdetection_detail_detected) to formatTimestampFull(leak.timestamp),
                ),
                colors = colors,
            )
        }

        // Reference path
        if (leak.referencePath.isNotEmpty()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Text(
                        text = stringResource(R.string.leakdetection_section_reference_path),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelSecondary,
                        modifier = Modifier.semantics { heading() },
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                        color = colors.detailBackground,
                    ) {
                        Column(
                            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                        ) {
                            leak.referencePath.forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.valuePrimary,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            color = colors.detailBackground,
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                items.forEach { (label, value) ->
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.labelSecondary,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.valuePrimary,
                        )
                    }
                }
            }
        }
    }
}
