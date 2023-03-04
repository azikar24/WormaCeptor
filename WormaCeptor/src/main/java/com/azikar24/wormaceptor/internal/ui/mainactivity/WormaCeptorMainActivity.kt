/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import com.azikar24.wormaceptor.internal.support.UIHelper
import com.azikar24.wormaceptor.internal.ui.features.NavGraphs
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.ramcosta.composedestinations.DestinationsNavHost

class WormaCeptorMainActivity : ComponentActivity() {
    private lateinit var mNotificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNotificationHelper = NotificationHelper(baseContext)
        WormaCeptorToolbar.activity = this
        WindowCompat.setDecorFitsSystemWindows(window, false)
        UIHelper.fullScreen(window.decorView, window)

        setContent {
            WormaCeptorMainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    DestinationsNavHost(navGraph = NavGraphs.root)
                }
            }
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