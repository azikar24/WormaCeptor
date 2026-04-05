package com.azikar24.wormaceptor.feature.loadedlibraries.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterSection(
    selectedType: LoadedLibrary.LibraryType?,
    showSystemLibs: Boolean,
    onTypeSelected: (LoadedLibrary.LibraryType?) -> Unit,
    onShowSystemLibsChanged: (Boolean) -> Unit,
    colors: ToolColors.LoadedLibraries.Scheme,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier, Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        WormaCeptorFlowRow(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text(stringResource(R.string.loadedlibraries_filter_all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primary.copy(WormaCeptorTokens.Alpha.MEDIUM),
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
                    leadingIcon = { Icon(icon, null, Modifier.size(WormaCeptorTokens.IconSize.sm)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(WormaCeptorTokens.Alpha.MEDIUM),
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
            Switch(
                checked = showSystemLibs,
                onCheckedChange = { newValue ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onShowSystemLibsChanged(newValue)
                },
            )
        }
    }
}
