package com.azikar24.wormaceptor.core.ui.components.toggle

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Unified switch component for WormaCeptor.
 *
 * Wraps [Switch] with a [SwitchVariant] that supports either Material3
 * defaults or a feature-scoped accent color. Pass `onCheckedChange = null`
 * for decorative switches whose parent row handles the toggle.
 */
@Composable
fun WormaCeptorSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    variant: SwitchVariant = SwitchVariant.Standard,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = variant.toColors(),
    )
}

@Composable
private fun SwitchVariant.toColors(): SwitchColors = when (this) {
    SwitchVariant.Standard -> SwitchDefaults.colors()
    is SwitchVariant.Accent -> SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = color,
    )
}

@Preview(name = "Switch - Light")
@Composable
private fun WormaCeptorSwitchPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorSwitch(checked = true, onCheckedChange = {})
                WormaCeptorSwitch(checked = false, onCheckedChange = {})
                WormaCeptorSwitch(
                    checked = true,
                    onCheckedChange = {},
                    variant = SwitchVariant.Accent(
                        color = WormaCeptorTokens.Colors.Location.enabled,
                    ),
                )
                WormaCeptorSwitch(
                    checked = true,
                    onCheckedChange = null,
                    enabled = false,
                )
            }
        }
    }
}

@Preview(name = "Switch - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WormaCeptorSwitchDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorSwitch(checked = true, onCheckedChange = {})
                WormaCeptorSwitch(
                    checked = true,
                    onCheckedChange = {},
                    variant = SwitchVariant.Accent(
                        color = WormaCeptorTokens.Colors.Location.enabled,
                    ),
                )
            }
        }
    }
}
