/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.loadedlibraries.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary.LibraryType
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.loadedLibrariesColors
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadedLibrariesScreen(
    libraries: ImmutableList<LoadedLibrary>,
    summary: LibrarySummary,
    isLoading: Boolean,
    error: String?,
    selectedType: LibraryType?,
    showSystemLibs: Boolean,
    searchQuery: String,
    selectedLibrary: LoadedLibrary?,
    onTypeSelected: (LibraryType?) -> Unit,
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

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Extension, null, tint = colors.primary)
                        Text("Loaded Libraries", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onBack?.let { IconButton(onClick = it) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            SearchBar(searchQuery, onSearchQueryChanged, colors, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
            SummarySection(summary, colors, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            FilterSection(selectedType, showSystemLibs, onTypeSelected, onShowSystemLibsChanged, colors, Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            error?.let {
                Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp), RoundedCornerShape(8.dp), MaterialTheme.colorScheme.errorContainer) {
                    Text(it, Modifier.padding(12.dp), MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (libraries.isEmpty() && !isLoading) {
                EmptyState(colors, Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    items(libraries, key = { it.path }) { lib ->
                        LibraryCard(lib, { onLibrarySelected(lib) }, colors)
                    }
                }
            }
        }

        selectedLibrary?.let { lib ->
            ModalBottomSheet(onDismissRequest = onDismissDetail, sheetState = sheetState) {
                LibraryDetailContent(lib, colors, Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChanged: (String) -> Unit, colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors, modifier: Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = { Text("Search libraries...", color = colors.labelSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.labelSecondary) },
        trailingIcon = {
            AnimatedVisibility(query.isNotBlank(), enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = { onQueryChanged("") }) { Icon(Icons.Default.Close, "Clear", tint = colors.labelSecondary) }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.searchBackground,
            unfocusedContainerColor = colors.searchBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun SummarySection(summary: LibrarySummary, colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors, modifier: Modifier) {
    Row(modifier, Arrangement.spacedBy(8.dp)) {
        SummaryCard("Native", summary.nativeSoCount, colors.nativeSo, colors, Modifier.weight(1f))
        SummaryCard("DEX", summary.dexCount, colors.dex, colors, Modifier.weight(1f))
        SummaryCard("JAR", summary.jarCount, colors.jar, colors, Modifier.weight(1f))
        SummaryCard("Total", summary.totalLibraries, colors.primary, colors, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, count: Int, color: Color, colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors, modifier: Modifier) {
    Card(modifier, RoundedCornerShape(12.dp), CardDefaults.cardColors(colors.cardBackground)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.labelSecondary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSection(
    selectedType: LibraryType?,
    showSystemLibs: Boolean,
    onTypeSelected: (LibraryType?) -> Unit,
    onShowSystemLibsChanged: (Boolean) -> Unit,
    colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors,
    modifier: Modifier,
) {
    Column(modifier, Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selectedType == null, { onTypeSelected(null) }, { Text("All") })
            LibraryType.entries.filter { it != LibraryType.AAR_RESOURCE }.forEach { type ->
                val (icon, label, color) = when (type) {
                    LibraryType.NATIVE_SO -> Triple(Icons.Default.Memory, "Native", colors.nativeSo)
                    LibraryType.DEX -> Triple(Icons.Default.Android, "DEX", colors.dex)
                    LibraryType.JAR -> Triple(Icons.Default.Code, "JAR", colors.jar)
                    else -> Triple(Icons.Default.Extension, "Other", colors.primary)
                }
                FilterChip(
                    selectedType == type, { onTypeSelected(if (selectedType == type) null else type) },
                    { Text(label) },
                    leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(color.copy(0.2f), color, color),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Show system libraries", style = MaterialTheme.typography.bodyMedium, color = colors.labelPrimary)
            Switch(showSystemLibs, onShowSystemLibsChanged)
        }
    }
}

@Composable
private fun LibraryCard(library: LoadedLibrary, onClick: () -> Unit, colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors) {
    val (icon, color) = when (library.type) {
        LibraryType.NATIVE_SO -> Icons.Default.Memory to colors.nativeSo
        LibraryType.DEX -> Icons.Default.Android to colors.dex
        LibraryType.JAR -> Icons.Default.Code to colors.jar
        LibraryType.AAR_RESOURCE -> Icons.Default.Extension to colors.primary
    }

    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(12.dp), CardDefaults.cardColors(colors.cardBackground)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(0.15f)), Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(library.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = colors.labelPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(library.path, style = MaterialTheme.typography.bodySmall, color = colors.labelSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    library.size?.let { Text(formatSize(it), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = color) }
                    if (library.isSystemLibrary) Text("System", style = MaterialTheme.typography.labelSmall, color = colors.systemBadge)
                }
            }
            Surface(shape = RoundedCornerShape(4.dp), color = color.copy(0.15f)) {
                Text(library.type.name.take(3), Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun LibraryDetailContent(library: LoadedLibrary, colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors, modifier: Modifier) {
    val (icon, color) = when (library.type) {
        LibraryType.NATIVE_SO -> Icons.Default.Memory to colors.nativeSo
        LibraryType.DEX -> Icons.Default.Android to colors.dex
        LibraryType.JAR -> Icons.Default.Code to colors.jar
        LibraryType.AAR_RESOURCE -> Icons.Default.Extension to colors.primary
    }

    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.15f)), Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(library.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.labelPrimary)
                Text(library.type.name.replace("_", " "), style = MaterialTheme.typography.bodySmall, color = color)
            }
        }

        DetailSection("Details", listOfNotNull(
            "Path" to library.path,
            library.size?.let { "Size" to formatSize(it) },
            library.loadAddress?.let { "Load Address" to it },
            library.version?.let { "Version" to it },
            "Type" to if (library.isSystemLibrary) "System Library" else "App Library",
        ), colors)

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailSection(title: String, items: List<Pair<String, String>>, colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors) {
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = colors.labelSecondary)
        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(8.dp), colors.searchBackground) {
            Column(Modifier.padding(12.dp), Arrangement.spacedBy(8.dp)) {
                items.forEach { (label, value) ->
                    Column {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.labelSecondary)
                        Text(value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = colors.valuePrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(colors: com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme.LoadedLibrariesColors, modifier: Modifier) {
    Box(modifier, Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Extension, null, tint = colors.labelSecondary, modifier = Modifier.size(48.dp))
            Text("No libraries found", style = MaterialTheme.typography.bodyLarge, color = colors.labelSecondary)
        }
    }
}

private fun formatSize(bytes: Long) = when {
    bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
    bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
    else -> "$bytes B"
}
