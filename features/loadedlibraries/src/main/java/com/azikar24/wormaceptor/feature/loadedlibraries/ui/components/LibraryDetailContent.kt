package com.azikar24.wormaceptor.feature.loadedlibraries.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.detail.DetailItem
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailHeader
import com.azikar24.wormaceptor.core.ui.components.detail.WormaCeptorDetailSection
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R

@Composable
internal fun LibraryDetailContent(
    library: LoadedLibrary,
    colors: ToolColors.LoadedLibraries.Scheme,
    modifier: Modifier = Modifier,
) {
    val (icon, color) = when (library.type) {
        LoadedLibrary.LibraryType.NATIVE_SO -> Icons.Default.Memory to colors.nativeSo
        LoadedLibrary.LibraryType.DEX -> Icons.Default.Android to colors.dex
        LoadedLibrary.LibraryType.JAR -> Icons.Default.Code to colors.jar
        LoadedLibrary.LibraryType.AAR_RESOURCE -> Icons.Default.Extension to colors.primary
    }

    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg)) {
        WormaCeptorDetailHeader(
            icon = icon,
            iconTint = color,
            iconBackgroundColor = color.copy(WormaCeptorTokens.Alpha.LIGHT),
            title = library.name,
            titleColor = colors.labelPrimary,
            subtitle = {
                Text(
                    library.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                )
            },
        )

        val detailTitle = stringResource(R.string.loadedlibraries_detail_title)
        val pathLabel = stringResource(R.string.loadedlibraries_detail_path)
        val sizeLabel = stringResource(R.string.loadedlibraries_detail_size)
        val loadAddressLabel = stringResource(R.string.loadedlibraries_detail_load_address)
        val versionLabel = stringResource(R.string.loadedlibraries_detail_version)
        val typeLabel = stringResource(R.string.loadedlibraries_detail_type)
        val systemLibraryType = stringResource(R.string.loadedlibraries_type_system)
        val appLibraryType = stringResource(R.string.loadedlibraries_type_app)

        WormaCeptorDetailSection(
            title = detailTitle,
            items = listOfNotNull(
                DetailItem(pathLabel, library.path),
                library.size?.let { DetailItem(sizeLabel, formatBytes(it)) },
                library.loadAddress?.let { DetailItem(loadAddressLabel, it) },
                library.version?.let { DetailItem(versionLabel, it) },
                DetailItem(typeLabel, if (library.isSystemLibrary) systemLibraryType else appLibraryType),
            ),
            labelColor = colors.labelSecondary,
            valueColor = colors.valuePrimary,
            surfaceColor = colors.searchBackground,
        )

        Spacer(Modifier.height(WormaCeptorTokens.Spacing.lg))
    }
}
