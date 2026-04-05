package com.azikar24.wormaceptor.feature.loadedlibraries.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Box(
                Modifier.size(
                    WormaCeptorTokens.Spacing.xxxl,
                ).clip(WormaCeptorTokens.Shapes.card).background(color.copy(WormaCeptorTokens.Alpha.LIGHT)),
                Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(WormaCeptorTokens.Spacing.xl))
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

        Spacer(Modifier.height(WormaCeptorTokens.Spacing.lg))
    }
}

@Composable
internal fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: ToolColors.LoadedLibraries.Scheme,
) {
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        Text(
            title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(Modifier.fillMaxWidth(), WormaCeptorTokens.Shapes.card, colors.searchBackground) {
            Column(
                Modifier.padding(WormaCeptorTokens.Spacing.md),
                Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
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
