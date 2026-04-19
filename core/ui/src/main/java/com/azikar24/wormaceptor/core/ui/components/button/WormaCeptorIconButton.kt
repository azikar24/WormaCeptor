package com.azikar24.wormaceptor.core.ui.components.button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme

@Suppress("LongParameterList")
@Composable
fun WormaCeptorIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color? = null,
    iconSize: Dp? = null,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier,
            tint = tint ?: LocalContentColor.current,
        )
    }
}

@Composable
fun WormaCeptorIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content,
    )
}

@Preview(name = "IconButton - Light")
@Composable
private fun WormaCeptorIconButtonPreview() {
    WormaCeptorTheme {
        WormaCeptorIconButton(
            onClick = {},
            icon = Icons.Default.Refresh,
            contentDescription = "Refresh",
        )
    }
}
