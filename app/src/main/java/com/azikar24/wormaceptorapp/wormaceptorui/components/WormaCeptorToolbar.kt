/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.MyIconPack

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun Preview() {
    WormaCeptorToolbar(title = "WormaCeptor") {
        IconButton(onClick = {}) {
            Icon(imageVector = MyIconPack.IcGithub, contentDescription = "", tint = MaterialTheme.colors.onPrimary)
        }
    }
}

@Composable
fun WormaCeptorToolbar(title: String, menuActions: @Composable() RowScope.() -> Unit) {
    TopAppBar(
        title = { Text(text = title) },
        actions = menuActions,
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = 0.dp,
        modifier = Modifier
            .background(MaterialTheme.colors.primary)
            .padding(top = 30.dp)

    )
}