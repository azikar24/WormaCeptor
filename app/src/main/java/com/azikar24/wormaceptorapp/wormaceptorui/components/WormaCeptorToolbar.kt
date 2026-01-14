/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.MyIconPack

@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun Preview() {
    WormaCeptorToolbar(title = "WormaCeptor") {
        AnimatedIconButton(
            icon = MyIconPack.IcGithub,
            contentDescription = "View on GitHub",
            onClick = {}
        )
    }
}

/**
 * Enhanced IconButton with subtle press animation
 */
@Composable
fun AnimatedIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = WormaCeptorDesignSystem.AnimationSpecs.normalSpring,
        label = "iconButtonScale"
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier.scale(scale)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WormaCeptorToolbar(title: String, menuActions: @Composable RowScope.() -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = menuActions,
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
    )
}