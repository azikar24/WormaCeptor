/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import com.azikar24.wormaceptor.domain.entities.GridConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Engine for displaying a grid overlay on top of the application.
 *
 * This engine uses a WindowManager overlay to draw a customizable grid
 * that helps visualize layout alignment with Material Design guidelines.
 *
 * Features:
 * - Main grid with configurable size (4dp, 8dp, 16dp, etc.)
 * - Keylines at specific positions from screen edges
 * - Baseline grid for typography alignment
 * - Spacing column visualization
 *
 * Usage:
 * ```kotlin
 * val engine = GridOverlayEngine(context)
 * engine.enable(activity)
 * engine.updateConfig(config)
 * engine.disable()
 * ```
 */
class GridOverlayEngine(private val context: Context) {

    private var overlayView: GridOverlayView? = null
    private var windowManager: WindowManager? = null
    private var activityRef: WeakReference<Activity>? = null

    private val _config = MutableStateFlow(GridConfig.DEFAULT)
    val config: StateFlow<GridConfig> = _config.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    /**
     * Enables the grid overlay for the given activity.
     *
     * @param activity The activity to attach the overlay to
     */
    fun enable(activity: Activity) {
        if (_isEnabled.value) return

        activityRef = WeakReference(activity)
        windowManager = activity.getSystemService(Activity.WINDOW_SERVICE) as WindowManager

        createOverlayView(activity)

        _isEnabled.value = true
    }

    /**
     * Enables the grid overlay using the stored context.
     * Should be called when activity is available.
     */
    fun enable() {
        val activity = activityRef?.get()
        if (activity != null) {
            enable(activity)
        }
    }

    /**
     * Disables the grid overlay and cleans up resources.
     */
    fun disable() {
        if (!_isEnabled.value) return

        removeOverlayView()

        activityRef = null
        windowManager = null

        _isEnabled.value = false
    }

    /**
     * Updates the configuration and refreshes the overlay.
     *
     * @param newConfig The new configuration to apply
     */
    fun updateConfig(newConfig: GridConfig) {
        _config.value = newConfig
        overlayView?.updateConfig(newConfig)
    }

    /**
     * Sets the grid size and updates the overlay.
     *
     * @param size Grid size in dp
     */
    fun setGridSize(size: Int) {
        val clampedSize = size.coerceIn(GridConfig.MIN_GRID_SIZE, GridConfig.MAX_GRID_SIZE)
        updateConfig(_config.value.copy(gridSize = clampedSize))
    }

    /**
     * Toggles the keylines visibility.
     */
    fun toggleKeylines() {
        updateConfig(_config.value.copy(showKeylines = !_config.value.showKeylines))
    }

    /**
     * Toggles the baseline grid visibility.
     */
    fun toggleBaseline() {
        updateConfig(_config.value.copy(baselineGridEnabled = !_config.value.baselineGridEnabled))
    }

    /**
     * Sets the baseline grid size.
     *
     * @param size Baseline grid size in dp
     */
    fun setBaselineSize(size: Int) {
        val clampedSize = size.coerceIn(GridConfig.MIN_BASELINE_SIZE, GridConfig.MAX_BASELINE_SIZE)
        updateConfig(_config.value.copy(baselineGridSize = clampedSize))
    }

    /**
     * Adds a keyline position.
     *
     * @param position Keyline position in dp from edge
     */
    fun addKeyline(position: Int) {
        val currentPositions = _config.value.keylinePositions
        if (currentPositions.size < GridConfig.MAX_KEYLINES && position !in currentPositions) {
            updateConfig(_config.value.copy(keylinePositions = (currentPositions + position).sorted()))
        }
    }

    /**
     * Removes a keyline position.
     *
     * @param position Keyline position in dp to remove
     */
    fun removeKeyline(position: Int) {
        val newPositions = _config.value.keylinePositions - position
        updateConfig(_config.value.copy(keylinePositions = newPositions))
    }

    /**
     * Sets the grid color.
     *
     * @param color Color in ARGB Long format
     */
    fun setGridColor(color: Long) {
        updateConfig(_config.value.copy(gridColor = color))
    }

    /**
     * Sets the grid opacity.
     *
     * @param alpha Opacity value (0.0 to 1.0)
     */
    fun setGridAlpha(alpha: Float) {
        val clampedAlpha = alpha.coerceIn(0.0f, 1.0f)
        updateConfig(_config.value.copy(gridAlpha = clampedAlpha))
    }

    /**
     * Toggles spacing visualization.
     */
    fun toggleSpacing() {
        updateConfig(_config.value.copy(showSpacing = !_config.value.showSpacing))
    }

    private fun createOverlayView(activity: Activity) {
        overlayView = GridOverlayView(activity).apply {
            updateConfig(_config.value)
        }

        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            // Failed to add overlay view
            overlayView = null
        }
    }

    private fun removeOverlayView() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        overlayView = null
    }

    /**
     * Custom View that draws the grid overlay.
     */
    private class GridOverlayView(context: Context) : View(context) {

        private var currentConfig: GridConfig = GridConfig.DEFAULT
        private val density = resources.displayMetrics.density

        private val gridPaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = 1f
        }

        private val keylinePaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = 2f
        }

        private val baselinePaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = 1f
        }

        private val spacingPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        fun updateConfig(config: GridConfig) {
            currentConfig = config

            val baseColor = config.gridColor.toInt()
            val alpha = (config.gridAlpha * 255).toInt()

            // Apply alpha to the grid color
            gridPaint.color = (baseColor and 0x00FFFFFF) or (alpha shl 24)

            // Keylines are more visible (double alpha, max 255)
            val keylineAlpha = minOf((alpha * 2), 255)
            keylinePaint.color = (baseColor and 0x00FFFFFF) or (keylineAlpha shl 24)

            // Baseline grid is more subtle (half alpha)
            val baselineAlpha = alpha / 2
            baselinePaint.color = (baseColor and 0x00FFFFFF) or (baselineAlpha shl 24)

            // Spacing fill is very subtle
            val spacingAlpha = alpha / 4
            spacingPaint.color = (baseColor and 0x00FFFFFF) or (spacingAlpha shl 24)

            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (!currentConfig.enabled) return

            val screenWidth = width
            val screenHeight = height

            // Draw spacing columns first (behind everything)
            if (currentConfig.showSpacing && currentConfig.keylinePositions.isNotEmpty()) {
                drawSpacingColumns(canvas, screenWidth, screenHeight)
            }

            // Draw main grid
            drawMainGrid(canvas, screenWidth, screenHeight)

            // Draw baseline grid
            if (currentConfig.baselineGridEnabled) {
                drawBaselineGrid(canvas, screenWidth, screenHeight)
            }

            // Draw keylines
            if (currentConfig.showKeylines) {
                drawKeylines(canvas, screenWidth, screenHeight)
            }
        }

        private fun drawMainGrid(canvas: Canvas, width: Int, height: Int) {
            val gridSizePx = currentConfig.gridSize * density

            // Vertical lines
            var x = 0f
            while (x < width) {
                canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
                x += gridSizePx
            }

            // Horizontal lines
            var y = 0f
            while (y < height) {
                canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
                y += gridSizePx
            }
        }

        private fun drawBaselineGrid(canvas: Canvas, width: Int, height: Int) {
            val baselineSizePx = currentConfig.baselineGridSize * density

            // Only horizontal lines for baseline grid
            var y = 0f
            while (y < height) {
                canvas.drawLine(0f, y, width.toFloat(), y, baselinePaint)
                y += baselineSizePx
            }
        }

        private fun drawKeylines(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
            for (position in currentConfig.keylinePositions) {
                val positionPx = position * density

                // Left keyline
                canvas.drawLine(positionPx, 0f, positionPx, screenHeight.toFloat(), keylinePaint)

                // Right keyline
                val rightPosition = screenWidth - positionPx
                canvas.drawLine(rightPosition, 0f, rightPosition, screenHeight.toFloat(), keylinePaint)
            }
        }

        private fun drawSpacingColumns(canvas: Canvas, screenWidth: Int, screenHeight: Int) {
            val sortedPositions = currentConfig.keylinePositions.sorted()

            if (sortedPositions.isEmpty()) return

            // Draw spacing fill from edge to first keyline
            val firstKeyline = sortedPositions.first() * density

            // Left spacing column
            canvas.drawRect(0f, 0f, firstKeyline, screenHeight.toFloat(), spacingPaint)

            // Right spacing column
            canvas.drawRect(
                screenWidth - firstKeyline,
                0f,
                screenWidth.toFloat(),
                screenHeight.toFloat(),
                spacingPaint,
            )
        }
    }

    companion object {
        private const val TAG = "GridOverlayEngine"
    }
}
