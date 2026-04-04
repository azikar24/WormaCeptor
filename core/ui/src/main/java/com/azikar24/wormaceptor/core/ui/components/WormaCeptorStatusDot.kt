package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Simple colored dot indicator for inline status display.
 *
 * @param color Dot fill color
 * @param modifier Modifier for the root composable
 * @param size Dot diameter (defaults to [WormaCeptorTokens.Spacing.sm])
 */
@Composable
fun WormaCeptorStatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = WormaCeptorTokens.Spacing.sm,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

// region Previews

@Preview(name = "StatusDot - Light")
@Composable
private fun StatusDotLightPreview() {
    WormaCeptorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.green)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.amber)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.orange)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.red)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.blue)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.grey)
            }
        }
    }
}

@Preview(name = "StatusDot - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusDotDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.green)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.amber)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.orange)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.red)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.blue)
                WormaCeptorStatusDot(color = WormaCeptorTokens.Colors.Status.grey)
            }
        }
    }
}

// endregion
