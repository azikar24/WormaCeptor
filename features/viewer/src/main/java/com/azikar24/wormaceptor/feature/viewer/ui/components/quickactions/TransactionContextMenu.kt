/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.quickactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem

/**
 * Context menu for transaction items with copy, share, replay, and delete actions.
 *
 * Design: Clean dropdown with subtle elevation and rounded corners.
 * Destructive actions (delete) are visually distinguished with red color.
 */
@Composable
fun TransactionContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCopyUrl: () -> Unit,
    onShare: () -> Unit,
    onCopyAsCurl: () -> Unit,
    onReplay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier
            .widthIn(min = 200.dp, max = 280.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg)
            ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        offset = DpOffset(x = 0.dp, y = WormaCeptorDesignSystem.Spacing.xs),
        properties = PopupProperties(focusable = true)
    ) {
        // Copy URL
        ContextMenuItem(
            icon = Icons.Outlined.ContentCopy,
            label = "Copy URL",
            onClick = {
                onCopyUrl()
                onDismiss()
            }
        )

        // Share
        ContextMenuItem(
            icon = Icons.Outlined.Share,
            label = "Share",
            onClick = {
                onShare()
                onDismiss()
            }
        )

        // Copy as cURL
        ContextMenuItem(
            icon = Icons.Outlined.Code,
            label = "Copy as cURL",
            onClick = {
                onCopyAsCurl()
                onDismiss()
            }
        )

        // Replay Request
        ContextMenuItem(
            icon = Icons.Outlined.Replay,
            label = "Replay Request",
            onClick = {
                onReplay()
                onDismiss()
            }
        )

        // Divider before destructive action
        HorizontalDivider(
            modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
            thickness = WormaCeptorDesignSystem.BorderWidth.thin,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        // Delete - Destructive action
        ContextMenuItem(
            icon = Icons.Outlined.Delete,
            label = "Delete",
            destructive = true,
            onClick = {
                onDelete()
                onDismiss()
            }
        )
    }
}

/**
 * Individual menu item with consistent styling.
 * Supports normal and destructive (red) variants.
 */
@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    enabled: Boolean = true
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val iconColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    DropdownMenuItem(
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (destructive) FontWeight.Medium else FontWeight.Normal,
                color = contentColor
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
        },
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.xs),
        colors = MenuDefaults.itemColors(
            textColor = contentColor,
            leadingIconColor = iconColor
        )
    )
}

/**
 * Alternative custom context menu with more visual styling options.
 * Use this for a more polished, custom appearance.
 */
@Composable
fun CustomContextMenu(
    visible: Boolean,
    actions: List<QuickAction>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = tween(150)
        ),
        exit = fadeOut(animationSpec = tween(100)) + scaleOut(
            targetScale = 0.92f,
            animationSpec = tween(100)
        )
    ) {
        Surface(
            modifier = modifier
                .widthIn(min = 180.dp, max = 260.dp),
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = WormaCeptorDesignSystem.Elevation.md,
            shadowElevation = WormaCeptorDesignSystem.Elevation.lg
        ) {
            Column(
                modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.sm)
            ) {
                val (normalActions, destructiveActions) = actions.partition { !it.destructive }

                // Normal actions
                normalActions.forEach { action ->
                    CustomContextMenuItem(
                        action = action,
                        onDismiss = onDismiss
                    )
                }

                // Divider if there are destructive actions
                if (destructiveActions.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = WormaCeptorDesignSystem.Spacing.sm,
                            horizontal = WormaCeptorDesignSystem.Spacing.md
                        ),
                        thickness = WormaCeptorDesignSystem.BorderWidth.thin,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )

                    // Destructive actions
                    destructiveActions.forEach { action ->
                        CustomContextMenuItem(
                            action = action,
                            onDismiss = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomContextMenuItem(
    action: QuickAction,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.02f else 1f,
        animationSpec = tween(100),
        label = "menuItemScale"
    )

    val backgroundColor = when {
        !action.enabled -> Color.Transparent
        isHovered && action.destructive -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        isHovered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val contentColor = when {
        !action.enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        action.destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    val iconColor = when {
        !action.enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        action.destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = action.enabled,
                onClick = {
                    action.onClick()
                    onDismiss()
                }
            )
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.md
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = iconColor
        )

        Text(
            text = action.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (action.destructive) FontWeight.Medium else FontWeight.Normal,
            color = contentColor
        )
    }
}
