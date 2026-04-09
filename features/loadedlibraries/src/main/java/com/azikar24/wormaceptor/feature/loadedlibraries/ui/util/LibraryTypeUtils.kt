package com.azikar24.wormaceptor.feature.loadedlibraries.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Memory
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R

internal fun LoadedLibrary.LibraryType.icon(): ImageVector = when (this) {
    LoadedLibrary.LibraryType.NATIVE_SO -> Icons.Default.Memory
    LoadedLibrary.LibraryType.DEX -> Icons.Default.Android
    LoadedLibrary.LibraryType.JAR -> Icons.Default.Code
    LoadedLibrary.LibraryType.AAR_RESOURCE -> Icons.Default.Extension
}

internal fun LoadedLibrary.LibraryType.color(colors: ToolColors.LoadedLibraries.Scheme): Color = when (this) {
    LoadedLibrary.LibraryType.NATIVE_SO -> colors.nativeSo
    LoadedLibrary.LibraryType.DEX -> colors.dex
    LoadedLibrary.LibraryType.JAR -> colors.jar
    LoadedLibrary.LibraryType.AAR_RESOURCE -> colors.aarResource
}

@Composable
internal fun LoadedLibrary.LibraryType.badgeLabel(): String = when (this) {
    LoadedLibrary.LibraryType.NATIVE_SO -> stringResource(R.string.loadedlibraries_badge_native)
    LoadedLibrary.LibraryType.DEX -> stringResource(R.string.loadedlibraries_badge_dex)
    LoadedLibrary.LibraryType.JAR -> stringResource(R.string.loadedlibraries_badge_jar)
    LoadedLibrary.LibraryType.AAR_RESOURCE -> stringResource(R.string.loadedlibraries_badge_aar)
}
