package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

@Composable
fun WormaCeptorFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: ImageVector = Icons.Default.Add,
    iconModifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = WormaCeptorDesignSystem.Shapes.fab,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.fab,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = iconModifier,
        )
    }
}

@Composable
fun WormaCeptorSmallFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: ImageVector = Icons.Default.Add,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.fab,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

@Composable
fun WormaCeptorExtendedFAB(
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = WormaCeptorDesignSystem.Shapes.fab,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.fab,
        ),
    ) {
        icon?.invoke()
        text()
    }
}
