package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Toggle button for monitoring start/stop in TopAppBar actions.
 * Shows Pause icon when active, PlayArrow when inactive.
 *
 * @param isActive Whether monitoring is currently active
 * @param onToggle Callback when the button is clicked
 * @param modifier Modifier for the root composable
 * @param activeContentDescription Content description when active
 * @param inactiveContentDescription Content description when inactive
 */
@Composable
fun WormaCeptorPlayPauseButton(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    activeContentDescription: String = "Pause monitoring",
    inactiveContentDescription: String = "Start monitoring",
) {
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isActive) activeContentDescription else inactiveContentDescription,
            tint = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
