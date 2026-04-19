package com.azikar24.wormaceptor.feature.loadedlibraries.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.components.toggle.WormaCeptorSwitch
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R

@Composable
internal fun FilterSection(
    selectedType: LoadedLibrary.LibraryType?,
    showSystemLibs: Boolean,
    onSelectType: (LoadedLibrary.LibraryType?) -> Unit,
    onToggleSystemLibs: (Boolean) -> Unit,
    colors: ToolColors.LoadedLibraries.Scheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        TypeChipRow(selectedType, onSelectType, colors)
        SystemLibsToggle(showSystemLibs, onToggleSystemLibs, colors)
    }
}

@Composable
private fun TypeChipRow(
    selectedType: LoadedLibrary.LibraryType?,
    onSelectType: (LoadedLibrary.LibraryType?) -> Unit,
    colors: ToolColors.LoadedLibraries.Scheme,
) {
    val haptic = LocalHapticFeedback.current
    WormaCeptorScrollableRow(contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg)) {
        WormaCeptorChip(
            label = stringResource(R.string.loadedlibraries_filter_all),
            selected = selectedType == null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectType(null)
            },
            accentColor = colors.primary,
        )
        LoadedLibrary.LibraryType.entries.filter { it != LoadedLibrary.LibraryType.AAR_RESOURCE }.forEach { type ->
            val (icon, labelRes, color) = when (type) {
                LoadedLibrary.LibraryType.NATIVE_SO -> ChipConfig(
                    icon = Icons.Default.Memory,
                    labelRes = R.string.loadedlibraries_filter_native,
                    color = colors.nativeSo,
                )

                LoadedLibrary.LibraryType.DEX -> ChipConfig(
                    icon = Icons.Default.Android,
                    labelRes = R.string.loadedlibraries_filter_dex,
                    color = colors.dex,
                )

                LoadedLibrary.LibraryType.JAR -> ChipConfig(
                    icon = Icons.Default.Code,
                    labelRes = R.string.loadedlibraries_filter_jar,
                    color = colors.jar,
                )

                LoadedLibrary.LibraryType.AAR_RESOURCE -> error("AAR_RESOURCE is filtered out")
            }
            WormaCeptorChip(
                label = stringResource(labelRes),
                selected = selectedType == type,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelectType(if (selectedType == type) null else type)
                },
                leadingIcon = icon,
                accentColor = color,
            )
        }
    }
}

@Composable
private fun SystemLibsToggle(
    showSystemLibs: Boolean,
    onToggleSystemLibs: (Boolean) -> Unit,
    colors: ToolColors.LoadedLibraries.Scheme,
) {
    val haptic = LocalHapticFeedback.current
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(
            stringResource(R.string.loadedlibraries_show_system),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.labelPrimary,
        )
        WormaCeptorSwitch(
            checked = showSystemLibs,
            onCheckedChange = { newValue ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleSystemLibs(newValue)
            },
        )
    }
}
