package com.azikar24.wormaceptor.core.ui.components.divider

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Unified divider component for WormaCeptor.
 *
 * Replaces raw [HorizontalDivider]/[VerticalDivider] calls with consistent, semantic variants.
 *
 * @param modifier Modifier for the divider
 * @param style Divider style variant
 * @param orientation Layout orientation; use [DividerOrientation.Vertical] for in-row separators
 */
@Composable
fun WormaCeptorDivider(
    modifier: Modifier = Modifier,
    style: DividerStyle = DividerStyle.Standard,
    orientation: DividerOrientation = DividerOrientation.Horizontal,
) {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val color: Color = when (style) {
        DividerStyle.Standard, DividerStyle.Thick -> outlineVariant
        DividerStyle.Subtle -> outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
        DividerStyle.Section -> outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD)
    }
    val thickness: Dp = if (style == DividerStyle.Thick) {
        WormaCeptorTokens.BorderWidth.thick
    } else {
        1.dp
    }
    when (orientation) {
        DividerOrientation.Horizontal -> HorizontalDivider(
            modifier = modifier,
            thickness = thickness,
            color = color,
        )
        DividerOrientation.Vertical -> VerticalDivider(
            modifier = modifier,
            thickness = thickness,
            color = color,
        )
    }
}

// region Previews

@Preview(name = "Divider - Light")
@Composable
private fun DividerLightPreview() {
    WormaCeptorTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Request Headers")
                WormaCeptorDivider(style = DividerStyle.Standard)
                Text("Response Headers")
                WormaCeptorDivider(style = DividerStyle.Subtle)
                Text("Body")
                WormaCeptorDivider(style = DividerStyle.Section)
                Text("Metadata")
                WormaCeptorDivider(style = DividerStyle.Thick)
                Text("Footer")
            }
        }
    }
}

@Preview(name = "Divider - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DividerDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Request Headers")
                WormaCeptorDivider(style = DividerStyle.Standard)
                Text("Response Headers")
                WormaCeptorDivider(style = DividerStyle.Subtle)
                Text("Body")
                WormaCeptorDivider(style = DividerStyle.Section)
                Text("Metadata")
                WormaCeptorDivider(style = DividerStyle.Thick)
                Text("Footer")
            }
        }
    }
}

// endregion
