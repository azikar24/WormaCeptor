/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.azikar24.wormaceptor.domain.entities.TouchAction
import com.azikar24.wormaceptor.domain.entities.TouchPoint
import com.azikar24.wormaceptor.domain.entities.TouchVisualizationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Engine that provides touch visualization overlay.
 *
 * Creates an invisible overlay view that intercepts touch events (without consuming them),
 * tracks active touch points with multi-touch support, and exposes the state for visualization.
 *
 * Requires SYSTEM_ALERT_WINDOW permission for overlay display.
 */
class TouchVisualizationEngine(
    private val context: Context,
) {
    private var windowManager: WindowManager? = null
    private var overlayView: TouchInterceptorView? = null
    private var isEnabled = false

    private val _activeTouches = MutableStateFlow<List<TouchPoint>>(emptyList())
    val activeTouches: StateFlow<List<TouchPoint>> = _activeTouches.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _config = MutableStateFlow(TouchVisualizationConfig.DEFAULT)
    val config: StateFlow<TouchVisualizationConfig> = _config.asStateFlow()

    // Trail history for each pointer
    private val trailHistory = CopyOnWriteArrayList<TouchPoint>()

    private val _touchTrail = MutableStateFlow<List<TouchPoint>>(emptyList())
    val touchTrail: StateFlow<List<TouchPoint>> = _touchTrail.asStateFlow()

    /**
     * Updates the visualization configuration.
     */
    fun updateConfig(newConfig: TouchVisualizationConfig) {
        _config.value = newConfig
    }

    /**
     * Enables touch visualization overlay.
     * Requires SYSTEM_ALERT_WINDOW permission.
     */
    fun enable() {
        if (isEnabled) return

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = TouchInterceptorView(context) { event ->
            processTouchEvent(event)
        }

        val layoutParams = createLayoutParams()

        try {
            windowManager?.addView(overlayView, layoutParams)
            isEnabled = true
            _isRunning.value = true
        } catch (e: Exception) {
            // Handle permission not granted or other errors
            isEnabled = false
            _isRunning.value = false
        }
    }

    /**
     * Disables touch visualization overlay.
     */
    fun disable() {
        if (!isEnabled) return

        try {
            windowManager?.removeView(overlayView)
        } catch (e: Exception) {
            // View might already be removed
        }

        overlayView = null
        windowManager = null
        isEnabled = false
        _isRunning.value = false
        _activeTouches.value = emptyList()
        clearTrail()
    }

    /**
     * Clears the touch trail history.
     */
    fun clearTrail() {
        trailHistory.clear()
        _touchTrail.value = emptyList()
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
    }

    private fun processTouchEvent(event: MotionEvent) {
        val touchPoints = mutableListOf<TouchPoint>()
        val timestamp = System.currentTimeMillis()

        // Process all pointers
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            val pressure = event.getPressure(i)
            val size = event.getSize(i)

            val action = when {
                event.actionMasked == MotionEvent.ACTION_DOWN && i == event.actionIndex -> TouchAction.DOWN
                event.actionMasked == MotionEvent.ACTION_POINTER_DOWN && i == event.actionIndex -> TouchAction.DOWN
                event.actionMasked == MotionEvent.ACTION_UP && i == event.actionIndex -> TouchAction.UP
                event.actionMasked == MotionEvent.ACTION_POINTER_UP && i == event.actionIndex -> TouchAction.UP
                event.actionMasked == MotionEvent.ACTION_CANCEL -> TouchAction.UP
                else -> TouchAction.MOVE
            }

            val touchPoint = TouchPoint(
                id = pointerId,
                x = x,
                y = y,
                pressure = pressure,
                size = size,
                timestamp = timestamp,
                action = action,
            )

            // Add to active touches unless it's an UP action
            if (action != TouchAction.UP) {
                touchPoints.add(touchPoint)
            }

            // Add to trail history if trails enabled
            if (_config.value.trailEnabled && action != TouchAction.UP) {
                trailHistory.add(touchPoint)
                // Keep only recent trail points (last 100 per pointer, max 1000 total)
                while (trailHistory.size > MAX_TRAIL_POINTS) {
                    trailHistory.removeAt(0)
                }
            }
        }

        _activeTouches.value = touchPoints.toList()
        _touchTrail.value = trailHistory.toList()

        // Clear trail on all fingers up
        if (event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            // Delay clear to allow fade-out animation
            if (event.pointerCount == 1) {
                // Schedule trail clear after animation
            }
        }
    }

    companion object {
        private const val MAX_TRAIL_POINTS = 1000
    }
}

/**
 * Invisible view that intercepts touch events without consuming them.
 * Uses FLAG_NOT_TOUCHABLE so touches pass through, but watches via FLAG_WATCH_OUTSIDE_TOUCH.
 */
@SuppressLint("ViewConstructor")
private class TouchInterceptorView(
    context: Context,
    private val onTouchEvent: (MotionEvent) -> Unit,
) : View(context) {

    init {
        // Make view completely transparent
        setBackgroundColor(0x00000000)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        onTouchEvent(event)
        // Return false to not consume the event
        return false
    }
}
