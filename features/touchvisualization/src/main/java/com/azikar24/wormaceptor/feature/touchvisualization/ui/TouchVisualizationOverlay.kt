/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.touchvisualization.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.TouchPoint
import com.azikar24.wormaceptor.domain.entities.TouchVisualizationConfig
import com.azikar24.wormaceptor.feature.touchvisualization.ui.theme.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

/**
 * Composable that draws touch visualization overlay.
 *
 * Renders:
 * - Colored circles at touch points with ripple effect
 * - Optional trail lines following finger movement
 * - Optional coordinate text near touch points
 */
@Composable
fun TouchVisualizationOverlay(
    activeTouches: ImmutableList<TouchPoint>,
    touchTrail: ImmutableList<TouchPoint>,
    config: TouchVisualizationConfig,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val circleSizePx = with(density) { config.circleSize.dp.toPx() }
    val circleColor = config.circleColor.toComposeColor()

    // Ripple animations for each touch point
    val rippleAnimations = remember { mutableMapOf<Int, Animatable<Float, *>>() }

    // Launch ripple animations for new touches
    LaunchedEffect(activeTouches) {
        activeTouches.forEach { touch ->
            if (!rippleAnimations.containsKey(touch.id)) {
                rippleAnimations[touch.id] = Animatable(0f)
                launch {
                    rippleAnimations[touch.id]?.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = RIPPLE_DURATION_MS,
                            easing = LinearEasing,
                        ),
                    )
                }
            }
        }

        // Clean up animations for removed touches
        val activeIds = activeTouches.map { it.id }.toSet()
        rippleAnimations.keys.filterNot { it in activeIds }.forEach {
            rippleAnimations.remove(it)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw trails if enabled
        if (config.trailEnabled && touchTrail.isNotEmpty()) {
            drawTouchTrails(
                touchTrail = touchTrail,
                color = circleColor,
            )
        }

        // Draw touch points with ripple effects
        activeTouches.forEach { touch ->
            val rippleProgress = rippleAnimations[touch.id]?.value ?: 0f

            drawTouchPoint(
                touch = touch,
                color = circleColor,
                baseSize = circleSizePx,
                rippleProgress = rippleProgress,
            )

            // Draw coordinates if enabled
            if (config.showCoordinates) {
                drawCoordinates(
                    touch = touch,
                    baseSize = circleSizePx,
                )
            }
        }
    }
}

/**
 * Draws a single touch point with ripple effect.
 */
private fun DrawScope.drawTouchPoint(touch: TouchPoint, color: Color, baseSize: Float, rippleProgress: Float) {
    val center = Offset(touch.x, touch.y)

    // Adjust size based on pressure
    val pressureScale = 0.8f + touch.pressure * 0.4f
    val adjustedSize = baseSize * pressureScale / 2

    // Draw outer ripple effect
    if (rippleProgress > 0f && rippleProgress < 1f) {
        val rippleRadius = adjustedSize * (1f + rippleProgress * 1.5f)
        val rippleAlpha = (1f - rippleProgress) * 0.4f

        drawCircle(
            color = color.copy(alpha = rippleAlpha),
            radius = rippleRadius,
            center = center,
            style = Stroke(width = 4f),
        )
    }

    // Draw main circle with gradient-like effect
    // Outer ring
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = adjustedSize,
        center = center,
    )

    // Inner circle
    drawCircle(
        color = color.copy(alpha = 0.6f),
        radius = adjustedSize * 0.7f,
        center = center,
    )

    // Core dot
    drawCircle(
        color = color,
        radius = adjustedSize * 0.4f,
        center = center,
    )

    // White highlight for depth
    drawCircle(
        color = Color.White.copy(alpha = 0.4f),
        radius = adjustedSize * 0.15f,
        center = Offset(center.x - adjustedSize * 0.2f, center.y - adjustedSize * 0.2f),
    )
}

/**
 * Draws touch trails.
 */
private fun DrawScope.drawTouchTrails(touchTrail: List<TouchPoint>, color: Color) {
    // Group trail points by pointer ID
    val trailsByPointer = touchTrail.groupBy { it.id }

    trailsByPointer.forEach { (_, points) ->
        if (points.size < 2) return@forEach

        val path = Path()
        val sortedPoints = points.sortedBy { it.timestamp }

        sortedPoints.forEachIndexed { index, point ->
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                // Use quadratic bezier for smoother curves
                val prevPoint = sortedPoints[index - 1]
                val midX = (prevPoint.x + point.x) / 2
                val midY = (prevPoint.y + point.y) / 2

                if (index == 1) {
                    path.lineTo(midX, midY)
                } else {
                    path.quadraticBezierTo(prevPoint.x, prevPoint.y, midX, midY)
                }
            }
        }

        // Draw trail with gradient-like effect using multiple strokes
        drawPath(
            path = path,
            color = color.copy(alpha = 0.2f),
            style = Stroke(
                width = 12f,
                pathEffect = PathEffect.cornerPathEffect(8f),
            ),
        )

        drawPath(
            path = path,
            color = color.copy(alpha = 0.4f),
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.cornerPathEffect(8f),
            ),
        )

        drawPath(
            path = path,
            color = color.copy(alpha = 0.7f),
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.cornerPathEffect(8f),
            ),
        )
    }
}

/**
 * Draws coordinate text near a touch point.
 */
private fun DrawScope.drawCoordinates(touch: TouchPoint, baseSize: Float) {
    val text = "(${touch.x.toInt()}, ${touch.y.toInt()})"
    val textSize = 28f
    val padding = 8f

    // Position text above the touch point
    val textX = touch.x
    val textY = touch.y - baseSize - padding - textSize

    // Draw using native canvas for text
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            this.textSize = textSize
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
        }

        // Draw background
        val textBounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)

        val bgPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(180, 0, 0, 0)
            isAntiAlias = true
        }

        val bgLeft = textX - textBounds.width() / 2 - padding
        val bgTop = textY - textBounds.height() - padding
        val bgRight = textX + textBounds.width() / 2 + padding
        val bgBottom = textY + padding

        drawRoundRect(
            bgLeft,
            bgTop,
            bgRight,
            bgBottom,
            8f,
            8f,
            bgPaint,
        )

        // Draw text
        drawText(text, textX, textY, paint)
    }
}

private const val RIPPLE_DURATION_MS = 400
