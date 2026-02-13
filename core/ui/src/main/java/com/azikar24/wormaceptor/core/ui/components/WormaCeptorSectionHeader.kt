package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Icon + title row with heading semantics for section headers.
 * Used in metrics cards, filter sheets, and detail screens.
 *
 * @param title Section title text
 * @param icon Leading icon for the section
 * @param modifier Modifier for the root composable
 * @param iconTint Tint color for the icon
 * @param trailingContent Optional trailing composable (e.g., action button)
 */
@Composable
fun WormaCeptorSectionHeader(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            tint = iconTint,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailingContent?.invoke()
    }
}
