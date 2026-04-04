package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

private const val MonospaceLengthThreshold = 20

/**
 * Label-value pair row with optional monospace and selection support.
 * Used in device info screens, preference details, and other detail views.
 *
 * @param label Descriptive label text
 * @param value The value to display
 * @param modifier Modifier for the root composable
 * @param labelWeight Weight fraction for the label column (0.0-1.0)
 * @param useMonospace Whether to force monospace font. When null, auto-detects based on value length.
 * @param selectable Whether the value text is selectable
 */
@Composable
fun WormaCeptorDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelWeight: Float = 0.4f,
    useMonospace: Boolean? = null,
    selectable: Boolean = true,
) {
    val fontFamily = when (useMonospace) {
        true -> FontFamily.Monospace
        false -> FontFamily.Default
        null -> if (value.length > MonospaceLengthThreshold) FontFamily.Monospace else FontFamily.Default
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorTokens.Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(labelWeight),
        )
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
        val valueModifier = Modifier.weight(1f - labelWeight)
        if (selectable) {
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = valueModifier,
                    fontFamily = fontFamily,
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = valueModifier,
                fontFamily = fontFamily,
            )
        }
    }
}

@Preview(name = "DetailRow - Light")
@Composable
private fun WormaCeptorDetailRowPreview() {
    WormaCeptorTheme {
        Surface {
            Column(modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg)) {
                WormaCeptorDetailRow(label = "Status", value = "200 OK")
                WormaCeptorDetailRow(label = "Method", value = "GET")
                WormaCeptorDetailRow(
                    label = "URL",
                    value = "https://api.example.com/v1/transactions",
                )
            }
        }
    }
}

@Preview(name = "DetailRow - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WormaCeptorDetailRowDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg)) {
                WormaCeptorDetailRow(label = "Status", value = "200 OK")
                WormaCeptorDetailRow(label = "Method", value = "GET")
                WormaCeptorDetailRow(
                    label = "URL",
                    value = "https://api.example.com/v1/transactions",
                )
            }
        }
    }
}
