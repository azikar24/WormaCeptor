/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import com.azikar24.wormaceptor.databinding.ActivityWormaceptorMainBinding
import com.azikar24.wormaceptor.internal.support.NotificationHelper

class WormaCeptorMainActivity : AppCompatActivity() {
    private lateinit var mNotificationHelper: NotificationHelper

    lateinit var binding: ActivityWormaceptorMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNotificationHelper = NotificationHelper(baseContext)

        binding = ActivityWormaceptorMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            view.updatePadding(bottom = 0)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        IN_FOREGROUND = true
        mNotificationHelper.dismiss()
    }

    override fun onPause() {
        super.onPause()
        IN_FOREGROUND = false
    }
    companion object {
        var IN_FOREGROUND = false

    }
}