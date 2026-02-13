package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Card with header (icon + title + optional action button) and content slot.
 * Used in device info and other detail screens.
 *
 * @param title Card header title
 * @param icon Header leading icon
 * @param iconTint Tint color for the header icon
 * @param modifier Modifier for the root composable
 * @param onAction Optional action callback (e.g., copy)
 * @param actionIcon Icon for the action button
 * @param actionContentDescription Content description for the action button
 * @param content Card body content
 */
@Composable
fun WormaCeptorInfoCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
    actionIcon: ImageVector = Icons.Default.ContentCopy,
    actionContentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm),
        ),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                if (onAction != null) {
                    IconButton(onClick = onAction) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionContentDescription,
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            content()
        }
    }
}
