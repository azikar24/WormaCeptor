package com.azikar24.wormaceptor.core.ui.components.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled

@Composable
fun WormaCeptorDetailSection(
    title: String,
    items: List<DetailItem>,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface.copy(
        alpha = WormaCeptorTokens.Alpha.PROMINENT,
    ),
    surfaceColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm.scaled()),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
            color = surfaceColor,
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md.scaled()),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm.scaled()),
            ) {
                items.forEach { item ->
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = labelColor,
                        )
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = valueColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WormaCeptorDetailSection(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface.copy(
        alpha = WormaCeptorTokens.Alpha.PROMINENT,
    ),
    surfaceColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm.scaled()),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
            color = surfaceColor,
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md.scaled()),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = valueColor,
            )
        }
    }
}

@Preview
@Composable
private fun WormaCeptorDetailSectionPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorDetailSection(
                title = "Details",
                items = listOf(
                    DetailItem("Class", "com.example.app.MainActivity"),
                    DetailItem("Description", "Activity retained after onDestroy"),
                    DetailItem("Size", "2.0 MB"),
                ),
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )
        }
    }
}

@Preview
@Composable
private fun WormaCeptorDetailSectionDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorDetailSection(
                title = "Details",
                value = "test",
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )
        }
    }
}
