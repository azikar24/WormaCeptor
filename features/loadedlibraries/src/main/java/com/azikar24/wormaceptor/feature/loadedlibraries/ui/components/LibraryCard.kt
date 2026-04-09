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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.util.badgeLabel
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.util.color
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.util.icon

@Composable
internal fun LibraryCard(
    library: LoadedLibrary,
    onClick: () -> Unit,
    colors: ToolColors.LoadedLibraries.Scheme,
) {
    val icon = library.type.icon()
    val color = library.type.color(colors)

    WormaCeptorCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.cardBackground,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LibraryTypeIcon(icon, color)
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.md))
            LibraryDetails(library, colors, color, Modifier.weight(1f))
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.sm))
            TypeBadge(library.type, color)
        }
    }
}

@Composable
private fun LibraryTypeIcon(
    icon: ImageVector,
    color: Color,
) {
    Box(
        Modifier
            .size(WormaCeptorTokens.IconSize.xxl)
            .clip(WormaCeptorTokens.Shapes.card)
            .background(color.copy(WormaCeptorTokens.Alpha.LIGHT)),
        Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
        )
    }
}

@Composable
private fun LibraryDetails(
    library: LoadedLibrary,
    colors: ToolColors.LoadedLibraries.Scheme,
    typeColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
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
                    color = typeColor,
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
}

@Composable
private fun TypeBadge(
    type: LoadedLibrary.LibraryType,
    color: Color,
) {
    Surface(
        shape = WormaCeptorTokens.Shapes.chip,
        color = color.copy(WormaCeptorTokens.Alpha.LIGHT),
    ) {
        Text(
            type.badgeLabel(),
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

private class LibraryPreviewProvider : PreviewParameterProvider<LoadedLibrary> {
    override val values: Sequence<LoadedLibrary> = sequenceOf(
        LoadedLibrary(
            name = "libc.so",
            path = "/system/lib64/libc.so",
            type = LoadedLibrary.LibraryType.NATIVE_SO,
            size = 1_245_184L,
            loadAddress = "0x7f8a3c0000",
            version = null,
            isSystemLibrary = true,
        ),
        LoadedLibrary(
            name = "classes.dex",
            path = "/data/app/com.example/base.apk!classes.dex",
            type = LoadedLibrary.LibraryType.DEX,
            size = 8_388_608L,
            loadAddress = null,
            version = null,
            isSystemLibrary = false,
        ),
        LoadedLibrary(
            name = "kotlin-stdlib.jar",
            path = "/data/app/com.example/kotlin-stdlib.jar",
            type = LoadedLibrary.LibraryType.JAR,
            size = 3_145_728L,
            loadAddress = null,
            version = "1.9.22",
            isSystemLibrary = false,
        ),
        LoadedLibrary(
            name = "appcompat-resources",
            path = "/data/app/com.example/res",
            type = LoadedLibrary.LibraryType.AAR_RESOURCE,
            size = null,
            loadAddress = null,
            version = "1.6.1",
            isSystemLibrary = false,
        ),
    )
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun LibraryCardPreview(@PreviewParameter(LibraryPreviewProvider::class) library: LoadedLibrary) {
    WormaCeptorTheme {
        LibraryCard(
            library = library,
            onClick = {},
            colors = ToolColors.LoadedLibraries.scheme(),
        )
    }
}
