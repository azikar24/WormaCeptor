package com.azikar24.wormaceptor.feature.viewer.ui.components.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorToolTile
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.ToolItem

private const val GridColumns = 3

@Suppress("LongMethod")
@Composable
internal fun ToolCategorySection(
    categoryName: String,
    categoryColor: Color,
    categoryIcon: ImageVector,
    tools: List<ToolItem>,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onToolClick: (String) -> Unit,
    onToolLongClick: (ToolItem) -> Unit,
    favorites: Set<Feature>,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
        label = "collapse_rotation",
    )

    val headerBackground by animateColorAsState(
        targetValue = if (isCollapsed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            categoryColor.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        },
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "header_bg",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
    ) {
        Surface(
            onClick = onToggleCollapse,
            modifier = Modifier
                .fillMaxWidth()
                .clip(WormaCeptorTokens.Shapes.card),
            color = headerBackground,
            shape = WormaCeptorTokens.Shapes.card,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WormaCeptorTokens.Spacing.md,
                        vertical = WormaCeptorTokens.Spacing.sm,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                    Surface(
                        shape = WormaCeptorTokens.Shapes.badge,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = "${tools.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.xs,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) {
                        stringResource(
                            R.string.viewer_body_expand,
                        )
                    } else {
                        stringResource(R.string.viewer_body_collapse)
                    },
                    modifier = Modifier
                        .size(WormaCeptorTokens.IconSize.md)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        AnimatedVisibility(
            visible = !isCollapsed,
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            Column {
                if (headerContent != null) {
                    headerContent()
                }

                val spacing = WormaCeptorTokens.Spacing.sm
                val columns = GridColumns

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WormaCeptorTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    tools.chunked(columns).forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                        ) {
                            rowTools.forEach { tool ->
                                WormaCeptorToolTile(
                                    label = tool.name,
                                    icon = tool.icon,
                                    accentColor = categoryColor,
                                    onClick = { onToolClick(tool.route) },
                                    onLongClick = { onToolLongClick(tool) },
                                    isFavorite = tool.feature in favorites,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(columns - rowTools.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
