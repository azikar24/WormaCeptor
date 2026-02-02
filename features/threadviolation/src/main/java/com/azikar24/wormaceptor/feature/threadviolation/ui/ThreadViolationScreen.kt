/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.threadviolation.ui

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import com.azikar24.wormaceptor.feature.threadviolation.R
import com.azikar24.wormaceptor.feature.threadviolation.ui.theme.threadViolationColors
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen for Thread Violation Detection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadViolationScreen(
    violations: ImmutableList<ThreadViolation>,
    stats: ViolationStats,
    isMonitoring: Boolean,
    selectedType: ViolationType?,
    selectedViolation: ThreadViolation?,
    onToggleMonitoring: () -> Unit,
    onTypeSelected: (ViolationType?) -> Unit,
    onViolationSelected: (ThreadViolation) -> Unit,
    onDismissDetail: () -> Unit,
    onClearViolations: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = threadViolationColors()
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
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.primary,
                        )
                        Text(
                            text = stringResource(R.string.threadviolation_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        MonitoringIndicator(isMonitoring = isMonitoring, colors = colors)
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.threadviolation_back),
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleMonitoring) {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = stringResource(
                                if (isMonitoring) {
                                    R.string.threadviolation_action_stop
                                } else {
                                    R.string.threadviolation_action_start
                                },
                            ),
                            tint = if (isMonitoring) colors.monitoring else colors.idle,
                        )
                    }
                    IconButton(onClick = onClearViolations) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.threadviolation_clear),
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
            SummarySection(stats = stats, colors = colors)

            // Type filter chips
            TypeFilterChips(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                colors = colors,
            )

            // Violations list
            if (violations.isEmpty()) {
                EmptyState(isMonitoring = isMonitoring, colors = colors, modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    items(violations, key = { it.id }) { violation ->
                        ViolationCard(
                            violation = violation,
                            onClick = { onViolationSelected(violation) },
                            colors = colors,
                        )
                    }
                }
            }
        }

        selectedViolation?.let { violation ->
            ModalBottomSheet(
                modifier = modifier.padding(top = WormaCeptorDesignSystem.Spacing.xxxl),
                onDismissRequest = onDismissDetail,
                sheetState = sheetState,
                shape = WormaCeptorDesignSystem.Shapes.sheet,
            ) {
                ViolationDetailContent(
                    violation = violation,
                    colors = colors,
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun MonitoringIndicator(
    isMonitoring: Boolean,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
) {
    val color by animateColorAsState(
        targetValue = if (isMonitoring) colors.monitoring else colors.idle,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.slow),
        label = "monitoring",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isMonitoring) 0.5f else 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse_alpha",
    )

    Box(
        modifier = Modifier
            .size(WormaCeptorDesignSystem.Spacing.sm)
            .clip(CircleShape)
            .background(color.copy(alpha = if (isMonitoring) alpha else 1f)),
    )
}

@Composable
private fun SummarySection(
    stats: ViolationStats,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        SummaryCard(
            stringResource(R.string.threadviolation_summary_disk_read),
            stats.diskReadCount,
            colors.diskRead,
            Modifier.weight(1f),
        )
        SummaryCard(
            stringResource(R.string.threadviolation_summary_disk_write),
            stats.diskWriteCount,
            colors.diskWrite,
            Modifier.weight(1f),
        )
        SummaryCard(
            stringResource(R.string.threadviolation_summary_network),
            stats.networkCount,
            colors.network,
            Modifier.weight(1f),
        )
        SummaryCard(
            stringResource(R.string.threadviolation_summary_slow),
            stats.slowCallCount + stats.customSlowCodeCount,
            colors.slowCall,
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    val colors = threadViolationColors()
    Card(
        modifier = modifier,
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = colors.labelSecondary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeFilterChips(
    selectedType: ViolationType?,
    onTypeSelected: (ViolationType?) -> Unit,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        FilterChip(selected = selectedType == null, onClick = {
            onTypeSelected(null)
        }, label = { Text(stringResource(R.string.threadviolation_filter_all)) })
        ViolationType.entries.forEach { type ->
            val (icon, labelRes, color) = when (type) {
                ViolationType.DISK_READ -> Triple(
                    Icons.Default.SaveAlt,
                    R.string.threadviolation_filter_read,
                    colors.diskRead,
                )
                ViolationType.DISK_WRITE -> Triple(
                    Icons.Default.Storage,
                    R.string.threadviolation_filter_write,
                    colors.diskWrite,
                )
                ViolationType.NETWORK -> Triple(
                    Icons.Default.Cloud,
                    R.string.threadviolation_filter_network,
                    colors.network,
                )
                ViolationType.SLOW_CALL -> Triple(
                    Icons.Default.SlowMotionVideo,
                    R.string.threadviolation_filter_slow,
                    colors.slowCall,
                )
                ViolationType.CUSTOM_SLOW_CODE -> Triple(
                    Icons.Default.Speed,
                    R.string.threadviolation_filter_custom,
                    colors.customSlowCode,
                )
            }
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                label = { Text(stringResource(labelRes)) },
                leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                    selectedLabelColor = color,
                    selectedLeadingIconColor = color,
                ),
            )
        }
    }
}

@Composable
private fun ViolationCard(
    violation: ThreadViolation,
    onClick: () -> Unit,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
) {
    val typeColor = colors.colorForType(violation.violationType)
    val icon = when (violation.violationType) {
        ViolationType.DISK_READ -> Icons.Default.SaveAlt
        ViolationType.DISK_WRITE -> Icons.Default.Storage
        ViolationType.NETWORK -> Icons.Default.Cloud
        ViolationType.SLOW_CALL -> Icons.Default.SlowMotionVideo
        ViolationType.CUSTOM_SLOW_CODE -> Icons.Default.Speed
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = WormaCeptorDesignSystem.Shapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .background(typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = typeColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = violation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
                    Text(
                        formatTime(violation.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.labelSecondary,
                    )
                    violation.durationMs?.let {
                        Text(
                            "${it}ms",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = typeColor,
                        )
                    }
                }
            }
            Surface(
                shape = WormaCeptorDesignSystem.Shapes.chip,
                color = typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
            ) {
                Text(
                    text = violation.violationType.name.take(1),
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                )
            }
        }
    }
}

@Composable
private fun ViolationDetailContent(
    violation: ThreadViolation,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
    modifier: Modifier = Modifier,
) {
    val typeColor = colors.colorForType(violation.violationType)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorDesignSystem.Spacing.xxxl)
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg))
                        .background(typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        null,
                        tint = typeColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                    )
                }
                Column {
                    Text(
                        violation.violationType.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    Text(
                        violation.threadName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.labelSecondary,
                    )
                }
            }
        }

        item {
            DetailSection(
                stringResource(R.string.threadviolation_detail_section_details),
                listOf(
                    stringResource(R.string.threadviolation_detail_label_description) to violation.description,
                    stringResource(R.string.threadviolation_detail_label_thread) to violation.threadName,
                    stringResource(R.string.threadviolation_detail_label_time) to formatTimeFull(violation.timestamp),
                ) + (
                    violation.durationMs?.let {
                        listOf(stringResource(R.string.threadviolation_detail_label_duration) to "${it}ms")
                    } ?: emptyList()
                    ),
                colors,
            )
        }

        if (violation.stackTrace.isNotEmpty()) {
            item {
                val clipboardManager = LocalClipboardManager.current
                Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.threadviolation_detail_section_stack_trace),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.labelSecondary,
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(
                                    AnnotatedString(violation.stackTrace.joinToString("\n")),
                                )
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.threadviolation_copy_stack),
                                tint = colors.labelSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Surface(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                        colors.detailBackground,
                    ) {
                        Column(
                            Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                            Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
                        ) {
                            violation.stackTrace.forEach {
                                Text(
                                    it,
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

        item { Spacer(Modifier.height(WormaCeptorDesignSystem.Spacing.lg)) }
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
) {
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            colors.detailBackground,
        ) {
            Column(
                Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                items.forEach { (label, value) ->
                    Column {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.labelSecondary)
                        Text(
                            value,
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
    isMonitoring: Boolean,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Icon(
                Icons.Default.Warning,
                null,
                tint = if (isMonitoring) colors.monitoring else colors.labelSecondary,
                modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xxxl),
            )
            Text(
                stringResource(
                    if (isMonitoring) {
                        R.string.threadviolation_empty_monitoring
                    } else {
                        R.string.threadviolation_empty_no_violations
                    },
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.labelSecondary,
            )
            Text(
                stringResource(
                    if (isMonitoring) {
                        R.string.threadviolation_empty_hint_monitoring
                    } else {
                        R.string.threadviolation_empty_hint_start
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.labelSecondary,
            )
        }
    }
}

private fun formatTime(timestamp: Long) = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(timestamp))
private fun formatTimeFull(timestamp: Long) = SimpleDateFormat("MMM d, HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
