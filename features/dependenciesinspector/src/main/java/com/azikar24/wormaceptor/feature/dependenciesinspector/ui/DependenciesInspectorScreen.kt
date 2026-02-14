package com.azikar24.wormaceptor.feature.dependenciesinspector.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.feature.dependenciesinspector.R
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.theme.DependenciesInspectorColors
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.theme.dependenciesInspectorColors
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.theme.shortLabel
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesInspectorScreen(
    dependencies: ImmutableList<DependencyInfo>,
    summary: DependencySummary,
    isLoading: Boolean,
    error: String?,
    selectedCategory: DependencyCategory?,
    searchQuery: String,
    selectedDependency: DependencyInfo?,
    showVersionedOnly: Boolean,
    onCategorySelected: (DependencyCategory?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onDependencySelected: (DependencyInfo) -> Unit,
    onDismissDetail: () -> Unit,
    onShowVersionedOnlyChanged: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = dependenciesInspectorColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.dependenciesinspector_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        onBack?.let {
                            IconButton(onClick = it) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    stringResource(R.string.dependenciesinspector_navigation_back),
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            searchActive = !searchActive
                            if (!searchActive) onSearchQueryChanged("")
                        }) {
                            Icon(
                                if (searchActive) Icons.Default.Close else Icons.Default.Search,
                                stringResource(R.string.dependenciesinspector_action_search),
                            )
                        }
                        IconButton(onClick = onRefresh, enabled = !isLoading) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                                    strokeWidth = WormaCeptorDesignSystem.BorderWidth.thick,
                                )
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    stringResource(R.string.dependenciesinspector_action_refresh),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                )
                AnimatedVisibility(
                    visible = searchActive,
                    enter = expandVertically(
                        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                    ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
                    exit = shrinkVertically(
                        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                    ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
                ) {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                            .padding(top = WormaCeptorDesignSystem.Spacing.sm),
                        placeholder = stringResource(R.string.dependenciesinspector_search_placeholder),
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            SummarySection(
                summary,
                colors,
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                    .padding(top = WormaCeptorDesignSystem.Spacing.sm),
            )

            Spacer(Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            FilterSection(
                selectedCategory,
                summary,
                showVersionedOnly,
                onCategorySelected,
                onShowVersionedOnlyChanged,
                colors,
                Modifier.fillMaxWidth().padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
            )

            Spacer(Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            error?.let {
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        it,
                        Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                        MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (dependencies.isEmpty() && !isLoading) {
                WormaCeptorEmptyState(
                    title = stringResource(R.string.dependenciesinspector_empty_title),
                    modifier = Modifier.fillMaxSize(),
                    subtitle = stringResource(R.string.dependenciesinspector_empty_subtitle),
                    icon = Icons.Default.HelpOutline,
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    contentPadding = PaddingValues(
                        start = WormaCeptorDesignSystem.Spacing.lg,
                        end = WormaCeptorDesignSystem.Spacing.lg,
                        bottom = WormaCeptorDesignSystem.Spacing.lg,
                    ),
                ) {
                    items(dependencies.size) { index ->
                        val dep = dependencies[index]
                        DependencyCard(dep, { onDependencySelected(dep) }, colors)
                    }
                }
            }
        }

        selectedDependency?.let { dep ->
            ModalBottomSheet(onDismissRequest = onDismissDetail, sheetState = sheetState) {
                DependencyDetailContent(dep, colors, Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
            }
        }
    }
}

@Composable
private fun SummarySection(summary: DependencySummary, colors: DependenciesInspectorColors, modifier: Modifier) {
    Row(modifier, Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        WormaCeptorSummaryCard(
            count = summary.totalDetected.toString(),
            label = stringResource(R.string.dependenciesinspector_summary_detected),
            color = colors.primary,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.withVersion.toString(),
            label = stringResource(R.string.dependenciesinspector_summary_versioned),
            color = colors.versionDetected,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.withoutVersion.toString(),
            label = stringResource(R.string.dependenciesinspector_summary_unknown),
            color = colors.versionUnknown,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    selectedCategory: DependencyCategory?,
    summary: DependencySummary,
    showVersionedOnly: Boolean,
    onCategorySelected: (DependencyCategory?) -> Unit,
    onShowVersionedOnlyChanged: (Boolean) -> Unit,
    colors: DependenciesInspectorColors,
    modifier: Modifier,
) {
    Column(modifier, Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        // Category filter chips - scrollable horizontally
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.dependenciesinspector_filter_all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(WormaCeptorDesignSystem.Alpha.medium),
                ),
            )

            // Show categories that have detected dependencies, sorted by count
            summary.byCategory.entries
                .sortedByDescending { it.value }
                .forEach { (category, count) ->
                    val color = colors.colorForCategory(category)
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            onCategorySelected(if (selectedCategory == category) null else category)
                        },
                        label = { Text("${category.shortLabel()} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(WormaCeptorDesignSystem.Alpha.medium),
                            selectedLabelColor = color,
                        ),
                    )
                }
        }

        // Version filter toggle
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                stringResource(R.string.dependenciesinspector_filter_versioned_only),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.labelPrimary,
            )
            Switch(showVersionedOnly, onShowVersionedOnlyChanged)
        }
    }
}

@Composable
private fun DependencyCard(dependency: DependencyInfo, onClick: () -> Unit, colors: DependenciesInspectorColors) {
    val categoryColor = colors.colorForCategory(dependency.category)
    val hasVersion = dependency.version != null

    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        CardDefaults.cardColors(colors.cardBackground),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Category icon
            Box(
                Modifier.size(
                    40.dp,
                ).clip(
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                ).background(categoryColor.copy(0.15f)),
                Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Code,
                    null,
                    tint = categoryColor,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
            }

            Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            // Info
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dependency.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (hasVersion) {
                        Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                        Icon(
                            Icons.Default.CheckCircle,
                            stringResource(R.string.dependenciesinspector_status_version_detected),
                            tint = colors.versionDetected,
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xs),
                        )
                    }
                }

                Text(
                    dependency.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = colors.labelSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
                    // Version badge
                    if (hasVersion) {
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                            color = colors.versionDetected.copy(0.15f),
                        ) {
                            Text(
                                "v${dependency.version}",
                                Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.versionText,
                            )
                        }
                    }

                    // Detection method badge
                    Text(
                        dependency.detectionMethod.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.labelSecondary,
                    )
                }
            }

            // Category badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                color = categoryColor.copy(0.15f),
            ) {
                Text(
                    dependency.category.shortLabel(),
                    Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor,
                )
            }
        }
    }
}

@Composable
private fun DependencyDetailContent(
    dependency: DependencyInfo,
    colors: DependenciesInspectorColors,
    modifier: Modifier,
) {
    val categoryColor = colors.colorForCategory(dependency.category)
    val uriHandler = LocalUriHandler.current

    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg)) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Box(
                Modifier.size(
                    48.dp,
                ).clip(
                    RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
                ).background(categoryColor.copy(0.15f)),
                Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Code,
                    null,
                    tint = categoryColor,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                )
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dependency.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    if (dependency.version != null) {
                        Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                            color = colors.versionDetected.copy(0.2f),
                        ) {
                            Text(
                                "v${dependency.version}",
                                Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.versionText,
                            )
                        }
                    }
                }
                Text(
                    dependency.category.displayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = categoryColor,
                )
            }
        }

        // Description
        Text(
            dependency.description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.labelSecondary,
        )

        // Details section
        val packageLabel = stringResource(R.string.dependenciesinspector_detail_label_package)
        val groupIdLabel = stringResource(R.string.dependenciesinspector_detail_label_group_id)
        val artifactIdLabel = stringResource(R.string.dependenciesinspector_detail_label_artifact_id)
        val mavenLabel = stringResource(R.string.dependenciesinspector_detail_label_maven)
        DetailSection(
            stringResource(R.string.dependenciesinspector_detail_section_details),
            listOfNotNull(
                packageLabel to dependency.packageName,
                dependency.groupId?.let { groupIdLabel to it },
                dependency.artifactId?.let { artifactIdLabel to it },
                dependency.mavenCoordinate?.let { mavenLabel to it },
            ),
            colors,
        )

        // Detection info
        val methodLabel = stringResource(R.string.dependenciesinspector_detail_label_method)
        val confidenceLabel = stringResource(R.string.dependenciesinspector_detail_label_confidence)
        val versionStatusLabel = stringResource(R.string.dependenciesinspector_detail_label_version_status)
        val versionDetected = stringResource(R.string.dependenciesinspector_summary_detected)
        val versionUnknown = stringResource(R.string.dependenciesinspector_summary_unknown)
        DetailSection(
            stringResource(R.string.dependenciesinspector_detail_section_detection),
            listOf(
                methodLabel to dependency.detectionMethod.displayName(),
                confidenceLabel to dependency.detectionMethod.confidence(),
                versionStatusLabel to if (dependency.version != null) versionDetected else versionUnknown,
            ),
            colors,
        )

        // Website link
        dependency.website?.let { url ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(url) }
                    .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    Icons.Default.Language,
                    stringResource(R.string.dependenciesinspector_detail_section_details),
                    tint = colors.link,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
                Text(
                    url,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.link,
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
    }
}

@Composable
private fun DetailSection(title: String, items: List<Pair<String, String>>, colors: DependenciesInspectorColors) {
    if (items.isEmpty()) return

    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        Text(
            title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(
            Modifier.fillMaxWidth(),
            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            colors.searchBackground,
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
