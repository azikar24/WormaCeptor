package com.azikar24.wormaceptor.feature.viewer.ui.components.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorToolTile
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.ToolItem

@Composable
internal fun FavoritesStrip(
    favorites: List<ToolItem>,
    onToolClick: (String) -> Unit,
    onToolLongClick: (ToolItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = WormaCeptorTokens.Colors.Category.favorites,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
            )
            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
            Text(
                text = stringResource(R.string.viewer_tools_quick_access),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = WormaCeptorTokens.Typography.overlineWide.letterSpacing,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        // Horizontal scrolling favorites
        LazyRow(
            contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            items(
                items = favorites,
                key = { tool -> "fav_${tool.feature}" },
            ) { tool ->
                WormaCeptorToolTile(
                    label = tool.name,
                    icon = tool.icon,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onToolClick(tool.route) },
                    onLongClick = { onToolLongClick(tool) },
                    modifier = Modifier.width(116.dp),
                )
            }
        }
    }
}
