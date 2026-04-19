package com.azikar24.wormaceptor.core.ui.components.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled

@Composable
fun WormaCeptorDetailHeader(
    icon: ImageVector,
    iconTint: Color,
    iconBackgroundColor: Color,
    title: String,
    modifier: Modifier = Modifier,
    iconContentDescription: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitle: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md.scaled()),
    ) {
        Box(
            modifier = Modifier
                .size(WormaCeptorTokens.Spacing.xxxl.scaled())
                .clip(WormaCeptorTokens.Shapes.cardLarge)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = iconTint,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            subtitle?.invoke()
        }
    }
}

// region Previews

@Preview(name = "DetailHeader - Light")
@Composable
private fun DetailHeaderLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorDetailHeader(
                icon = Icons.Default.Memory,
                iconTint = MaterialTheme.colorScheme.error,
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                title = "MainActivity",
                subtitle = {
                    Text(
                        text = "CRITICAL",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            )
        }
    }
}

@Preview(name = "DetailHeader - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DetailHeaderDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorDetailHeader(
                icon = Icons.Default.Memory,
                iconTint = MaterialTheme.colorScheme.error,
                iconBackgroundColor = MaterialTheme.colorScheme.errorContainer,
                title = "MainActivity",
            )
        }
    }
}

@Preview(name = "DetailHeader - No Subtitle")
@Composable
private fun DetailHeaderNoSubtitlePreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorDetailHeader(
                icon = Icons.Default.Memory,
                iconTint = MaterialTheme.colorScheme.primary,
                iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                title = "SomeComponent",
            )
        }
    }
}

// endregion
