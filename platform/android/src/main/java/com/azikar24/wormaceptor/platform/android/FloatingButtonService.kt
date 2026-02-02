package com.azikar24.wormaceptor.platform.android

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlin.math.abs

/**
 * A foreground service that displays a draggable floating button using WindowManager.
 * The button can be dragged around the screen and snaps to the nearest edge when released.
 * Tapping the button opens the ViewerActivity.
 */
class FloatingButtonService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private lateinit var layoutParams: WindowManager.LayoutParams

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Touch handling state
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    // For detecting tap vs drag
    private var touchStartTime = 0L
    private var totalMovement = 0f

    // Animation state for pressed effect - using MutableState wrapper
    private val pressedScaleState: MutableState<Float> = mutableStateOf(1f)

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "wormaceptor_floating_channel"
        private const val NOTIFICATION_ID = 9999
        private const val TAP_THRESHOLD_MS = 200L
        private const val MOVEMENT_THRESHOLD_PX = 20f
        private const val BUTTON_SIZE_DP = 56
        private const val EDGE_MARGIN_DP = 8

        // Preference keys for persisting position
        private const val PREFS_NAME = "wormaceptor_floating_prefs"
        private const val PREF_X = "floating_x"
        private const val PREF_Y = "floating_y"

        /**
         * Check if the app can draw overlays.
         */
        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.provider.Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        /**
         * Start the floating button service.
         */
        fun start(context: Context) {
            val intent = Intent(context, FloatingButtonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Stop the floating button service.
         */
        fun stop(context: Context) {
            context.stopService(Intent(context, FloatingButtonService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        setupFloatingButton()

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        floatingView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
                // View might already be removed
            }
        }
        floatingView = null
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "WormaCeptor Floating Button",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows network inspector floating button"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        // Create intent to open ViewerActivity when notification is tapped
        val openIntent = getLaunchIntent()
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, openIntent, pendingIntentFlags)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("WormaCeptor")
                .setContentText("Network inspector is active")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("WormaCeptor")
                .setContentText("Network inspector is active")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }

    private fun setupFloatingButton() {
        // Determine window type based on API level
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val buttonSizePx = (BUTTON_SIZE_DP * density).toInt()

        // Restore saved position or use default
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedX = prefs.getInt(PREF_X, Int.MIN_VALUE)
        val savedY = prefs.getInt(PREF_Y, Int.MIN_VALUE)

        layoutParams = WindowManager.LayoutParams(
            buttonSizePx,
            buttonSizePx,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = if (savedX != Int.MIN_VALUE) savedX else 0
            y = if (savedY != Int.MIN_VALUE) savedY else (resources.displayMetrics.heightPixels / 4)
        }

        // Create ComposeView for the floating button
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingButtonService)
            setViewTreeSavedStateRegistryOwner(this@FloatingButtonService)

            setContent {
                FloatingButtonContent(pressedScaleState = pressedScaleState)
            }
        }

        // Set up touch handling
        composeView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    touchStartTime = System.currentTimeMillis()
                    totalMovement = 0f
                    isDragging = false
                    pressedScaleState.value = FloatingButtonConstants.Visual.PRESSED_SCALE
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    totalMovement += abs(deltaX) + abs(deltaY)

                    if (totalMovement > MOVEMENT_THRESHOLD_PX) {
                        isDragging = true
                    }

                    layoutParams.x = initialX + deltaX.toInt()
                    layoutParams.y = initialY + deltaY.toInt()

                    try {
                        windowManager.updateViewLayout(view, layoutParams)
                    } catch (_: Exception) {
                        // View might have been removed
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    pressedScaleState.value = FloatingButtonConstants.Visual.NORMAL_SCALE
                    val touchDuration = System.currentTimeMillis() - touchStartTime

                    if (!isDragging && touchDuration < TAP_THRESHOLD_MS) {
                        // Tap detected - open ViewerActivity
                        openViewerActivity()
                    } else {
                        // Drag ended - snap to edge
                        snapToEdge()
                    }
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    pressedScaleState.value = FloatingButtonConstants.Visual.NORMAL_SCALE
                    true
                }

                else -> false
            }
        }

        floatingView = composeView

        try {
            windowManager.addView(composeView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun snapToEdge() {
        val screenWidth = resources.displayMetrics.widthPixels
        val density = resources.displayMetrics.density
        val buttonSizePx = (BUTTON_SIZE_DP * density).toInt()
        val edgeMarginPx = (EDGE_MARGIN_DP * density).toInt()

        // Determine which edge is closer
        val centerX = layoutParams.x + buttonSizePx / 2
        val targetX = if (centerX < screenWidth / 2) {
            edgeMarginPx // Snap to left
        } else {
            screenWidth - buttonSizePx - edgeMarginPx // Snap to right
        }

        // Clamp Y to screen bounds
        val screenHeight = resources.displayMetrics.heightPixels
        val statusBarHeight = getStatusBarHeight()
        val minY = statusBarHeight + edgeMarginPx
        val maxY = screenHeight - buttonSizePx - edgeMarginPx
        val targetY = layoutParams.y.coerceIn(minY, maxY)

        // Animate to target position
        animateToPosition(targetX, targetY)
    }

    private fun animateToPosition(targetX: Int, targetY: Int) {
        val startX = layoutParams.x
        val startY = layoutParams.y

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = FloatingButtonConstants.Animation.SNAP_DURATION_MS
            interpolator = DecelerateInterpolator()

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                layoutParams.x = (startX + (targetX - startX) * progress).toInt()
                layoutParams.y = (startY + (targetY - startY) * progress).toInt()

                floatingView?.let { view ->
                    try {
                        windowManager.updateViewLayout(view, layoutParams)
                    } catch (_: Exception) {
                        // View might have been removed
                    }
                }
            }
        }

        animator.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Save position after animation completes
                savePosition(targetX, targetY)
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })

        animator.start()
    }

    private fun savePosition(x: Int, y: Int) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(PREF_X, x)
            .putInt(PREF_Y, y)
            .apply()
    }

    private fun getStatusBarHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

    private fun openViewerActivity() {
        val intent = getLaunchIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun getLaunchIntent(): Intent {
        // Use reflection to get the launch intent from WormaCeptorApi
        // This avoids a direct dependency on api:client module
        return try {
            val apiClass = Class.forName("com.azikar24.wormaceptor.api.WormaCeptorApi")
            val instanceField = apiClass.getDeclaredField("INSTANCE")
            val instance = instanceField.get(null)
            val method = apiClass.getMethod("getLaunchIntent", Context::class.java)
            method.invoke(instance, this) as Intent
        } catch (_: Exception) {
            // Fallback: try to launch ViewerActivity directly
            try {
                val viewerClass = Class.forName("com.azikar24.wormaceptor.feature.viewer.ViewerActivity")
                Intent(this, viewerClass)
            } catch (_: Exception) {
                Intent()
            }
        }
    }
}

@Composable
private fun FloatingButtonContent(pressedScaleState: MutableState<Float>) {
    val scale by animateFloatAsState(
        targetValue = pressedScaleState.value,
        animationSpec = tween(durationMillis = FloatingButtonConstants.Animation.SCALE_DURATION_MS),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .size(FloatingButtonConstants.Dimensions.BUTTON_SIZE)
            .scale(scale)
            .shadow(
                elevation = FloatingButtonConstants.Dimensions.SHADOW_ELEVATION,
                shape = CircleShape,
            )
            .background(
                color = FloatingButtonConstants.Visual.BUTTON_COLOR,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Open WormaCeptor",
            tint = FloatingButtonConstants.Visual.ICON_TINT,
            modifier = Modifier
                .size(FloatingButtonConstants.Dimensions.ICON_SIZE)
                .alpha(FloatingButtonConstants.Visual.ICON_ALPHA),
        )
    }
}
