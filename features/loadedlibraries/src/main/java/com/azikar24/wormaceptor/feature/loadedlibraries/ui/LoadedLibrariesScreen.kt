package com.azikar24.wormaceptor.feature.loadedlibraries.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Memory
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.loadedLibrariesColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadedLibrariesScreen(
    libraries: ImmutableList<LoadedLibrary>,
    summary: LibrarySummary,
    isLoading: Boolean,
    error: String?,
    selectedType: LoadedLibrary.LibraryType?,
    showSystemLibs: Boolean,
    searchQuery: String,
    selectedLibrary: LoadedLibrary?,
    onTypeSelected: (LoadedLibrary.LibraryType?) -> Unit,
    onShowSystemLibsChanged: (Boolean) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onLibrarySelected: (LoadedLibrary) -> Unit,
    onDismissDetail: () -> Unit,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = loadedLibrariesColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.loadedlibraries_title), fontWeight = FontWeight.SemiBold)
                    },
                    navigationIcon = {
                        onBack?.let {
                            IconButton(
                                onClick = it,
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    stringResource(R.string.loadedlibraries_back),
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
                                stringResource(R.string.loadedlibraries_search),
                            )
                        }
                        IconButton(onClick = onRefresh, enabled = !isLoading) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                                    strokeWidth = WormaCeptorDesignSystem.BorderWidth.thick,
                                )
                            } else {
                                Icon(Icons.Default.Refresh, stringResource(R.string.loadedlibraries_refresh))
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
                        placeholder = stringResource(R.string.loadedlibraries_search_placeholder),
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
                selectedType,
                showSystemLibs,
                onTypeSelected,
                onShowSystemLibsChanged,
                colors,
                Modifier.fillMaxWidth().padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
            )
            Spacer(Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            error?.let {
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
                    WormaCeptorDesignSystem.Shapes.card,
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

            if (libraries.isEmpty() && !isLoading) {
                WormaCeptorEmptyState(
                    title = stringResource(R.string.loadedlibraries_empty),
                    modifier = Modifier.fillMaxSize(),
                    icon = Icons.Default.Extension,
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
                    items(libraries, key = { it.path }) { lib ->
                        LibraryCard(lib, { onLibrarySelected(lib) }, colors)
                    }
                }
            }
        }

        selectedLibrary?.let { lib ->
            ModalBottomSheet(onDismissRequest = onDismissDetail, sheetState = sheetState) {
                LibraryDetailContent(lib, colors, Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
            }
        }
    }
}

@Composable
private fun SummarySection(
    summary: LibrarySummary,
    colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors,
    modifier: Modifier,
) {
    Row(modifier, Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        WormaCeptorSummaryCard(
            count = summary.nativeSoCount.toString(),
            label = stringResource(R.string.loadedlibraries_summary_native),
            color = colors.nativeSo,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.dexCount.toString(),
            label = stringResource(R.string.loadedlibraries_summary_dex),
            color = colors.dex,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.jarCount.toString(),
            label = stringResource(R.string.loadedlibraries_summary_jar),
            color = colors.jar,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.totalLibraries.toString(),
            label = stringResource(R.string.loadedlibraries_summary_total),
            color = colors.primary,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    selectedType: LoadedLibrary.LibraryType?,
    showSystemLibs: Boolean,
    onTypeSelected: (LoadedLibrary.LibraryType?) -> Unit,
    onShowSystemLibsChanged: (Boolean) -> Unit,
    colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors,
    modifier: Modifier,
) {
    Column(modifier, Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text(stringResource(R.string.loadedlibraries_filter_all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(WormaCeptorDesignSystem.Alpha.medium),
                ),
            )
            LoadedLibrary.LibraryType.entries.filter { it != LoadedLibrary.LibraryType.AAR_RESOURCE }.forEach { type ->
                val (icon, labelRes, color) = when (type) {
                    LoadedLibrary.LibraryType.NATIVE_SO -> Triple(
                        Icons.Default.Memory,
                        R.string.loadedlibraries_filter_native,
                        colors.nativeSo,
                    )
                    LoadedLibrary.LibraryType.DEX -> Triple(
                        Icons.Default.Android,
                        R.string.loadedlibraries_filter_dex,
                        colors.dex,
                    )
                    LoadedLibrary.LibraryType.JAR -> Triple(
                        Icons.Default.Code,
                        R.string.loadedlibraries_filter_jar,
                        colors.jar,
                    )
                    else -> Triple(Icons.Default.Extension, R.string.loadedlibraries_filter_other, colors.primary)
                }
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(if (selectedType == type) null else type) },
                    label = { Text(stringResource(labelRes)) },
                    leadingIcon = { Icon(icon, null, Modifier.size(WormaCeptorDesignSystem.IconSize.sm)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(WormaCeptorDesignSystem.Alpha.medium),
                        selectedLabelColor = color,
                        selectedLeadingIconColor = color,
                    ),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(
                stringResource(R.string.loadedlibraries_show_system),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.labelPrimary,
            )
            Switch(showSystemLibs, onShowSystemLibsChanged)
        }
    }
}

@Composable
private fun LibraryCard(
    library: LoadedLibrary,
    onClick: () -> Unit,
    colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors,
) {
    val (icon, color) = when (library.type) {
        LoadedLibrary.LibraryType.NATIVE_SO -> Icons.Default.Memory to colors.nativeSo
        LoadedLibrary.LibraryType.DEX -> Icons.Default.Android to colors.dex
        LoadedLibrary.LibraryType.JAR -> Icons.Default.Code to colors.jar
        LoadedLibrary.LibraryType.AAR_RESOURCE -> Icons.Default.Extension to colors.primary
    }

    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        WormaCeptorDesignSystem.Shapes.card,
        CardDefaults.cardColors(colors.cardBackground),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(
                    40.dp,
                ).clip(WormaCeptorDesignSystem.Shapes.card).background(color.copy(WormaCeptorDesignSystem.Alpha.light)),
                Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md))
            }
            Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    library.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    library.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.labelSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
                    library.size?.let {
                        Text(
                            formatBytes(it),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = color,
                        )
                    }
                    if (library.isSystemLibrary) {
                        Text(
                            stringResource(R.string.loadedlibraries_badge_system),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.systemBadge,
                        )
                    }
                }
            }
            Spacer(Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Surface(
                shape = WormaCeptorDesignSystem.Shapes.chip,
                color = color.copy(WormaCeptorDesignSystem.Alpha.light),
            ) {
                Text(
                    library.type.name.take(3),
                    Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun LibraryDetailContent(
    library: LoadedLibrary,
    colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors,
    modifier: Modifier,
) {
    val (icon, color) = when (library.type) {
        LoadedLibrary.LibraryType.NATIVE_SO -> Icons.Default.Memory to colors.nativeSo
        LoadedLibrary.LibraryType.DEX -> Icons.Default.Android to colors.dex
        LoadedLibrary.LibraryType.JAR -> Icons.Default.Code to colors.jar
        LoadedLibrary.LibraryType.AAR_RESOURCE -> Icons.Default.Extension to colors.primary
    }

    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Box(
                Modifier.size(
                    WormaCeptorDesignSystem.Spacing.xxxl,
                ).clip(WormaCeptorDesignSystem.Shapes.card).background(color.copy(WormaCeptorDesignSystem.Alpha.light)),
                Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl))
            }
            Column {
                Text(
                    library.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                )
                Text(library.type.name.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = color)
            }
        }

        val detailTitle = stringResource(R.string.loadedlibraries_detail_title)
        val pathLabel = stringResource(R.string.loadedlibraries_detail_path)
        val sizeLabel = stringResource(R.string.loadedlibraries_detail_size)
        val loadAddressLabel = stringResource(R.string.loadedlibraries_detail_load_address)
        val versionLabel = stringResource(R.string.loadedlibraries_detail_version)
        val typeLabel = stringResource(R.string.loadedlibraries_detail_type)
        val systemLibraryType = stringResource(R.string.loadedlibraries_type_system)
        val appLibraryType = stringResource(R.string.loadedlibraries_type_app)

        DetailSection(
            detailTitle,
            listOfNotNull(
                pathLabel to library.path,
                library.size?.let { sizeLabel to formatBytes(it) },
                library.loadAddress?.let { loadAddressLabel to it },
                library.version?.let { versionLabel to it },
                typeLabel to if (library.isSystemLibrary) systemLibraryType else appLibraryType,
            ),
            colors,
        )

        Spacer(Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors,
) {
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        Text(
            title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(Modifier.fillMaxWidth(), WormaCeptorDesignSystem.Shapes.card, colors.searchBackground) {
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

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun LoadedLibrariesScreenPreview() {
    WormaCeptorTheme {
        LoadedLibrariesScreen(
            libraries = persistentListOf(
                LoadedLibrary(
                    name = "libc.so",
                    path = "/system/lib64/libc.so",
                    type = LoadedLibrary.LibraryType.NATIVE_SO,
                    size = 1_200_000L,
                    loadAddress = "0x7f8a000000",
                    version = null,
                    isSystemLibrary = true,
                ),
                LoadedLibrary(
                    name = "classes.dex",
                    path = "/data/app/com.example/base.apk!classes.dex",
                    type = LoadedLibrary.LibraryType.DEX,
                    size = 4_500_000L,
                    loadAddress = null,
                    version = null,
                    isSystemLibrary = false,
                ),
                LoadedLibrary(
                    name = "okhttp.jar",
                    path = "/data/app/com.example/lib/okhttp.jar",
                    type = LoadedLibrary.LibraryType.JAR,
                    size = 800_000L,
                    loadAddress = null,
                    version = "4.12.0",
                    isSystemLibrary = false,
                ),
            ),
            summary = LibrarySummary(
                totalLibraries = 3,
                nativeSoCount = 1,
                dexCount = 1,
                jarCount = 1,
                totalSizeBytes = 6_500_000L,
                systemLibraryCount = 1,
                appLibraryCount = 2,
            ),
            isLoading = false,
            error = null,
            selectedType = null,
            showSystemLibs = true,
            searchQuery = "",
            selectedLibrary = null,
            onTypeSelected = {},
            onShowSystemLibsChanged = {},
            onSearchQueryChanged = {},
            onLibrarySelected = {},
            onDismissDetail = {},
            onRefresh = {},
            onBack = {},
        )
    }
}
