/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.touchvisualization.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.TouchAction
import com.azikar24.wormaceptor.domain.entities.TouchPoint
import com.azikar24.wormaceptor.domain.entities.TouchVisualizationConfig
import com.azikar24.wormaceptor.feature.touchvisualization.ui.theme.TouchVisualizationColors
import com.azikar24.wormaceptor.feature.touchvisualization.ui.theme.toComposeColor
import com.azikar24.wormaceptor.feature.touchvisualization.ui.theme.touchVisualizationColors
import com.azikar24.wormaceptor.feature.touchvisualization.vm.TouchVisualizationViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

/**
 * Main settings screen for Touch Visualization feature.
 *
 * Features:
 * - Enable/Disable toggle for overlay
 * - Circle color picker with preset colors
 * - Circle size slider
 * - Trail enable toggle
 * - Show coordinates toggle
 * - Interactive preview area
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchVisualizationScreen(
    viewModel: TouchVisualizationViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val config by viewModel.config.collectAsState()
    val isEnabled by viewModel.isEnabled.collectAsState()

    val colors = touchVisualizationColors()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Touch Visualization",
                            fontWeight = FontWeight.SemiBold,
                        )
                        EnabledIndicator(isEnabled = isEnabled)
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetToDefaults() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset to defaults",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Enable/Disable Card
            EnableToggleCard(
                isEnabled = isEnabled,
                onToggle = { viewModel.toggle() },
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )

            // Preview Area
            PreviewCard(
                config = config,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
            )

            // Settings Cards
            SettingsSection(
                config = config,
                onColorChange = { viewModel.setCircleColor(it) },
                onSizeChange = { viewModel.setCircleSize(it) },
                onTrailToggle = { viewModel.setTrailEnabled(it) },
                onCoordinatesToggle = { viewModel.setShowCoordinates(it) },
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun EnabledIndicator(isEnabled: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(
                if (isEnabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                },
            ),
    )
}

@Composable
private fun EnableToggleCard(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    colors: TouchVisualizationColors,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            colors.settingsCardBackground
        },
        animationSpec = tween(300),
        label = "enable_bg_color",
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = if (isEnabled) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Column {
                    Text(
                        text = "Touch Overlay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )

                    Text(
                        text = if (isEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isEnabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
            )
        }
    }
}

@Composable
private fun PreviewCard(
    config: TouchVisualizationConfig,
    colors: TouchVisualizationColors,
    modifier: Modifier = Modifier,
) {
    // Local preview touches state
    val previewTouches = remember { mutableStateListOf<TouchPoint>() }
    val previewTrail = remember { mutableStateListOf<TouchPoint>() }
    var touchIdCounter by remember { mutableStateOf(0) }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.previewBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "Preview - Touch to test",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = colors.previewBorder,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .pointerInput(config) {
                        detectTapGestures(
                            onPress = { offset ->
                                val id = touchIdCounter++
                                val touchPoint = TouchPoint(
                                    id = id,
                                    x = offset.x,
                                    y = offset.y,
                                    pressure = 0.5f,
                                    size = 0.1f,
                                    timestamp = System.currentTimeMillis(),
                                    action = TouchAction.DOWN,
                                )
                                previewTouches.add(touchPoint)
                                if (config.trailEnabled) {
                                    previewTrail.add(touchPoint)
                                }

                                tryAwaitRelease()

                                previewTouches.removeAll { it.id == id }
                            },
                        )
                    }
                    .pointerInput(config) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val id = touchIdCounter++
                                val touchPoint = TouchPoint(
                                    id = id,
                                    x = offset.x,
                                    y = offset.y,
                                    pressure = 0.5f,
                                    size = 0.1f,
                                    timestamp = System.currentTimeMillis(),
                                    action = TouchAction.DOWN,
                                )
                                previewTouches.clear()
                                previewTouches.add(touchPoint)
                                if (config.trailEnabled) {
                                    previewTrail.clear()
                                    previewTrail.add(touchPoint)
                                }
                            },
                            onDrag = { change, _ ->
                                if (previewTouches.isNotEmpty()) {
                                    val id = previewTouches.first().id
                                    val touchPoint = TouchPoint(
                                        id = id,
                                        x = change.position.x,
                                        y = change.position.y,
                                        pressure = 0.5f,
                                        size = 0.1f,
                                        timestamp = System.currentTimeMillis(),
                                        action = TouchAction.MOVE,
                                    )
                                    previewTouches[0] = touchPoint
                                    if (config.trailEnabled) {
                                        previewTrail.add(touchPoint)
                                        // Limit trail size
                                        while (previewTrail.size > 100) {
                                            previewTrail.removeAt(0)
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                previewTouches.clear()
                                // Keep trail for a moment then clear
                            },
                            onDragCancel = {
                                previewTouches.clear()
                            },
                        )
                    },
            ) {
                // Draw the touch visualization overlay
                TouchVisualizationOverlay(
                    activeTouches = previewTouches.toImmutableList(),
                    touchTrail = previewTrail.toImmutableList(),
                    config = config,
                    modifier = Modifier.fillMaxSize(),
                )

                // Show hint if no touches
                if (previewTouches.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Touch or drag to preview",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    config: TouchVisualizationConfig,
    onColorChange: (Long) -> Unit,
    onSizeChange: (Float) -> Unit,
    onTrailToggle: (Boolean) -> Unit,
    onCoordinatesToggle: (Boolean) -> Unit,
    colors: TouchVisualizationColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Color Picker
        ColorPickerCard(
            selectedColor = config.circleColor,
            onColorChange = onColorChange,
            colors = colors,
            modifier = Modifier.fillMaxWidth(),
        )

        // Circle Size Slider
        SizeSliderCard(
            size = config.circleSize,
            onSizeChange = onSizeChange,
            circleColor = config.circleColor,
            colors = colors,
            modifier = Modifier.fillMaxWidth(),
        )

        // Trail Toggle
        ToggleSettingCard(
            title = "Touch Trail",
            description = "Show trailing line following finger movement",
            isEnabled = config.trailEnabled,
            onToggle = onTrailToggle,
            colors = colors,
            modifier = Modifier.fillMaxWidth(),
        )

        // Coordinates Toggle
        ToggleSettingCard(
            title = "Show Coordinates",
            description = "Display X/Y coordinates near touch points",
            isEnabled = config.showCoordinates,
            onToggle = onCoordinatesToggle,
            colors = colors,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ColorPickerCard(
    selectedColor: Long,
    onColorChange: (Long) -> Unit,
    colors: TouchVisualizationColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.settingsCardBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Circle Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TouchVisualizationConfig.PRESET_COLORS.forEach { color ->
                    ColorSwatch(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onColorChange(color) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Long, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "swatch_border",
    )

    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color.toComposeColor())
            .border(
                width = 2.dp,
                color = borderColor,
                shape = CircleShape,
            )
            .clickable { onClick() },
    ) {
        if (isSelected) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw checkmark indicator
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = size.minDimension / 4,
                    center = Offset(size.width / 2, size.height / 2),
                )
            }
        }
    }
}

@Composable
private fun SizeSliderCard(
    size: Float,
    onSizeChange: (Float) -> Unit,
    circleColor: Long,
    colors: TouchVisualizationColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.settingsCardBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Circle Size",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "${size.roundToInt()} dp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Small circle preview
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(circleColor.toComposeColor().copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(circleColor.toComposeColor()),
                    )
                }

                Slider(
                    value = size,
                    onValueChange = onSizeChange,
                    valueRange = TouchVisualizationConfig.MIN_CIRCLE_SIZE..TouchVisualizationConfig.MAX_CIRCLE_SIZE,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = circleColor.toComposeColor(),
                        activeTrackColor = circleColor.toComposeColor(),
                    ),
                )

                // Large circle preview
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(circleColor.toComposeColor().copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(circleColor.toComposeColor()),
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: TouchVisualizationColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.settingsCardBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }
    }
}
