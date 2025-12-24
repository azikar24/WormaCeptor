/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.mainactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import com.azikar24.wormaceptor.internal.ui.features.home.HomeScreen
import com.azikar24.wormaceptor.internal.di.wormaCeptorModule
import com.azikar24.wormaceptor.ui.components.WormaCeptorToolbar
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class WormaCeptorMainActivity : ComponentActivity() {
    private lateinit var mNotificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin {
            androidContext(this@WormaCeptorMainActivity)
            modules(wormaCeptorModule)
        }
        enableEdgeToEdge()
        mNotificationHelper = NotificationHelper(baseContext)
        WormaCeptorToolbar.activity = this


        setContent {
            WormaCeptorMainTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen()
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

    override fun onDestroy() {
        stopKoin()
        super.onDestroy()
    }
}