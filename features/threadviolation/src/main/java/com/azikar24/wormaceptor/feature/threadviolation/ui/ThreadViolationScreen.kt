package com.azikar24.wormaceptor.feature.threadviolation.ui

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringIndicator
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.core.ui.util.formatTimestampCompact
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import com.azikar24.wormaceptor.feature.threadviolation.R
import com.azikar24.wormaceptor.feature.threadviolation.ui.theme.threadViolationColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
                        Text(
                            text = stringResource(R.string.threadviolation_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        WormaCeptorMonitoringIndicator(
                            isActive = isMonitoring,
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
                                contentDescription = stringResource(R.string.threadviolation_back),
                            )
                        }
                    }
                },
                actions = {
                    WormaCeptorPlayPauseButton(
                        isActive = isMonitoring,
                        onToggle = onToggleMonitoring,
                        activeContentDescription = stringResource(R.string.threadviolation_action_stop),
                        inactiveContentDescription = stringResource(R.string.threadviolation_action_start),
                    )
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
                WormaCeptorEmptyState(
                    title = stringResource(
                        if (isMonitoring) {
                            R.string.threadviolation_empty_monitoring
                        } else {
                            R.string.threadviolation_empty_no_violations
                        },
                    ),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    subtitle = stringResource(
                        if (isMonitoring) {
                            R.string.threadviolation_empty_hint_monitoring
                        } else {
                            R.string.threadviolation_empty_hint_start
                        },
                    ),
                    icon = Icons.Default.Warning,
                )
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
private fun SummarySection(
    stats: ViolationStats,
    colors: com.azikar24.wormaceptor.feature.threadviolation.ui.theme.ThreadViolationColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        WormaCeptorSummaryCard(
            count = stats.diskReadCount.toString(),
            label = stringResource(R.string.threadviolation_summary_disk_read),
            color = colors.diskRead,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = stats.diskWriteCount.toString(),
            label = stringResource(R.string.threadviolation_summary_disk_write),
            color = colors.diskWrite,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = stats.networkCount.toString(),
            label = stringResource(R.string.threadviolation_summary_network),
            color = colors.network,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = (stats.slowCallCount + stats.customSlowCodeCount).toString(),
            label = stringResource(R.string.threadviolation_summary_slow),
            color = colors.slowCall,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
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
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.threadviolation_filter_all)) },
            modifier = Modifier.semantics { selected = selectedType == null },
        )
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
            val isSelected = selectedType == type
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                label = { Text(stringResource(labelRes)) },
                leadingIcon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )
                },
                modifier = Modifier.semantics { selected = isSelected },
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
                    .size(WormaCeptorDesignSystem.TouchTarget.minimum)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .background(typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = violation.violationType.name.replace("_", " "),
                    tint = typeColor,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
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
                        formatTimestamp(violation.timestamp),
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
                    stringResource(
                        R.string.threadviolation_detail_label_time,
                    ) to formatTimestampCompact(violation.timestamp),
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
                                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
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

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun ThreadViolationScreenPreview() {
    WormaCeptorTheme {
        ThreadViolationScreen(
            violations = persistentListOf(
                ThreadViolation(
                    id = 1L,
                    timestamp = System.currentTimeMillis(),
                    violationType = ViolationType.DISK_READ,
                    description = "SharedPreferences read on main thread",
                    stackTrace = listOf(
                        "com.example.app.SettingsRepository.getPrefs(SettingsRepository.kt:42)",
                        "com.example.app.MainActivity.onCreate(MainActivity.kt:28)",
                    ),
                    durationMs = 15L,
                    threadName = "main",
                ),
                ThreadViolation(
                    id = 2L,
                    timestamp = System.currentTimeMillis() - 5000L,
                    violationType = ViolationType.NETWORK,
                    description = "Network call on main thread",
                    stackTrace = listOf(
                        "com.example.app.ApiClient.fetch(ApiClient.kt:55)",
                    ),
                    durationMs = 230L,
                    threadName = "main",
                ),
                ThreadViolation(
                    id = 3L,
                    timestamp = System.currentTimeMillis() - 10000L,
                    violationType = ViolationType.DISK_WRITE,
                    description = "Database write on main thread",
                    stackTrace = emptyList(),
                    durationMs = 45L,
                    threadName = "main",
                ),
            ),
            stats = ViolationStats(
                totalViolations = 3,
                diskReadCount = 1,
                diskWriteCount = 1,
                networkCount = 1,
                slowCallCount = 0,
                customSlowCodeCount = 0,
            ),
            isMonitoring = true,
            selectedType = null,
            selectedViolation = null,
            onToggleMonitoring = {},
            onTypeSelected = {},
            onViolationSelected = {},
            onDismissDetail = {},
            onClearViolations = {},
            onBack = {},
        )
    }
}
