/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.azikar24.wormaceptor.domain.entities.MeasurementConfig
import com.azikar24.wormaceptor.domain.entities.MeasurementMode
import com.azikar24.wormaceptor.domain.entities.MeasurementResult
import com.azikar24.wormaceptor.domain.entities.ViewMeasurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Engine that provides UI measurement functionality.
 *
 * Supports two measurement modes:
 * - DISTANCE: Measure the distance between two tapped points
 * - VIEW_BOUNDS: Measure the dimensions and layout properties of a tapped view
 *
 * Creates a transparent overlay that intercepts touch events for measurement input.
 */
class MeasurementEngine {

    private var windowManager: WindowManager? = null
    private var overlayView: MeasurementInterceptorView? = null
    private var activityRef: WeakReference<Activity>? = null
    private var density: Float = 1f

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _mode = MutableStateFlow(MeasurementMode.DISTANCE)
    val mode: StateFlow<MeasurementMode> = _mode.asStateFlow()

    private val _currentMeasurement = MutableStateFlow<MeasurementResult?>(null)
    val currentMeasurement: StateFlow<MeasurementResult?> = _currentMeasurement.asStateFlow()

    private val _selectedView = MutableStateFlow<ViewMeasurement?>(null)
    val selectedView: StateFlow<ViewMeasurement?> = _selectedView.asStateFlow()

    private val _config = MutableStateFlow(MeasurementConfig.DEFAULT)
    val config: StateFlow<MeasurementConfig> = _config.asStateFlow()

    // For distance measurement: tracks first point
    private var firstPoint: MeasurementResult.Point? = null

    // Listener for overlay view updates
    private var overlayUpdateListener: ((MeasurementResult?, ViewMeasurement?, MeasurementConfig) -> Unit)? = null

    /**
     * Enables the measurement overlay for the given activity.
     *
     * @param activity The activity to attach the overlay to
     */
    fun enable(activity: Activity) {
        if (_isEnabled.value) return

        activityRef = WeakReference(activity)
        windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        density = activity.resources.displayMetrics.density

        createOverlayView(activity)

        _isEnabled.value = true
    }

    /**
     * Disables the measurement overlay and cleans up resources.
     */
    fun disable() {
        if (!_isEnabled.value) return

        removeOverlayView()

        windowManager = null
        activityRef = null
        firstPoint = null

        _isEnabled.value = false
        _currentMeasurement.value = null
        _selectedView.value = null
    }

    /**
     * Sets the measurement mode.
     *
     * @param newMode The measurement mode to use
     */
    fun setMode(newMode: MeasurementMode) {
        _mode.value = newMode
        clear()
    }

    /**
     * Updates the measurement configuration.
     *
     * @param newConfig The new configuration to apply
     */
    fun updateConfig(newConfig: MeasurementConfig) {
        _config.value = newConfig
        notifyOverlayUpdate()
    }

    /**
     * Measures a specific view programmatically.
     *
     * @param view The view to measure
     */
    fun measureView(view: View) {
        val measurement = createViewMeasurement(view)
        _selectedView.value = measurement
        notifyOverlayUpdate()
    }

    /**
     * Clears the current measurement and resets state.
     */
    fun clear() {
        firstPoint = null
        _currentMeasurement.value = null
        _selectedView.value = null
        notifyOverlayUpdate()
    }

    /**
     * Sets the overlay update listener for custom overlay views.
     */
    fun setOverlayUpdateListener(listener: (MeasurementResult?, ViewMeasurement?, MeasurementConfig) -> Unit) {
        overlayUpdateListener = listener
    }

    private fun createOverlayView(activity: Activity) {
        overlayView = MeasurementInterceptorView(activity) { event ->
            processTouchEvent(event)
        }

        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            overlayView = null
            _isEnabled.value = false
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

    private fun processTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked != MotionEvent.ACTION_DOWN) {
            return false
        }

        val x = event.x
        val y = event.y

        when (_mode.value) {
            MeasurementMode.DISTANCE -> handleDistanceMeasurement(x, y)
            MeasurementMode.VIEW_BOUNDS -> handleViewBoundsMeasurement(x, y)
        }

        return true
    }

    private fun handleDistanceMeasurement(x: Float, y: Float) {
        val snappedX: Float
        val snappedY: Float

        if (_config.value.snapToGrid) {
            val gridSizePx = _config.value.gridSize * density
            snappedX = (x / gridSizePx).toInt() * gridSizePx
            snappedY = (y / gridSizePx).toInt() * gridSizePx
        } else {
            snappedX = x
            snappedY = y
        }

        val point = MeasurementResult.Point(snappedX, snappedY)

        if (firstPoint == null) {
            // First tap - set start point
            firstPoint = point
            _currentMeasurement.value = MeasurementResult(
                startPoint = point,
                endPoint = point,
                distancePx = 0f,
                distanceDp = 0f,
                angle = 0f,
                timestamp = System.currentTimeMillis(),
            )
        } else {
            // Second tap - calculate measurement
            val startPoint = firstPoint!!
            val distancePx = calculateDistance(startPoint, point)
            val distanceDp = distancePx / density
            val angle = calculateAngle(startPoint, point)

            _currentMeasurement.value = MeasurementResult(
                startPoint = startPoint,
                endPoint = point,
                distancePx = distancePx,
                distanceDp = distanceDp,
                angle = angle,
                timestamp = System.currentTimeMillis(),
            )

            // Reset for next measurement
            firstPoint = null
        }

        notifyOverlayUpdate()
    }

    private fun handleViewBoundsMeasurement(x: Float, y: Float) {
        val activity = activityRef?.get() ?: return
        val decorView = activity.window?.decorView ?: return

        // Find the view at the touch coordinates
        val targetView = findViewAtPosition(decorView, x.toInt(), y.toInt())

        if (targetView != null && targetView !== overlayView) {
            val measurement = createViewMeasurement(targetView)
            _selectedView.value = measurement
        } else {
            _selectedView.value = null
        }

        notifyOverlayUpdate()
    }

    private fun findViewAtPosition(root: View, x: Int, y: Int): View? {
        if (root.visibility != View.VISIBLE) return null
        if (root === overlayView) return null

        val location = IntArray(2)
        root.getLocationOnScreen(location)

        val left = location[0]
        val top = location[1]
        val right = left + root.width
        val bottom = top + root.height

        // Check if point is within this view's bounds
        if (x < left || x > right || y < top || y > bottom) {
            return null
        }

        // If it's a ViewGroup, recursively check children (reverse order for topmost)
        if (root is ViewGroup) {
            for (i in root.childCount - 1 downTo 0) {
                val child = root.getChildAt(i)
                val result = findViewAtPosition(child, x, y)
                if (result != null) {
                    return result
                }
            }
        }

        return root
    }

    private fun createViewMeasurement(view: View): ViewMeasurement {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

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

        // Try to get resource ID name
        val resourceId = try {
            if (view.id != View.NO_ID) {
                view.resources.getResourceEntryName(view.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        return ViewMeasurement(
            viewClass = view.javaClass.simpleName,
            resourceId = resourceId,
            widthPx = view.width,
            heightPx = view.height,
            widthDp = view.width / density,
            heightDp = view.height / density,
            x = location[0],
            y = location[1],
            paddingLeft = view.paddingLeft,
            paddingTop = view.paddingTop,
            paddingRight = view.paddingRight,
            paddingBottom = view.paddingBottom,
            marginLeft = marginLeft,
            marginTop = marginTop,
            marginRight = marginRight,
            marginBottom = marginBottom,
        )
    }

    private fun calculateDistance(p1: MeasurementResult.Point, p2: MeasurementResult.Point): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun calculateAngle(p1: MeasurementResult.Point, p2: MeasurementResult.Point): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y

        // Calculate angle in radians, then convert to degrees
        // atan2 returns angle from -PI to PI, we convert to 0-360
        var angleDegrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

        // Normalize to 0-360 range
        if (angleDegrees < 0) {
            angleDegrees += 360f
        }

        return angleDegrees
    }

    private fun notifyOverlayUpdate() {
        overlayUpdateListener?.invoke(
            _currentMeasurement.value,
            _selectedView.value,
            _config.value,
        )
        overlayView?.invalidate()
    }

    companion object {
        private const val TAG = "MeasurementEngine"
    }
}

/**
 * Transparent view that intercepts touch events for measurement.
 * Unlike touch visualization, this view consumes touch events to enable precise measurement.
 */
@SuppressLint("ViewConstructor")
private class MeasurementInterceptorView(
    context: Context,
    private val onTouchEvent: (MotionEvent) -> Boolean,
) : View(context) {

    init {
        // Make view completely transparent
        setBackgroundColor(0x00000000)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = onTouchEvent.invoke(event)
        return handled || super.onTouchEvent(event)
    }
}
