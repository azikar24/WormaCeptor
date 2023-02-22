/*
 * Copyright AziKar24 22/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding

object UIHelper {

    fun isGestureNavigationEnabled(contentResolver: ContentResolver): Boolean {
        return Settings.Secure.getInt(contentResolver, "navigation_mode", -1) == 2
    }

    fun fullScreen(parentView: View?, window: Window? = null) {
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        parentView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { view, insets ->
                @SuppressLint("WrongConstant")
                val keyboardInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsets.Type.ime()).bottom
                } else {
                    calculateHeightDiff(parentView)
                }
                @SuppressLint("WrongConstant")
                val bottomInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsets.Type.systemGestures()).bottom
                } else {
                    (48 * Resources.getSystem().displayMetrics.density).toInt()
                }
                view.updatePadding(
                    top = 0,
                    bottom =
                    if (keyboardInsets > 0)
                        keyboardInsets
                    else if (isGestureNavigationEnabled(it.context.contentResolver))
                        0
                    else
                        bottomInsets
                )
                insets
            }
        }
    }

    fun calculateHeightDiff(parentView: View): Int {
        val rect = Rect()
        parentView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = parentView.rootView.height
        val heightDiff = screenHeight - rect.bottom

        return if (heightDiff > screenHeight * 0.15) {
            heightDiff
        } else 0

    }
}
