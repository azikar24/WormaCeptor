package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/** Reusable FAB with primary/onPrimary colors for better contrast. */
@Composable
fun WormaCeptorFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: ImageVector = Icons.Default.Add,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = WormaCeptorDesignSystem.Shapes.fab,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
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
