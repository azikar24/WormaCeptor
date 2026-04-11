package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
fun WormaCeptorStatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.chip,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

// region Previews

@Preview(name = "StatusBadge")
@Composable
private fun StatusBadgePreview() {
    WormaCeptorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorStatusBadge(
                    text = "C",
                    containerColor = Color(0xFFFFCDD2),
                    contentColor = Color(0xFFD32F2F),
                )
                WormaCeptorStatusBadge(
                    text = "DISK",
                    containerColor = Color(0xFFE1BEE7),
                    contentColor = Color(0xFF7B1FA2),
                )
                WormaCeptorStatusBadge(
                    text = "SO",
                    containerColor = Color(0xFFBBDEFB),
                    contentColor = Color(0xFF1976D2),
                )
            }
        }
    }
}

// endregion
