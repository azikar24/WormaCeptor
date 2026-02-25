package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/** Unified tile for tool items in the tools grid and favorites strip. */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("LongParameterList", "LongMethod")
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
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .height(116.dp)
            .clip(WormaCeptorDesignSystem.Shapes.cardLarge)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick?.let {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.strong,
            ),
        ),
        shape = WormaCeptorDesignSystem.Shapes.cardLarge,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
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
                        .padding(WormaCeptorDesignSystem.Spacing.sm)
                        .size(WormaCeptorDesignSystem.IconSize.xxs),
                    tint = WormaCeptorColors.Category.Favorites,
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
            .padding(WormaCeptorDesignSystem.Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
                    shape = WormaCeptorDesignSystem.Shapes.card,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp),
                tint = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 14.sp,
        )
    }
}
