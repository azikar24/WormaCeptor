/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewborders.ui

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.ViewBordersConfig
import com.azikar24.wormaceptor.feature.viewborders.ui.theme.ViewBordersColors
import com.azikar24.wormaceptor.feature.viewborders.ui.theme.toArgbLong
import com.azikar24.wormaceptor.feature.viewborders.ui.theme.toComposeColor
import com.azikar24.wormaceptor.feature.viewborders.ui.theme.viewBordersColors
import com.azikar24.wormaceptor.feature.viewborders.vm.ViewBordersViewModel
import kotlin.math.roundToInt

/**
 * Main screen for configuring View Borders feature.
 *
 * Features:
 * - Enable/Disable toggle
 * - Border width slider (1-5dp)
 * - Color customization for margin/padding/content
 * - Show dimensions toggle
 * - Visual legend explaining colors
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewBordersScreen(
    viewModel: ViewBordersViewModel,
    activity: Activity,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    val config by viewModel.config.collectAsState()
    val colors = viewBordersColors()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = "View Borders",
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (config.enabled) {
                            Surface(
                                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(
                                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                    ),
                                )
                            }
                        }
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
                    IconButton(onClick = { viewModel.resetToDefaults(activity) }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
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
                .verticalScroll(scrollState),
        ) {
            // Enable Toggle Section
            EnableToggleSection(
                enabled = config.enabled,
                onToggle = { viewModel.toggleEnabled(activity) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.sm))

            // Visual Legend
            ColorLegendCard(
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

            // Settings Section
            SectionHeader(text = "Settings")

            // Border Width Slider
            BorderWidthSlider(
                borderWidth = config.borderWidth,
                onWidthChange = { viewModel.setBorderWidth(it) },
            )

            // Show Dimensions Toggle
            ListItem(
                headlineContent = {
                    Text("Show Dimensions")
                },
                supportingContent = {
                    Text(
                        text = "Display width x height on each view",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = config.showDimensions,
                        onCheckedChange = { viewModel.setShowDimensions(it) },
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.sm))

            // Color Customization Section
            SectionHeader(text = "Colors")

            ColorPickerItem(
                label = "Margin Color",
                description = "Space outside the view's border",
                currentColor = config.marginColor.toComposeColor(),
                defaultColor = ViewBordersColors.DefaultMargin,
                onColorSelected = { viewModel.setMarginColor(it.copy(alpha = 0.3f).toArgbLong()) },
            )

            ColorPickerItem(
                label = "Padding Color",
                description = "Space between border and content",
                currentColor = config.paddingColor.toComposeColor(),
                defaultColor = ViewBordersColors.DefaultPadding,
                onColorSelected = { viewModel.setPaddingColor(it.copy(alpha = 0.3f).toArgbLong()) },
            )

            ColorPickerItem(
                label = "Content Color",
                description = "The actual content area",
                currentColor = config.contentColor.toComposeColor(),
                defaultColor = ViewBordersColors.DefaultContent,
                onColorSelected = { viewModel.setContentColor(it.copy(alpha = 0.3f).toArgbLong()) },
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
        }
    }
}

@Composable
private fun EnableToggleSection(enabled: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(
        targetValue = if (enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "toggle_background",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(WormaCeptorDesignSystem.Spacing.xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.BorderOuter,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xxl),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                Column {
                    Text(
                        text = if (enabled) "View Borders Active" else "View Borders Disabled",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = if (enabled) {
                            "Tap to disable the overlay"
                        } else {
                            "Tap to visualize view boundaries"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun ColorLegendCard(colors: ViewBordersColors, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                )
                Text(
                    text = "Color Legend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Visual diagram showing nested boxes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .background(colors.marginBackground)
                    .border(
                        WormaCeptorDesignSystem.BorderWidth.thick,
                        colors.margin,
                        RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                    )
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                        .background(colors.paddingBackground)
                        .border(
                            WormaCeptorDesignSystem.BorderWidth.thick,
                            colors.padding,
                            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                        )
                        .padding(WormaCeptorDesignSystem.Spacing.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
                            .background(colors.contentBackground)
                            .border(
                                WormaCeptorDesignSystem.BorderWidth.thick,
                                colors.content,
                                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Content",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.content,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Legend items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                LegendItem(color = colors.margin, label = "Margin")
                LegendItem(color = colors.padding, label = "Padding")
                LegendItem(color = colors.content, label = "Content")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(WormaCeptorDesignSystem.Spacing.md)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg, vertical = WormaCeptorDesignSystem.Spacing.md),
    )
}

@Composable
private fun BorderWidthSlider(borderWidth: Int, onWidthChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Border Width",
                style = MaterialTheme.typography.bodyLarge,
            )
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = "${borderWidth}dp",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Slider(
            value = borderWidth.toFloat(),
            onValueChange = { onWidthChange(it.roundToInt()) },
            valueRange = ViewBordersConfig.MIN_BORDER_WIDTH.toFloat()..ViewBordersConfig.MAX_BORDER_WIDTH.toFloat(),
            steps = ViewBordersConfig.MAX_BORDER_WIDTH - ViewBordersConfig.MIN_BORDER_WIDTH - 1,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Thin",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Thick",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorPickerItem(
    label: String,
    description: String,
    currentColor: Color,
    defaultColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ListItem(
            headlineContent = {
                Text(label)
            },
            supportingContent = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorDesignSystem.Spacing.xxxl)
                        .clip(CircleShape)
                        .background(currentColor)
                        .border(
                            WormaCeptorDesignSystem.BorderWidth.thick,
                            MaterialTheme.colorScheme.outline,
                            CircleShape,
                        ),
                )
            },
            trailingContent = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.BorderOuter,
                        contentDescription = "Select color",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            modifier = Modifier.clickable { expanded = !expanded },
        )

        if (expanded) {
            ColorPalette(
                currentColor = currentColor,
                defaultColor = defaultColor,
                onColorSelected = {
                    onColorSelected(it)
                    expanded = false
                },
                modifier = Modifier.padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                ),
            )
        }
    }
}

@Composable
private fun ColorPalette(
    currentColor: Color,
    defaultColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorOptions = listOf(
        // Default
        defaultColor,
        // Reds
        Color(0xFFE53935),
        Color(0xFFF44336),
        // Oranges
        Color(0xFFFF9800),
        Color(0xFFFFB300),
        // Yellows
        Color(0xFFFFEB3B),
        Color(0xFFFDD835),
        // Greens
        Color(0xFF4CAF50),
        Color(0xFF66BB6A),
        // Teals
        Color(0xFF009688),
        Color(0xFF26A69A),
        // Blues
        Color(0xFF2196F3),
        Color(0xFF42A5F5),
        // Purples
        Color(0xFF9C27B0),
        Color(0xFFAB47BC),
        // Pinks
        Color(0xFFE91E63),
        Color(0xFFEC407A),
        // Grays
        Color(0xFF607D8B),
        Color(0xFF78909C),
    )

    Surface(
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Color grid
            val chunkedColors = colorOptions.chunked(6)
            chunkedColors.forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    rowColors.forEach { color ->
                        ColorSwatch(
                            color = color,
                            isSelected = color == currentColor ||
                                (color == defaultColor && currentColor.alpha < 0.5f),
                            onClick = { onColorSelected(color) },
                        )
                    }
                    // Fill remaining space if row is incomplete
                    repeat(6 - rowColors.size) {
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xxl))
                    }
                }
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(WormaCeptorDesignSystem.Spacing.xxl)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.thick else WormaCeptorDesignSystem.BorderWidth.regular,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                },
                shape = CircleShape,
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.lg),
            )
        }
    }
}
