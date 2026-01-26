/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp.wormaceptorui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SegmentedControlHeight = 40.dp
private val SegmentedControlCornerRadius = 8.dp
private val SegmentedControlPadding = 4.dp
private const val AnimationDurationMs = 250

private val SelectedBackgroundLight = Color.White

@Suppress("MagicNumber")
private val SelectedBackgroundDark = Color(0xFF1F1F1F)

/**
 * A segmented control component with smooth slide animation.
 *
 * @param segments List of segment labels to display
 * @param selectedIndex Currently selected segment index (0-based)
 * @param onSelectedChange Callback invoked when selection changes
 * @param modifier Modifier to apply to the control
 */
@Composable
fun SegmentedControl(
    segments: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(segments.isNotEmpty()) { "Segments list cannot be empty" }
    require(selectedIndex in segments.indices) { "Selected index out of bounds" }

    val isDarkTheme = isSystemInDarkTheme()
    val selectedBackground = if (isDarkTheme) SelectedBackgroundDark else SelectedBackgroundLight
    val indicatorCornerRadius = SegmentedControlCornerRadius - SegmentedControlPadding / 2

    // Animate indicator position: -1f = left, 1f = right for 2 segments
    val segmentCount = segments.size
    val targetBias = if (segmentCount > 1) {
        -1f + 2f * selectedIndex / (segmentCount - 1)
    } else {
        0f
    }
    val animatedBias by animateFloatAsState(
        targetValue = targetBias,
        animationSpec = tween(durationMillis = AnimationDurationMs),
        label = "indicator_bias",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(SegmentedControlHeight)
            .clip(RoundedCornerShape(SegmentedControlCornerRadius))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        val containerWidth = maxWidth
        val indicatorWidth = (containerWidth - SegmentedControlPadding * 2) / segmentCount

        // Selection indicator with shadow
        Box(
            modifier = Modifier
                .padding(SegmentedControlPadding)
                .fillMaxHeight()
                .width(indicatorWidth)
                .align(BiasAlignment(horizontalBias = animatedBias, verticalBias = 0f))
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(indicatorCornerRadius),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f),
                )
                .background(
                    color = selectedBackground,
                    shape = RoundedCornerShape(indicatorCornerRadius),
                ),
        )

        // Segment buttons
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(SegmentedControlPadding),
        ) {
            for (index in segments.indices) {
                val label = segments[index]
                SegmentButton(
                    label = label,
                    isSelected = index == selectedIndex,
                    onClick = { onSelectedChange(index) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SegmentButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .semantics {
                role = Role.Tab
                selected = isSelected
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )
    }
}
