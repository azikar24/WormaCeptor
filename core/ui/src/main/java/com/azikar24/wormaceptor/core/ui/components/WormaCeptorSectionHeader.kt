package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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
    iconTint: Color = MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
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

// region Previews

@Preview(name = "SectionHeader - Light")
@Composable
private fun SectionHeaderLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorSectionHeader(
                title = "Request Details",
                icon = Icons.Default.Info,
            )
        }
    }
}

@Preview(name = "SectionHeader - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionHeaderDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorSectionHeader(
                title = "Request Details",
                icon = Icons.Default.Info,
            )
        }
    }
}

@Preview(name = "SectionHeader With Action - Light")
@Composable
private fun SectionHeaderWithActionLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorSectionHeader(
                title = "Network Logs",
                icon = Icons.Default.Settings,
                trailingContent = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        }
    }
}

@Preview(name = "SectionHeader With Action - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionHeaderWithActionDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorSectionHeader(
                title = "Network Logs",
                icon = Icons.Default.Settings,
                trailingContent = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
            )
        }
    }
}

// endregion
