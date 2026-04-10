package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
fun WormaCeptorDetailSection(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                items.forEach { (label, value) ->
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = WormaCeptorTokens.Alpha.PROMINENT,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "DetailSection - Light")
@Composable
private fun WormaCeptorDetailSectionPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorDetailSection(
                title = "Details",
                items = listOf(
                    "Class" to "com.example.app.MainActivity",
                    "Description" to "Activity retained after onDestroy",
                    "Size" to "2.0 MB",
                ),
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )
        }
    }
}

@Preview(name = "DetailSection - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WormaCeptorDetailSectionDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorDetailSection(
                title = "Details",
                items = listOf(
                    "Class" to "com.example.app.MainActivity",
                    "Description" to "Activity retained after onDestroy",
                    "Size" to "2.0 MB",
                ),
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )
        }
    }
}
