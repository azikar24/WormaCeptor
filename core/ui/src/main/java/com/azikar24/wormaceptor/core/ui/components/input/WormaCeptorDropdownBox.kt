package com.azikar24.wormaceptor.core.ui.components.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme

/**
 * Anchored dropdown: [anchor] (typically a read-only [WormaCeptorTextField]) opens [menu] on tap.
 *
 * Use instead of Material3 `ExposedDropdownMenuBox`. Its JVM facade class moved in Material3 1.4.0,
 * so a call compiled against 1.3.x throws `NoSuchMethodError` in host apps on 1.4.0+.
 * [DropdownMenu] is binary-stable across both.
 *
 * @param expanded Whether the menu is showing
 * @param onExpandedChange Called with the requested expanded state
 * @param anchor Field the menu is attached to; fills the container width
 * @param modifier Modifier for the container
 * @param enabled When false, taps on the anchor are ignored
 * @param menu Menu items, usually [DropdownMenuItem]s
 */
@Composable
fun WormaCeptorDropdownBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    anchor: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menu: @Composable ColumnScope.() -> Unit,
) {
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val anchorWidth = with(LocalDensity.current) { anchorWidthPx.toDp() }

    Box(modifier = modifier.onSizeChanged { anchorWidthPx = it.width }) {
        anchor()
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    role = Role.DropdownList,
                    onClick = { onExpandedChange(!expanded) },
                ),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.width(anchorWidth),
            content = menu,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun WormaCeptorDropdownBoxPreview() {
    WormaCeptorTheme {
        WormaCeptorDropdownBox(
            expanded = false,
            onExpandedChange = {},
            anchor = {
                WormaCeptorTextField(
                    value = "GET",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Method") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            menu = {
                DropdownMenuItem(text = { Text("GET") }, onClick = {})
            },
        )
    }
}
