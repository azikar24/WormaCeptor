package com.azikar24.wormaceptor.feature.viewer.ui.components.filter

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun GridFilterCard(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sublabel: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.ULTRA_FAST),
        label = "scale_animation",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            color.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.MEDIUM),
        label = "bg_animation",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.MEDIUM),
        label = "border_animation",
    )

    val context = LocalContext.current
    val stateDesc = when {
        count == 0 -> context.getString(R.string.viewer_filter_no_items)
        isSelected -> context.getString(R.string.viewer_filter_selected, count)
        else -> context.getString(R.string.viewer_filter_items_count, count)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(WormaCeptorTokens.Shapes.card)
            .background(backgroundColor)
            .border(
                width = if (isSelected) {
                    WormaCeptorTokens.BorderWidth.regular
                } else {
                    WormaCeptorTokens.BorderWidth.thin
                },
                color = borderColor,
                shape = WormaCeptorTokens.Shapes.card,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                enabled = count > 0,
            )
            .semantics {
                role = Role.Checkbox
                selected = isSelected
                stateDescription = stateDesc
                val actionText = if (count > 0) {
                    context.getString(
                        if (isSelected) {
                            R.string.viewer_filter_action_deselect
                        } else {
                            R.string.viewer_filter_action_select
                        },
                    )
                } else {
                    context.getString(R.string.viewer_filter_disabled)
                }
                contentDescription = "$label filter. $stateDesc. $actionText"
            }
            .padding(WormaCeptorTokens.Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (count > 0) color else color.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
                        ),
                )

                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (count > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = WormaCeptorTokens.Alpha.STRONG)
                        },
                    )
                    if (sublabel != null) {
                        Text(
                            text = sublabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (count > 0) {
                                    WormaCeptorTokens.Alpha.INTENSE
                                } else {
                                    WormaCeptorTokens.Alpha.MODERATE
                                },
                            ),
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (count > 0) color else color.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(WormaCeptorTokens.IconSize.sm)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.viewer_filter_selected_indicator),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
            }
        }
    }
}
