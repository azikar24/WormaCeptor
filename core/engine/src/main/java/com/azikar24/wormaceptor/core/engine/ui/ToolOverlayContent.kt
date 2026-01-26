/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.engine.ToolOverlayEngine
import com.azikar24.wormaceptor.core.engine.ToolOverlayState

// Colors
private val ToolbarBackground = Color(0xFF1C1C1E)
private val DismissBackground = Color(0xFFFF3B30) // Red when in dismiss zone
private val ActiveBackground = Color(0xFF32D74B).copy(alpha = 0.15f)
private val InactiveBackground = Color(0xFF8E8E93).copy(alpha = 0.10f)
private val ActiveIconColor = Color(0xFF32D74B)
private val InactiveIconColor = Color(0xFF8E8E93)
private val DismissIconColor = Color.White

/**
 * Floating toolbar overlay with View Borders and Measurement toggles.
 *
 * Displays a vertical stack of two icon buttons that can be dragged anywhere
 * on screen. Active tools show filled green icons, inactive show outlined grey icons.
 */
@Composable
fun ToolOverlayContent(
    state: ToolOverlayState,
    callbacks: ToolOverlayEngine.OverlayCallbacks,
    modifier: Modifier = Modifier,
) {
    val isInDismissZone = state.isDragging && state.isInDismissZone()

    // Drag scale animation - larger when in dismiss zone
    val dragScale by animateFloatAsState(
        targetValue = when {
            isInDismissZone -> 1.15f
            state.isDragging -> 1.08f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "dragScale",
    )

    // Background color animation - red when in dismiss zone
    val backgroundColor by animateColorAsState(
        targetValue = if (isInDismissZone) DismissBackground else ToolbarBackground,
        animationSpec = tween(durationMillis = 150),
        label = "backgroundColor",
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = dragScale
                scaleY = dragScale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { callbacks.onDragStart() },
                    onDragEnd = { callbacks.onDragEnd() },
                    onDragCancel = { callbacks.onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        callbacks.onDrag(dragAmount)
                    },
                )
            }
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // View Borders toggle
        ToolButton(
            icon = if (state.viewBordersEnabled) Icons.Filled.GridOn else Icons.Outlined.GridOn,
            isActive = state.viewBordersEnabled,
            isInDismissZone = isInDismissZone,
            contentDescription = "Toggle View Borders",
            onClick = callbacks.onToggleViewBorders,
        )

        // Measurement toggle
        ToolButton(
            icon = if (state.measurementEnabled) Icons.Filled.Straighten else Icons.Outlined.Straighten,
            isActive = state.measurementEnabled,
            isInDismissZone = isInDismissZone,
            contentDescription = "Toggle Measurement",
            onClick = callbacks.onToggleMeasurement,
        )
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    isActive: Boolean,
    isInDismissZone: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pressScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressScale",
    )

    // When in dismiss zone, show white icons on transparent background
    val buttonBackground = when {
        isInDismissZone -> Color.White.copy(alpha = 0.2f)
        isActive -> ActiveBackground
        else -> InactiveBackground
    }

    val iconColor = when {
        isInDismissZone -> DismissIconColor
        isActive -> ActiveIconColor
        else -> InactiveIconColor
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )
    }
}
