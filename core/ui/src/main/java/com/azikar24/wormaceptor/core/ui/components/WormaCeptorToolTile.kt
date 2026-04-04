package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Unified tile for tool items in the tools grid and favorites strip. */
@Suppress("LongParameterList")
@Composable
fun WormaCeptorToolTile(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
) {
    WormaCeptorCard(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.height(WormaCeptorTokens.ComponentSize.toolTileHeight),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorTokens.Alpha.STRONG,
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TileContent(label = label, icon = icon, accentColor = accentColor)

            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(WormaCeptorTokens.Spacing.sm)
                        .size(WormaCeptorTokens.IconSize.xxs),
                    tint = WormaCeptorTokens.Colors.Category.favorites,
                )
            }
        }
    }
}

@Composable
private fun TileContent(
    label: String,
    icon: ImageVector,
    accentColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WormaCeptorTokens.Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(WormaCeptorTokens.IconSize.xxl)
                .background(
                    color = accentColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                    shape = WormaCeptorTokens.Shapes.card,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                tint = accentColor.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(name = "ToolTile")
@Composable
private fun ToolTilePreview() {
    WormaCeptorTheme {
        WormaCeptorToolTile(
            label = "Network",
            icon = Icons.Default.Star,
            accentColor = WormaCeptorTokens.Colors.Status.blue,
            onClick = {},
        )
    }
}

@Preview(name = "ToolTile - Favorite")
@Composable
private fun ToolTileFavoritePreview() {
    WormaCeptorTheme {
        WormaCeptorToolTile(
            label = "Favorites Tool",
            icon = Icons.Default.Star,
            accentColor = WormaCeptorTokens.Colors.Status.amber,
            onClick = {},
            isFavorite = true,
        )
    }
}
