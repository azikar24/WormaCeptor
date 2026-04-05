package com.azikar24.wormaceptor.feature.loadedlibraries.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R

@Composable
internal fun LibraryCard(
    library: LoadedLibrary,
    onClick: () -> Unit,
    colors: ToolColors.LoadedLibraries.Scheme,
) {
    val (icon, color) = when (library.type) {
        LoadedLibrary.LibraryType.NATIVE_SO -> Icons.Default.Memory to colors.nativeSo
        LoadedLibrary.LibraryType.DEX -> Icons.Default.Android to colors.dex
        LoadedLibrary.LibraryType.JAR -> Icons.Default.Code to colors.jar
        LoadedLibrary.LibraryType.AAR_RESOURCE -> Icons.Default.Extension to colors.primary
    }

    WormaCeptorCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.cardBackground,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(
                    WormaCeptorTokens.IconSize.xxl,
                ).clip(WormaCeptorTokens.Shapes.card).background(color.copy(WormaCeptorTokens.Alpha.LIGHT)),
                Alignment.Center,
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(WormaCeptorTokens.IconSize.md))
            }
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.md))
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
                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
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
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.sm))
            Surface(
                shape = WormaCeptorTokens.Shapes.chip,
                color = color.copy(WormaCeptorTokens.Alpha.LIGHT),
            ) {
                Text(
                    library.type.name.take(3),
                    Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.sm,
                        vertical = WormaCeptorTokens.Spacing.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
    }
}
