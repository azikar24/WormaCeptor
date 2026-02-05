/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.leakdetection.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import com.azikar24.wormaceptor.feature.leakdetection.R
import com.azikar24.wormaceptor.feature.leakdetection.ui.theme.leakDetectionColors
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = null,
                            tint = colors.critical,
                        )
                        Text(
                            text = stringResource(R.string.leakdetection_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        // Monitoring indicator
                        MonitoringIndicator(
                            isRunning = isRunning,
                            colors = colors,
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
                EmptyState(
                    isRunning = isRunning,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
private fun MonitoringIndicator(
    isRunning: Boolean,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (isRunning) colors.monitoring else colors.idle,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.slow),
        label = "monitoring",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRunning) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Box(
        modifier = modifier
            .size(WormaCeptorDesignSystem.Spacing.sm)
            .clip(CircleShape)
            .background(color.copy(alpha = if (isRunning) alpha else 1f)),
    )
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
        SummaryCard(
            count = summary.criticalCount,
            label = stringResource(R.string.leakdetection_severity_critical),
            color = colors.critical,
            backgroundColor = colors.criticalBackground,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            count = summary.highCount,
            label = stringResource(R.string.leakdetection_severity_high),
            color = colors.high,
            backgroundColor = colors.highBackground,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            count = summary.mediumCount,
            label = stringResource(R.string.leakdetection_severity_medium),
            color = colors.medium,
            backgroundColor = colors.mediumBackground,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            count = summary.lowCount,
            label = stringResource(R.string.leakdetection_severity_low),
            color = colors.low,
            backgroundColor = colors.lowBackground,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    count: Int,
    label: String,
    color: Color,
    backgroundColor: Color,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 1f - WormaCeptorDesignSystem.Alpha.medium),
            )
        }
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
        )
        LeakSeverity.entries.forEach { severity ->
            val color = colors.colorForSeverity(severity)
            FilterChip(
                selected = selectedSeverity == severity,
                onClick = { onSeveritySelected(if (selectedSeverity == severity) null else severity) },
                label = { Text(severity.name) },
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
                    .size(40.dp)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .background(severityBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(20.dp),
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
                        text = formatSize(leak.retainedSize),
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
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(24.dp),
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
                            text = formatSize(leak.retainedSize),
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
                    stringResource(R.string.leakdetection_detail_retained_size) to formatSize(leak.retainedSize),
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

@Composable
private fun EmptyState(
    isRunning: Boolean,
    colors: com.azikar24.wormaceptor.feature.leakdetection.ui.theme.LeakDetectionColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = if (isRunning) colors.monitoring else colors.labelSecondary,
                modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xxxl),
            )
            Text(
                text = stringResource(
                    if (isRunning) R.string.leakdetection_empty_monitoring else R.string.leakdetection_empty_no_leaks,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.labelSecondary,
            )
            Text(
                text = stringResource(
                    if (isRunning) {
                        R.string.leakdetection_empty_hint_monitoring
                    } else {
                        R.string.leakdetection_empty_hint_start
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.labelSecondary,
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
    return sdf.format(Date(timestamp))
}

private fun formatTimestampFull(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' HH:mm:ss", Locale.US)
    return sdf.format(Date(timestamp))
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format(Locale.US, "%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format(Locale.US, "%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}
