/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import com.azikar24.wormaceptor.domain.entities.ViewBordersConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Engine for drawing colored borders around views to visualize layout hierarchy.
 *
 * This engine uses a WindowManager overlay to draw borders on top of the app's views,
 * similar to browser developer tools. It traverses the view hierarchy starting from
 * the DecorView and draws colored rectangles for margin, padding, and content areas.
 *
 * Usage:
 * ```kotlin
 * val engine = ViewBordersEngine()
 * engine.enable(activity)
 * engine.updateConfig(config)
 * engine.disable()
 * ```
 */
class ViewBordersEngine {

    private var overlayView: BordersOverlayView? = null
    private var windowManager: WindowManager? = null
    private var activityRef: WeakReference<Activity>? = null
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    private val _config = MutableStateFlow(ViewBordersConfig.DEFAULT)
    val config: StateFlow<ViewBordersConfig> = _config.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    /**
     * Data class representing view bounds information for drawing.
     */
    data class ViewBounds(
        val marginRect: Rect,
        val paddingRect: Rect,
        val contentRect: Rect,
        val width: Int,
        val height: Int,
        val viewName: String,
    )

    /**
     * Enables the view borders overlay for the given activity.
     *
     * @param activity The activity to attach the overlay to
     */
    fun enable(activity: Activity) {
        if (_isEnabled.value) return

        activityRef = WeakReference(activity)
        windowManager = activity.getSystemService(Activity.WINDOW_SERVICE) as WindowManager

        createOverlayView(activity)
        setupLayoutListener(activity)

        _isEnabled.value = true
    }

    /**
     * Disables the view borders overlay and cleans up resources.
     */
    fun disable() {
        if (!_isEnabled.value) return

        removeOverlayView()
        removeLayoutListener()

        activityRef = null
        windowManager = null

        _isEnabled.value = false
    }

    /**
     * Updates the configuration and refreshes the overlay.
     *
     * @param newConfig The new configuration to apply
     */
    fun updateConfig(newConfig: ViewBordersConfig) {
        _config.value = newConfig
        overlayView?.updateConfig(newConfig)
        refreshOverlay()
    }

    /**
     * Forces a refresh of the overlay drawing.
     */
    fun refreshOverlay() {
        val activity = activityRef?.get() ?: return
        val decorView = activity.window?.decorView ?: return

        val viewBoundsList = traverseViewHierarchy(decorView)
        overlayView?.setViewBounds(viewBoundsList)
    }

    private fun createOverlayView(activity: Activity) {
        overlayView = BordersOverlayView(activity).apply {
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

    private fun setupLayoutListener(activity: Activity) {
        val decorView = activity.window?.decorView ?: return

        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            if (_isEnabled.value) {
                refreshOverlay()
            }
        }

        decorView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun removeLayoutListener() {
        val activity = activityRef?.get() ?: return
        val decorView = activity.window?.decorView ?: return

        globalLayoutListener?.let { listener ->
            decorView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
        globalLayoutListener = null
    }

    private fun traverseViewHierarchy(rootView: View): List<ViewBounds> {
        val result = mutableListOf<ViewBounds>()
        traverseViewRecursively(rootView, result)
        return result
    }

    private fun traverseViewRecursively(view: View, result: MutableList<ViewBounds>) {
        // Skip invisible views
        if (view.visibility != View.VISIBLE) return

        // Skip the overlay view itself
        if (view === overlayView) return

        // Get view location on screen
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val left = location[0]
        val top = location[1]
        val right = left + view.width
        val bottom = top + view.height

        // Calculate margin rect (view bounds + margin)
        val params = view.layoutParams
        val marginLeft: Int
        val marginTop: Int
        val marginRight: Int
        val marginBottom: Int

        if (params is ViewGroup.MarginLayoutParams) {
            marginLeft = params.leftMargin
            marginTop = params.topMargin
            marginRight = params.rightMargin
            marginBottom = params.bottomMargin
        } else {
            marginLeft = 0
            marginTop = 0
            marginRight = 0
            marginBottom = 0
        }

        val marginRect = Rect(
            left - marginLeft,
            top - marginTop,
            right + marginRight,
            bottom + marginBottom,
        )

        // View bounds (padding area boundary)
        val paddingRect = Rect(left, top, right, bottom)

        // Content area (view bounds - padding)
        val contentRect = Rect(
            left + view.paddingLeft,
            top + view.paddingTop,
            right - view.paddingRight,
            bottom - view.paddingBottom,
        )

        val viewName = view.javaClass.simpleName

        result.add(
            ViewBounds(
                marginRect = marginRect,
                paddingRect = paddingRect,
                contentRect = contentRect,
                width = view.width,
                height = view.height,
                viewName = viewName,
            ),
        )

        // Recursively traverse children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                traverseViewRecursively(view.getChildAt(i), result)
            }
        }
    }

    /**
     * Custom View that draws the border overlays.
     */
    private class BordersOverlayView(activity: Activity) : View(activity) {

        private var viewBoundsList: List<ViewBounds> = emptyList()
        private var currentConfig: ViewBordersConfig = ViewBordersConfig.DEFAULT

        private val marginPaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        private val paddingPaint = Paint().apply {
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        private val contentPaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        private val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 24f
            color = 0xFF000000.toInt()
        }

        private val textBackgroundPaint = Paint().apply {
            style = Paint.Style.FILL
            color = 0xCCFFFFFF.toInt()
        }

        fun updateConfig(config: ViewBordersConfig) {
            currentConfig = config

            val strokeWidth = config.borderWidth * resources.displayMetrics.density

            marginPaint.apply {
                color = config.marginColor.toInt()
                this.strokeWidth = strokeWidth
            }

            paddingPaint.apply {
                color = config.paddingColor.toInt()
                this.strokeWidth = strokeWidth
            }

            contentPaint.color = config.contentColor.toInt()

            invalidate()
        }

        fun setViewBounds(bounds: List<ViewBounds>) {
            viewBoundsList = bounds
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (!currentConfig.enabled) return

            for (bounds in viewBoundsList) {
                // Draw content area fill
                canvas.drawRect(
                    bounds.contentRect.left.toFloat(),
                    bounds.contentRect.top.toFloat(),
                    bounds.contentRect.right.toFloat(),
                    bounds.contentRect.bottom.toFloat(),
                    contentPaint,
                )

                // Draw padding border (view bounds)
                canvas.drawRect(
                    bounds.paddingRect.left.toFloat(),
                    bounds.paddingRect.top.toFloat(),
                    bounds.paddingRect.right.toFloat(),
                    bounds.paddingRect.bottom.toFloat(),
                    paddingPaint,
                )

                // Draw margin border (only if there's actual margin)
                if (bounds.marginRect != bounds.paddingRect) {
                    canvas.drawRect(
                        bounds.marginRect.left.toFloat(),
                        bounds.marginRect.top.toFloat(),
                        bounds.marginRect.right.toFloat(),
                        bounds.marginRect.bottom.toFloat(),
                        marginPaint,
                    )
                }

                // Draw dimensions if enabled
                if (currentConfig.showDimensions) {
                    val dimensionText = "${bounds.width}x${bounds.height}"
                    val textWidth = textPaint.measureText(dimensionText)
                    val textX = bounds.paddingRect.left.toFloat() + 4
                    val textY = bounds.paddingRect.top.toFloat() + textPaint.textSize

                    // Draw background for text readability
                    canvas.drawRect(
                        textX - 2,
                        textY - textPaint.textSize,
                        textX + textWidth + 2,
                        textY + 4,
                        textBackgroundPaint,
                    )

                    canvas.drawText(dimensionText, textX, textY, textPaint)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ViewBordersEngine"
    }
}
